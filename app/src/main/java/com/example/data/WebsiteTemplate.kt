package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class WebsiteTemplate(
    val id: String,
    val title: String,
    val category: String, // "Portfolio", "Business", "Blog", "Event"
    val shortDescription: String,
    val fullPrompt: String,
    val iconName: String, // String representation of icon
    val accentColorHex: Long = 0xFF6366F1
) {
    fun getIcon(): ImageVector {
        return when (iconName) {
            "Brush" -> Icons.Default.Brush
            "Business" -> Icons.Default.Business
            "Coffee" -> Icons.Default.Coffee
            "LocalFlorist" -> Icons.Default.LocalFlorist
            "MedicalServices" -> Icons.Default.MedicalServices
            "SettingsEthernet" -> Icons.Default.SettingsEthernet
            "Explore" -> Icons.Default.Explore
            "MusicNote" -> Icons.Default.MusicNote
            "Restaurant" -> Icons.Default.Restaurant
            "FitnessCenter" -> Icons.Default.FitnessCenter
            else -> Icons.Default.Web
        }
    }
}

object TemplateLibrary {
    val list = listOf(
        // Portfolios (2)
        WebsiteTemplate(
            id = "portfolio_minimalist",
            title = "Minimalist Creative Portfolio",
            category = "Portfolio",
            shortDescription = "High-end designer portfolio screen in modern dark mode.",
            fullPrompt = """A high-end minimalist graphic designer portfolio. 
                Use modern sleek dark background (#111), clean elegant typography. 
                Include a hero section stating 'Crafting Digital Experiences', a floating project gallery grid displaying 4 pieces with realistic mock names (like 'Aura Brand', 'Krypton App', 'Neo Web'), 
                a responsive online work booking inquiry contact form, and smooth neon-purple hover effects.""",
            iconName = "Brush",
            accentColorHex = 0xFFA855F7
        ),
        WebsiteTemplate(
            id = "portfolio_architect",
            title = "Architect Brutalist Showcase",
            category = "Portfolio",
            shortDescription = "Brutalist architecture masonry gallery portfolio.",
            fullPrompt = """An architectural firm showcase with full-bleed geometric hero background (#1a1a1a). 
                Features robust brutalist typography, masonry project layouts, bold horizontal line separations, 
                an interactive team testimonial quote card, and a detailed consultation booking box.""",
            iconName = "Business",
            accentColorHex = 0xFF64748B
        ),
        // Businesses (5)
        WebsiteTemplate(
            id = "business_coffee",
            title = "Rustic Roasters Cafe",
            category = "Business",
            shortDescription = "Cosy cafe landing with warm sepia tones & pricing grid.",
            fullPrompt = """A cosy artisan coffee workshop website. 
                Features warm gold and sepia palettes (#2c1d11) with wood accents. 
                Includes a beautiful espresso-themed hero section, our signature menu grid (with items like 'Single-Origin Pour', 'Caramel Macchiato', 'Artisan Croissant'), 
                opening hours cards of the week, and an embedded reservation request contact sheet.""",
            iconName = "Coffee",
            accentColorHex = 0xFFB45309
        ),
        WebsiteTemplate(
            id = "business_florist",
            title = "Bloom & Stem Florist",
            category = "Business",
            shortDescription = "Pastel spring-themed flower shop layout.",
            fullPrompt = """A beautiful local boutique flower shop landing page. 
                Soft pastel pink and lavender color scheme. Includes beautiful elegant serif headers, 
                an active fresh flower bouquet visual database box, a customized arrangement slider simulation, 
                and a floral delivery booking form.""",
            iconName = "LocalFlorist",
            accentColorHex = 0xFFF472B6
        ),
        WebsiteTemplate(
            id = "business_dental",
            title = "Sparkle Care Den Clinic",
            category = "Business",
            shortDescription = "Professional blue medical page with appointments.",
            fullPrompt = """A professional, premium, reassuring dental clinic landing portal. 
                Utilizes sterile, soft sky blue and clean white layouts. 
                Features a team introduction section, high-quality accordion lists detailing key treatments (such as 'Orthodontics', 'Implants', 'Laser Whitening'), 
                five-star user ratings, and a booking appointment scheduler.""",
            iconName = "MedicalServices",
            accentColorHex = 0xFF06B6D4
        ),
        WebsiteTemplate(
            id = "business_fitness",
            title = "VoltFit Personal Training",
            category = "Business",
            shortDescription = "High-energy bold gym coaching screen.",
            fullPrompt = """An energetic fitness training landing page styled on high contrast dark charcoal and glowing energetic neon yellow colors. 
                Includes bold uppercase action headers, a grid of 3 premium training pricing tier cards, 
                personal coach credentials, and a high-impact booking form to schedule a trial workout.""",
            iconName = "FitnessCenter",
            accentColorHex = 0xFFEAB308
        ),
        // Blogs (2)
        WebsiteTemplate(
            id = "blog_tech",
            title = "Tech Byte Chronicles",
            category = "Blog",
            shortDescription = "Cyberpunk blog feed with category filters.",
            fullPrompt = """A vibrant modern tech and developer blogs layout page. 
                Designed on a cool slate background (#0f172a) with neon cyan and magenta text styling. 
                Includes a featured main article hero spotlight, a recent post list with category tags (like 'Jetpack Compose', 'AI', 'Ktor'), 
                a mock code-snippet layout widget, and a newsletter subscription block.""",
            iconName = "SettingsEthernet",
            accentColorHex = 0xFF06B6D4
        ),
        WebsiteTemplate(
            id = "blog_travel",
            title = "Wanderlust Travel Diary",
            category = "Blog",
            shortDescription = "Scenic blog with masonry itineraries & guides.",
            fullPrompt = """An expansive travel web diary timeline. 
                Uses warm canvas and olive accents. Features stunning large visual hero headers, 
                featured itinerary cards (like '7 Days in Kyoto', 'Exploring Amalfi Coast'), 
                a travel packing checklist simulator, and interactive newsletter signups.""",
            iconName = "Explore",
            accentColorHex = 0xFF10B981
        ),
        // Events (2)
        WebsiteTemplate(
            id = "event_synthwave",
            title = "Retro Synthwave Music Fest",
            category = "Event",
            shortDescription = "Neon festival lineup layout with a ticker.",
            fullPrompt = """An action packed music festival event portal. 
                Styled as highly visual retro synthwave 80s aesthetic with glowing grid backgrounds and neon magenta borders. 
                Includes music artist lineup schedule widgets, a mock ticker tape counting down tickets remaining, 
                and direct access reservation contact box.""",
            iconName = "MusicNote",
            accentColorHex = 0xFFEC4899
        ),
        WebsiteTemplate(
            id = "event_culinary",
            title = "Gourmet Charity Gala",
            category = "Event",
            shortDescription = "Luxurious black & gold dinner ceremony.",
            fullPrompt = """A highly premium, luxury charity gala event website index. 
                Rich black and gold elegant layouts. Includes executive chef introductions, 
                an extensive gourmet plating sample catalog, chronological evening itinerary schedules, 
                and sponsor corporate logos row.""",
            iconName = "Restaurant",
            accentColorHex = 0xFFD97706
        )
    )
}
