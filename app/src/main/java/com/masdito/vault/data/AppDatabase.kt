package com.masdito.vault.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "credentials")
data class Credential(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val username: String,
    val secret: String,
    val category: String,
    val websiteUrl: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Dao
interface CredentialDao {
    @Query("SELECT * FROM credentials ORDER BY title ASC")
    fun getAllCredentials(): Flow<List<Credential>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(credential: Credential)

    @Delete
    suspend fun delete(credential: Credential)
}

@Database(entities = [Credential::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun credentialDao(): CredentialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vault_offline_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
