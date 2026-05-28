package com.example.data

import kotlinx.coroutines.flow.Flow

class Repository(private val db: AppDatabase) {

    val allWebsites: Flow<List<GeneratedWebsite>> = db.generatedWebsiteDao().getAllWebsites()
    val allContacts: Flow<List<Contact>> = db.contactDao().getAllContacts()

    suspend fun getWebsiteById(id: Int): GeneratedWebsite? {
        return db.generatedWebsiteDao().getWebsiteById(id)
    }

    suspend fun insertWebsite(website: GeneratedWebsite): Long {
        return db.generatedWebsiteDao().insertWebsite(website)
    }

    suspend fun deleteWebsite(id: Int) {
        db.generatedWebsiteDao().deleteWebsite(id)
    }

    suspend fun getContactByPhone(phone: String): Contact? {
        return db.contactDao().getContactByPhone(phone.trim())
    }

    suspend fun insertContact(contact: Contact): Long {
        return db.contactDao().insertContact(contact.copy(phoneNumber = contact.phoneNumber.trim()))
    }

    suspend fun updateContact(contact: Contact) {
        db.contactDao().updateContact(contact.copy(phoneNumber = contact.phoneNumber.trim()))
    }

    suspend fun deleteContact(id: Int) {
        db.contactDao().deleteContact(id)
    }

    suspend fun getSubscriptionByPhone(phone: String): Subscription? {
        return db.subscriptionDao().getSubscriptionByPhone(phone.trim())
    }

    suspend fun insertSubscription(subscription: Subscription) {
        db.subscriptionDao().insertSubscription(subscription.copy(phoneNumber = subscription.phoneNumber.trim()))
    }

    suspend fun removeSubscription(phone: String) {
        db.subscriptionDao().removeSubscription(phone.trim())
    }

    suspend fun seedContactsIfNeeded() {
        // Let's see if we already have contacts
        val count = db.contactDao().getContactByPhone("0715871815")
        if (count == null) {
            // Seed a few useful testing contacts
            val initialContacts = listOf(
                Contact(name = "Host (Bradox)", phoneNumber = "0715871815", isInnerCircle = true),
                Contact(name = "Mercy Wanjiku", phoneNumber = "0712345678", isInnerCircle = true),
                Contact(name = "Kiplagat Peter", phoneNumber = "0722111222", isInnerCircle = false),
                Contact(name = "Amina Hassan", phoneNumber = "0733444555", isInnerCircle = true),
                Contact(name = "John Doe", phoneNumber = "0799000111", isInnerCircle = false)
            )
            for (contact in initialContacts) {
                db.contactDao().insertContact(contact)
            }
        }
    }
}
