package off.kys.sniffer.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

private const val TAG = "AdBlocker"

object AdBlocker {
    private var database: AdDatabase? = null
    private val dao get() = database?.adBlockDao()

    @Volatile
    private var isInitialized = false

    private val sources = listOf(
        "https://adaway.org/hosts.txt",
        "https://pgl.yoyo.org/adservers/serverlist.php?hostformat=hosts&showintro=0&mimetype=plaintext",
        "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts",
        "https://mtxadmin.github.io/hosts_pi-hole.txt"
    )

    /**
     * Call this once at app start.
     * Performs a sync if the database is as empty.
     */
    suspend fun initialize(context: Context) = withContext(Dispatchers.IO) {
        if (isInitialized) {
            Log.d(TAG, "Already initialized. Ignoring redundant call.")
            return@withContext
        }

        Log.d(TAG, "Initializing AdBlocker...")
        database = AdDatabase.getDatabase(context)

        val count = dao?.getRowCount() ?: 0
        Log.d(TAG, "Current database record count: $count")

        if (count == 0) {
            Log.i(TAG, "Database is empty. Starting initial download...")
            downloadAndSync()
        }

        isInitialized = true
        Log.i(TAG, "AdBlocker Initialized (SQLite Mode)")
    }

    /**
     * Downloads and parses the lists.
     */
    suspend fun downloadAndSync() = withContext(Dispatchers.IO) {
        Log.i(TAG, "Starting sync for ${sources.size} sources...")
        val startTime = System.currentTimeMillis()
        val allDomains = mutableSetOf<String>()

        coroutineScope {
            sources.map { sourceUrl ->
                launch {
                    runCatching {
                        Log.v(TAG, "Fetching: $sourceUrl")
                        URL(sourceUrl).openStream().bufferedReader().useLines { lines ->
                            var lineCount = 0
                            lines.forEach { line ->
                                extractDomain(line)?.let {
                                    allDomains.add(it)
                                    lineCount++
                                }
                            }
                            Log.v(TAG, "Extracted $lineCount domains from $sourceUrl")
                        }
                    }.onFailure { Log.e(TAG, "Failed to download $sourceUrl", it) }
                }
            }.joinAll()
        }

        val extractionTime = System.currentTimeMillis() - startTime
        Log.d(TAG, "Extraction finished. Total unique domains: ${allDomains.size}. Took ${extractionTime}ms. Starting DB write...")

        dao?.let { adDao ->
            try {
                adDao.clearAll()
                // Write to SQLite in chunks to avoid blowing up the transaction or memory
                allDomains.chunked(5000).forEachIndexed { index, chunk ->
                    adDao.insertAll(chunk.map { BlockedDomain(it) })
                    Log.v(TAG, "Inserted chunk ${index + 1} (${chunk.size} records)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Database write failed", e)
            }
        }

        Log.i(TAG, "Sync Complete. Total Domains: ${allDomains.size} | Total Time: ${System.currentTimeMillis() - startTime}ms")
    }

    private fun extractDomain(line: String): String? {
        val content = line.trim()
        if (content.isEmpty() || content.startsWith('#') || content.startsWith('!') || content.startsWith(';')) {
            return null
        }

        val cleanLine = content.split('#')[0].trim()
        val parts = cleanLine.split(Regex("\\s+"))

        val candidate = when {
            parts.size >= 2 && (parts[0] == "0.0.0.0" || parts[0] == "127.0.0.1") -> parts[1]
            parts.isNotEmpty() -> parts[0]
            else -> return null
        }

        val domain = candidate.lowercase().removePrefix("www.")
        return if (domain.contains('.') && !domain.startsWith('.')) domain else null
    }

    suspend fun isAd(host: String): Boolean {
        if (!isInitialized) {
            Log.w(TAG, "isAd called before initialization!")
            return false
        }
        if (host.isEmpty()) return false

        var current = host.lowercase().removePrefix("www.")

        while (current.contains('.')) {
            if (dao?.isBlocked(current) == true) {
                Log.d(TAG, "BLOCKED: $host (matched $current)")
                return true
            }
            val nextDot = current.indexOf('.')
            if (nextDot == -1) break
            current = current.substring(nextDot + 1)
        }
        return false
    }
}