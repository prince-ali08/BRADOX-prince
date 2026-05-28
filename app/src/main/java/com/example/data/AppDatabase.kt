package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Entities
@Entity(tableName = "generated_websites")
data class GeneratedWebsite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val title: String,
    val htmlContent: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val isInnerCircle: Boolean = false
)

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey val phoneNumber: String,
    val paymentDate: Long = System.currentTimeMillis(),
    val transactionId: String,
    val amount: Double = 50.0
)

// 2. DAOs
@Dao
interface GeneratedWebsiteDao {
    @Query("SELECT * FROM generated_websites ORDER BY createdAt DESC")
    fun getAllWebsites(): Flow<List<GeneratedWebsite>>

    @Query("SELECT * FROM generated_websites WHERE id = :id")
    suspend fun getWebsiteById(id: Int): GeneratedWebsite?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebsite(website: GeneratedWebsite): Long

    @Query("DELETE FROM generated_websites WHERE id = :id")
    suspend fun deleteWebsite(id: Int)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getContactByPhone(phone: String): Contact?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact): Long

    @Update
    suspend fun updateContact(contact: Contact)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContact(id: Int)
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getSubscriptionByPhone(phone: String): Subscription?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription)

    @Query("DELETE FROM subscriptions WHERE phoneNumber = :phone")
    suspend fun removeSubscription(phone: String)
}

// 3. App Database
@Database(
    entities = [GeneratedWebsite::class, Contact::class, Subscription::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun generatedWebsiteDao(): GeneratedWebsiteDao
    abstract fun contactDao(): ContactDao
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "website_creator_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
