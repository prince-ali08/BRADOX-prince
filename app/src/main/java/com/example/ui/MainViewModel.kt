package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Builder : Screen()
    object Contacts : Screen()
    data class ViewWebsite(val websiteId: Int) : Screen()
}

sealed class UserStatus {
    data class ExemptFree(val contactName: String) : UserStatus()
    data class SubscribedPaid(val transactionId: String, val paidAt: Long) : UserStatus()
    object UnpaidRestricted : UserStatus()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: Repository

    // Active App State Flow
    val allWebsites: StateFlow<List<GeneratedWebsite>>
    val allContacts: StateFlow<List<Contact>>

    // UI Interactive States
    val activeScreen = MutableStateFlow<Screen>(Screen.Builder)
    val currentUserPhone = MutableStateFlow("0715871815") // Start with the host number (free check)
    val inputDescription = MutableStateFlow("")
    val isGenerating = MutableStateFlow(false)
    val generationError = MutableStateFlow<String?>(null)

    // Logged-in/Simulated profile details
    val currentProfileName = MutableStateFlow("Bradox Prince")

    // Subscription Dialog States
    val mpesaTxInput = MutableStateFlow("")
    val showPaymentSuccessAlert = MutableStateFlow(false)

    // User Status tracking state
    val userStatus: StateFlow<UserStatus>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = Repository(db)

        allWebsites = repository.allWebsites
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allContacts = repository.allContacts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Combine inputs to form user authorization state in real-time
        userStatus = combine(currentUserPhone, allContacts) { phone, contacts ->
            val contact = contacts.firstOrNull { it.phoneNumber.trim() == phone.trim() }
            if (contact != null && contact.isInnerCircle) {
                UserStatus.ExemptFree(contact.name)
            } else {
                val subscription = repository.getSubscriptionByPhone(phone)
                if (subscription != null) {
                    UserStatus.SubscribedPaid(subscription.transactionId, subscription.paymentDate)
                } else {
                    UserStatus.UnpaidRestricted
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStatus.UnpaidRestricted)

        // Seed with sample contacts if database is empty on start
        viewModelScope.launch {
            repository.seedContactsIfNeeded()
            // Pull name if defaults matches
            updateProfileNameForCurrentPhone()
        }
    }

    fun updateProfilePhone(phone: String) {
        currentUserPhone.value = phone.trim()
        viewModelScope.launch {
            updateProfileNameForCurrentPhone()
        }
    }

    private suspend fun updateProfileNameForCurrentPhone() {
        val phone = currentUserPhone.value
        val contact = repository.getContactByPhone(phone)
        if (contact != null) {
            currentProfileName.value = contact.name
        } else {
            currentProfileName.value = "Guest Reader"
        }
    }

    // Website logic
    fun generateNewWebsite() {
        val desc = inputDescription.value.trim()
        if (desc.isEmpty()) return

        viewModelScope.launch {
            isGenerating.value = true
            generationError.value = null
            try {
                // Call Gemini Client integration
                val html = GeminiClient.generateWebsite(desc)
                
                // Formulate a professional title from description
                val title = if (desc.length > 25) desc.take(25) + "..." else desc

                val website = GeneratedWebsite(
                    description = desc,
                    title = title,
                    htmlContent = html
                )
                val id = repository.insertWebsite(website)
                
                // Open website viewer
                inputDescription.value = ""
                activeScreen.value = Screen.ViewWebsite(id.toInt())
            } catch (e: Exception) {
                generationError.value = e.localizedMessage ?: "Unknown compilation error"
            } finally {
                isGenerating.value = false
            }
        }
    }

    fun deleteWebsite(id: Int) {
        viewModelScope.launch {
            repository.deleteWebsite(id)
            if (activeScreen.value is Screen.ViewWebsite && (activeScreen.value as Screen.ViewWebsite).websiteId == id) {
                activeScreen.value = Screen.Builder
            }
        }
    }

    // Contacts logic
    fun addContact(name: String, phone: String, isInnerCircle: Boolean) {
        viewModelScope.launch {
            val contact = Contact(name = name, phoneNumber = phone.trim(), isInnerCircle = isInnerCircle)
            repository.insertContact(contact)
            if (phone.trim() == currentUserPhone.value.trim()) {
                updateProfileNameForCurrentPhone()
            }
        }
    }

    fun toggleInnerCircle(contact: Contact) {
        viewModelScope.launch {
            val updated = contact.copy(isInnerCircle = !contact.isInnerCircle)
            repository.updateContact(updated)
        }
    }

    fun deleteContact(id: Int) {
        viewModelScope.launch {
            repository.deleteContact(id)
        }
    }

    // Payments Logic
    fun processMpesaPayment() {
        val txId = mpesaTxInput.value.uppercase().trim()
        if (txId.length < 5) return

        viewModelScope.launch {
            val sub = Subscription(
                phoneNumber = currentUserPhone.value,
                transactionId = txId,
                amount = 50.0
            )
            repository.insertSubscription(sub)
            mpesaTxInput.value = ""
            showPaymentSuccessAlert.value = true
        }
    }

    fun resetSubscriptionForCurrentPhone() {
        viewModelScope.launch {
            repository.removeSubscription(currentUserPhone.value)
        }
    }
}
