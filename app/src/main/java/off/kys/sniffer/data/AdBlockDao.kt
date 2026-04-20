package off.kys.sniffer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AdBlockDao {
    @Query("SELECT COUNT(*) FROM blocked_domains")
    suspend fun getRowCount(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_domains WHERE domain = :domain)")
    suspend fun isBlocked(domain: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(domains: List<BlockedDomain>)

    @Query("DELETE FROM blocked_domains")
    suspend fun clearAll()
}