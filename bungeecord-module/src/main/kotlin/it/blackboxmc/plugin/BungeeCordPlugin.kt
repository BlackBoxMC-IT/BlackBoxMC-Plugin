package it.blackboxmc.plugin

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import it.blackboxmc.plugin.commands.BbmcCommand
import it.blackboxmc.plugin.config.YamlConfig
import it.blackboxmc.plugin.listeners.BungeeCordBlacklistChecker
import it.blackboxmc.plugin.listeners.BungeeCordLoginListener
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import org.bson.Document
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

class BungeeCordPlugin : Plugin() {
    private val MONGODB_URI = "mongodb://INDIRIZZO_IP:27017/BlackBoxMC"
    private lateinit var mongoClient: MongoClient
    lateinit var blacklistCollection: MongoCollection<Document>
    lateinit var participantsCollection: MongoCollection<Document>
    private lateinit var serverName: String
    private var isRegistered = false

    override fun onEnable() {
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
        val configFile = File(dataFolder, "config.yml")
        if (!configFile.exists()) {
            try {
                getResourceAsStream("config.yml").use { input ->
                    configFile.outputStream().use { output ->
                        input?.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val config = YamlConfig(configFile)
        val apiKey = config.getString("api-key")
        val founderName = config.getString("founder-name")
        val serverIconUrl = config.getString("server-icon-url")

        if (apiKey.isNullOrEmpty() || apiKey == "INSERISCI_LA_TUA_CHIAVE_API_QUI" || founderName.isNullOrEmpty()) {
            logger.warning("Impossibile avviare il plugin BlackBoxMC: L'API key, il nome del server o il nome del fondatore non sono stati configurati.")
            logger.warning("Modifica il file 'config.yml' con i dati corretti e riavvia il server.")
            return
        }

        try {
            mongoClient = MongoClients.create(MONGODB_URI)
            val database: MongoDatabase = mongoClient.getDatabase("BlackBoxMC")
            blacklistCollection = database.getCollection("blacklists")
            participantsCollection = database.getCollection("participants")
        } catch (e: Exception) {
            logger.severe("Errore di connessione a MongoDB: ${e.message}")
            return
        }

        serverName = config.getString("server-name")!!
        isRegistered = true

        proxy.scheduler.schedule(this, {
            val filter = Document("serverName", serverName)
            val update = Document("\$set", Document("serverName", serverName)
                .append("founderName", founderName)
                .append("serverIconUrl", serverIconUrl)
                .append("api_key", apiKey))

            val options = UpdateOptions().upsert(true)
            participantsCollection.updateOne(filter, update, options)
        }, 0, 5, TimeUnit.SECONDS)

        proxy.pluginManager.registerListener(this, BungeeCordLoginListener(this))
        proxy.pluginManager.registerCommand(this, BbmcCommand())
        proxy.scheduler.schedule(this, BungeeCordBlacklistChecker(this), 5, 5, TimeUnit.SECONDS)
        logger.info("BlackBoxMC BungeeCord Plugin è stato abilitato!")
    }

    override fun onDisable() {
        if (::mongoClient.isInitialized) {
            if (isRegistered && ::serverName.isInitialized && !serverName.isNullOrEmpty()) {
                val filter = Document("serverName", serverName)
                participantsCollection.deleteOne(filter)
            }
            mongoClient.close()
        }
        logger.info("BlackBoxMC BungeeCord Plugin è stato disabilitato!")
    }

    fun checkAndKickPlayer(uuid: UUID, connection: Any) {
        val playerUuid = uuid.toString()
        val query = Filters.eq("minecraft_uuid", playerUuid)
        val banEntry: Document? = blacklistCollection.find(query).first()

        if (banEntry != null) {
            val kickMessage = """
                ${ChatColor.RED}${ChatColor.BOLD}--------------------------------------
                ${ChatColor.RED}${ChatColor.BOLD}SEI STATO BLACKLISTATO GLOBALMENTE!
                
                ${ChatColor.GRAY}Motivazione: ${ChatColor.WHITE}${banEntry.getString("reason")}
                ${ChatColor.GRAY}Categoria: ${ChatColor.WHITE}${banEntry.getString("category")}
                
                ${ChatColor.RED}Per contestare la blacklist, apri un ticket sul nostro Discord:
                ${ChatColor.RED}${ChatColor.UNDERLINE}https://discord.blackboxmc.it
                ${ChatColor.RED}${ChatColor.BOLD}--------------------------------------
            """.trimIndent()

            val component = TextComponent(kickMessage)
            when (connection) {
                is ProxiedPlayer -> connection.disconnect(component)
                is PendingConnection -> connection.disconnect(component)
            }
        }
    }
}