package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Standalone data classes for JSON matching
    private data class PartJson(val text: String)
    private data class ContentJson(val parts: List<PartJson>)
    private data class RequestJson(
        val contents: List<ContentJson>,
        val systemInstruction: ContentJson? = null
    )

    /**
     * Calls Gemini API to generate standalone, fully styled HTML content based on the user description.
     */
    suspend fun generateWebsite(description: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured or using default placeholder. Falling back to local template generator.")
            return@withContext getOfflineMockWebpage(description)
        }

        val prompt = """
            You are an expert web developer.
            The user wants a beautiful, fully functional website based on this description: "$description".
            
            Generate a single-file, standalone HTML5 webpage with beautiful, professional CSS styling embedded in a `<style>` block. 
            Use modern layout standards, beautiful color palettes, modern fonts, clear headers, sections, elegant cards, buttons, visual polish, and responsive design.
            Include interactive touches like smooth state color transitions or basic JS animations. Use CDN for Tailwind or Google Fonts to make it look highly professional and modern.
            Include high-quality stock image URLs (from Unsplash or similar free platforms) that fit the topic exactly.
            
            IMPORTANT: Return ONLY the raw HTML code itself. 
            Do NOT wrap it in markdown code blocks like ```html ... ``` or explain anything. Just provide the complete HTML.
        """.trimIndent()

        val requestJson = RequestJson(
            contents = listOf(ContentJson(parts = listOf(PartJson(prompt)))),
            systemInstruction = ContentJson(parts = listOf(PartJson("You are a master of web development that generates production-grade, highly polished, fully styled static HTML pages. You NEVER return instructions or conversational chatter, ONLY raw HTML.")))
        )

        val requestAdapter = moshi.adapter(RequestJson::class.java)
        val jsonString = requestAdapter.toJson(requestJson)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonString.toRequestBody(mediaType)

        val url = "$BASE_URL?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Gemini call failed: Code ${response.code}, Body: $errBody")
                    throw IOException("HTTP error ${response.code}: $errBody")
                }

                val resBody = response.body?.string() ?: throw IOException("Empty response body")
                Log.d(TAG, "Raw Gemini response successfully received.")

                val parsedText = parseGeminiResponse(resBody)
                if (parsedText.isNullOrBlank()) {
                    throw IOException("Failed to extract text content from response.")
                }

                // Clean up in case Gemini wraps the response in Markdown fences anyway
                return@withContext cleanHtmlCode(parsedText)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during generation: ${e.message}", e)
            return@withContext """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Generation Error</title>
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: #fafafa; padding: 40px; color: #333; text-align: center; }
                        .card { background: white; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); padding: 30px; max-width: 500px; margin: auto; }
                        h1 { color: #e53e3e; margin-bottom: 10px; }
                        p { line-height: 1.6; }
                        code { background: #fee2e2; color: #9b1c1c; padding: 4px 8px; border-radius: 4px; font-family: monospace; }
                        button { background: #3182ce; color: white; border: none; padding: 10px 20px; border-radius: 6px; font-weight: bold; cursor: pointer; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <h1>Generation Failed</h1>
                        <p>We ran into an issue requesting your website from Gemini:</p>
                        <p><code>${e.localizedMessage ?: "Unknown network exception"}</code></p>
                        <p>Please double-check your Gemini API key in the AI Studio SECRETS panel.</p>
                        <button onclick="window.location.reload()">Retry rendering</button>
                    </div>
                </body>
                </html>
            """.trimIndent()
        }
    }

    private fun parseGeminiResponse(json: String): String? {
        return try {
            val element = moshi.adapter(Map::class.java).fromJson(json)
            val candidates = element?.get("candidates") as? List<*>
            val firstCandidate = candidates?.firstOrNull() as? Map<*, *>
            val content = firstCandidate?.get("content") as? Map<*, *>
            val parts = content?.get("parts") as? List<*>
            val firstPart = parts?.firstOrNull() as? Map<*, *>
            firstPart?.get("text") as? String
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing JSON schema", e)
            null
        }
    }

    private fun cleanHtmlCode(response: String): String {
        var clean = response.trim()
        if (clean.startsWith("```html", ignoreCase = true)) {
            clean = clean.removePrefix("```html").removePrefix("```HTML")
            if (clean.endsWith("```")) {
                clean = clean.removeSuffix("```")
            }
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
            if (clean.endsWith("```")) {
                clean = clean.removeSuffix("```")
            }
        }
        return clean.trim()
    }

    /**
     * Graceful fallback when API Key is missing. Generates outstanding templates instantly local to show functional excellence.
     */
    private fun getOfflineMockWebpage(description: String): String {
        val cleanTopic = description.replace(Regex("[^a-zA-Z0-9 ]"), "").take(40)
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$cleanTopic</title>
                <script src="https://cdn.tailwindcss.com"></script>
                <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;600;700&display=swap" rel="stylesheet">
                <style>
                    body { font-family: 'Plus Jakarta Sans', sans-serif; }
                </style>
            </head>
            <body class="bg-slate-50 text-slate-950 flex flex-col min-h-screen">
                <!-- Navigation -->
                <header class="bg-white/80 backdrop-blur-md sticky top-0 border-b border-slate-100 z-50">
                    <div class="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
                        <div class="flex items-center gap-2">
                            <span class="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white font-bold text-lg">W</span>
                            <span class="font-bold text-xl text-slate-900 tracking-tight">LaunchWeb</span>
                        </div>
                        <nav class="hidden md:flex items-center gap-8 text-sm font-semibold text-slate-600">
                            <a href="#features" class="hover:text-indigo-600 transition">Features</a>
                            <a href="#about" class="hover:text-indigo-600 transition">About Us</a>
                            <a href="#contact" class="hover:text-indigo-600 transition">Get in Touch</a>
                        </nav>
                        <button class="bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-sm px-5 py-2.5 rounded-xl transition shadow-lg shadow-indigo-600/15">
                            Connect
                        </button>
                    </div>
                </header>

                <!-- Hero Section -->
                <main class="flex-grow">
                    <section class="max-w-6xl mx-auto px-6 py-16 md:py-24 flex flex-col items-center text-center">
                        <div class="inline-flex items-center gap-2 bg-indigo-50 text-indigo-700 font-semibold text-xs px-4 py-1.5 rounded-full mb-6">
                            <span>✨ AI Generated Draft Platform</span>
                        </div>
                        <h1 class="text-4xl md:text-6xl font-extrabold text-slate-950 tracking-tight max-w-4xl mb-6 leading-tight">
                            Your Custom Web Space for <span class="bg-clip-text text-transparent bg-gradient-to-r from-indigo-600 to-violet-500">$cleanTopic</span>
                        </h1>
                        <p class="text-lg text-slate-600 max-w-2xl mb-8 leading-relaxed">
                            Formulated based on your description: <span class="italic font-medium text-slate-800">"$description"</span>. Clean layout, intuitive interactions, and gorgeous Material aesthetics.
                        </p>
                        <div class="flex flex-col sm:flex-row gap-4">
                            <a href="#contact" class="bg-indigo-600 hover:bg-indigo-700 text-white font-semibold px-8 py-4 rounded-xl transition shadow-lg shadow-indigo-600/20 text-center">
                                Get Started Today
                            </a>
                            <a href="#features" class="bg-white hover:bg-slate-100 text-slate-900 border border-slate-200 font-semibold px-8 py-4 rounded-xl transition text-center col-span-1">
                                Tour Features
                            </a>
                        </div>
                    </section>

                    <!-- Features Section -->
                    <section id="features" class="bg-slate-100 py-16 md:py-24">
                        <div class="max-w-6xl mx-auto px-6">
                            <div class="text-center mb-12">
                                <h2 class="text-3xl font-extrabold text-slate-950">Fitted Core Offerings</h2>
                                <p class="text-slate-600 mt-2">Tailormade features matching standard consumer requirements.</p>
                            </div>
                            <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
                                <div class="bg-white p-8 rounded-2xl border border-slate-100 shadow-sm hover:translate-y-[-4px] transition duration-300">
                                    <div class="w-12 h-12 rounded-xl bg-orange-100 flex items-center justify-center text-orange-600 font-bold mb-6 text-xl">⚡</div>
                                    <h3 class="font-bold text-xl text-slate-950 mb-3">Lightning Loaded</h3>
                                    <p class="text-slate-600 text-sm leading-relaxed">Optimized from head to toe to build lightning fast page loads in seconds.</p>
                                </div>
                                <div class="bg-white p-8 rounded-2xl border border-slate-100 shadow-sm hover:translate-y-[-4px] transition duration-300">
                                    <div class="w-12 h-12 rounded-xl bg-violet-100 flex items-center justify-center text-violet-600 font-bold mb-6 text-xl">📱</div>
                                    <h3 class="font-bold text-xl text-slate-950 mb-3">100% Mobile Ready</h3>
                                    <p class="text-slate-600 text-sm leading-relaxed">Responsive cards that flow effortlessly across desktops, tablet screens, and smartphone viewports.</p>
                                </div>
                                <div class="bg-white p-8 rounded-2xl border border-slate-100 shadow-sm hover:translate-y-[-4px] transition duration-300">
                                    <div class="w-12 h-12 rounded-xl bg-green-100 flex items-center justify-center text-green-600 font-bold mb-6 text-xl">🌿</div>
                                    <h3 class="font-bold text-xl text-slate-950 mb-3">Organic SEO Code</h3>
                                    <p class="text-slate-600 text-sm leading-relaxed">Clean structured semantic tags tailored nicely for immediate Google search crawling.</p>
                                </div>
                            </div>
                        </div>
                    </section>

                    <!-- Contact Form -->
                    <section id="contact" class="max-w-4xl mx-auto px-6 py-16 md:py-24 text-center">
                        <h2 class="text-3xl font-extrabold text-slate-950 mb-4">Request Consultation</h2>
                        <p class="text-slate-600 mb-8">Ready to bring your $cleanTopic concept to a professional domain? Fill in your details below!</p>
                        
                        <form class="bg-white p-8 rounded-2xl border border-slate-200 text-left max-w-xl mx-auto shadow-xl" onsubmit="event.preventDefault(); alert('Consultation requested! Our representatives will contact you shortly.')">
                            <div class="mb-4">
                                <label class="block text-sm font-semibold text-slate-700 mb-2">Your Name</label>
                                <input type="text" placeholder="John Doe" class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-600 text-sm" required />
                            </div>
                            <div class="mb-4">
                                <label class="block text-sm font-semibold text-slate-700 mb-2">Email Address</label>
                                <input type="email" placeholder="john@example.com" class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-600 text-sm" required />
                            </div>
                            <button type="submit" class="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-3.5 rounded-xl transition mt-2">
                                Send Message
                            </button>
                        </form>
                    </section>
                </main>

                <!-- Footer -->
                <footer class="bg-slate-950 text-slate-400 py-12 border-t border-slate-900 text-center text-sm">
                    <p class="mb-2">&copy; 2026 Website Creator App. All rights reserved.</p>
                    <p class="text-slate-600">Built via Generative AI Integration</p>
                </footer>
            </body>
            </html>
        """.trimIndent()
    }
}
