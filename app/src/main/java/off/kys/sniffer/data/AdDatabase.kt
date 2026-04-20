package off.kys.sniffer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BlockedDomain::class], version = 1, exportSchema = false)
abstract class AdDatabase : RoomDatabase() {
    abstract fun adBlockDao(): AdBlockDao

    companion object {
        @Volatile
        private var INSTANCE: AdDatabase? = null

        fun getDatabase(context: Context): AdDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AdDatabase::class.java,
                    "adblock_database"
                )
                    // If you change the schema (add columns), this wipes the old data
                    .fallbackToDestructiveMigration(false)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}