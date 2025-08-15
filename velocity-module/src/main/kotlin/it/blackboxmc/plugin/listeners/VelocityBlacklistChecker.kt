package it.blackboxmc.plugin.listeners

import it.blackboxmc.plugin.VelocityPlugin
import org.bson.Document
import java.util.concurrent.ConcurrentHashMap

class VelocityBlacklistChecker(private val plugin: VelocityPlugin) : Runnable {

    private val blacklistedPlayers = ConcurrentHashMap<String, Document>()

    override fun run() {
        try {
            val cursor = plugin.blacklistCollection.find().iterator()
            val newBlacklist = ConcurrentHashMap<String, Document>()

            while (cursor.hasNext()) {
                val doc = cursor.next()
                val uuid = doc.getString("uuid")
                if (uuid != null) {
                    newBlacklist[uuid] = doc
                }
            }

            blacklistedPlayers.clear()
            blacklistedPlayers.putAll(newBlacklist)
        } catch (e: Exception) {
            plugin.logger.error("Errore durante l'aggiornamento della blacklist: ${e.message}", e)
        }
    }

    fun isPlayerBlacklisted(uuid: String): Document? {
        return blacklistedPlayers[uuid]
    }
}