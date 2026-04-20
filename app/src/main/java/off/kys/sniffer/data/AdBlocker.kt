package off.kys.sniffer.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "AdBlocker"

object AdBlocker {
    private val blockedDomains = ConcurrentHashMap.newKeySet<String>()
    private var isLoaded = false

    // A collection of reliable host-file providers
    private val sources = listOf(
        "https://adaway.org/hosts.txt",
        "https://pgl.yoyo.org/adservers/serverlist.php?hostformat=hosts&showintro=0&mimetype=plaintext",
        "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts",
        "https://raw.githubusercontent.com/AdguardTeam/AdguardFilters/master/Filters/sections/adservers_firstparty.txt"
    )

    suspend fun loadList() = withContext(Dispatchers.IO) {
        if (isLoaded) return@withContext

        sources.forEach { sourceUrl ->
            try {
                val content = URL(sourceUrl).readText()
                parseLines(content)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load from $sourceUrl", e)
            }
        }

        isLoaded = blockedDomains.isNotEmpty()
        Log.d(TAG, "Loaded ${blockedDomains.size} unique ad domains.")
    }

    private fun parseLines(content: String) {
        content.lines().forEach { line ->
            val trimmed = line.trim()
            // Skip comments and empty lines
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) return@forEach

            // Handle lines starting with 0.0.0.0 or 127.0.0.1
            val parts = trimmed.split(Regex("\\s+"))
            if (parts.size >= 2 && (parts[0] == "0.0.0.0" || parts[0] == "127.0.0.1")) {
                blockedDomains.add(parts[1].trim())
            } else if (parts.size == 1 && trimmed.contains(".")) {
                // Some lists are just domain names
                blockedDomains.add(trimmed)
            }
        }
    }

    fun isAd(url: String): Boolean {
        if (!isLoaded) return false

        val host = try {
            URL(url).host.lowercase()
        } catch (e: Exception) {
            Log.d(TAG, e.message.orEmpty())
            return false
        }

        // Exact match or subdomain match (e.g., ads.example.com blocked if example.com is in list)
        return blockedDomains.contains(host) || blockedDomains.any { host.endsWith(".$it") }
    }
}