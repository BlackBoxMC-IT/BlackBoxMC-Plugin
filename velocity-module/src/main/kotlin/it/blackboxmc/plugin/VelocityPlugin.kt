package it.blackboxmc.plugin

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.UpdateOptions
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import it.blackboxmc.plugin.commands.BbmcCommand
import it.blackboxmc.plugin.config.YamlConfig
import it.blackboxmc.plugin.listeners.VelocityLoginListener
import org.bson.Document
import org.slf4j.Logger
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Plugin(
    id = "blackboxmc",
    name = "BlackBoxMC-Velocity",
    version = "v1.0b1rc",
    description = "Plugin per la blacklist di BlackBoxMC.",
    authors = ["StrayVibes"]
)
class VelocityPlugin @Inject constructor(val server: ProxyServer, val logger: Logger, private val dataDirectory: Path) {
    private val MONGODB_URI = "mongodb://INDIRIZZO_IP:27017/BlackBoxMC"
    private lateinit var mongoClient: MongoClient
    lateinit var blacklistCollection: MongoCollection<Document>
    lateinit var participantsCollection: MongoCollection<Document>
    private lateinit var serverName: String

    @Inject
    fun onEnable() {
        if (!dataDirectory.toFile().exists()) {
            dataDirectory.toFile().mkdir()
        }
        val configFile = dataDirectory.resolve("config.yml").toFile()
        if (!configFile.exists()) {
            try {
                javaClass.getResourceAsStream("/config.yml")?.use { input ->
                    configFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                logger.error("Impossibile creare il file di configurazione!", e)
            }
        }

        try {
            mongoClient = MongoClients.create(MONGODB_URI)
            val database: MongoDatabase = mongoClient.getDatabase("BlackBoxMC")
            blacklistCollection = database.getCollection("blacklists")
            participantsCollection = database.getCollection("participants")
        } catch (e: Exception) {
            logger.error("Errore di connessione a MongoDB: ${e.message}", e)
            return
        }

        val config = YamlConfig(configFile)
        val apiKey = config.getString("api-key")
        serverName = config.getString("server-name").toString()
        val founderName = config.getString("founder-name")
        val serverIconUrl = config.getString("server-icon-url")

        if (!apiKey.isNullOrEmpty() && serverName.isNotEmpty() && !founderName.isNullOrEmpty()) {
            server.scheduler.buildTask(this, Runnable {
                try {
                    val filter = Document("serverName", serverName)
                    val update = Document("\$set", Document("serverName", serverName)
                        .append("founderName", founderName)
                        .append("serverIconUrl", serverIconUrl)
                        .append("api_key", apiKey))

                    val options = UpdateOptions().upsert(true)
                    participantsCollection.updateOne(filter, update, options)
                } catch (e: Exception) {
                    logger.error("Errore durante l'aggiornamento del documento in MongoDB: ${e.message}", e)
                }
            })
                .repeat(5, TimeUnit.SECONDS)
                .schedule()
        } else {
            logger.warn("Impossibile registrare il server. Controlla che 'api-key', 'server-name' e 'founder-name' siano impostati correttamente nel file di configurazione.")
        }

        server.eventManager.register(this, VelocityLoginListener(this))
        server.commandManager.register("bbmc", BbmcCommand())
        logger.info("BlackBoxMC Velocity Plugin è stato abilitato!")
    }

    @Inject
    fun onDisable() {
        if (::mongoClient.isInitialized) {
            if (::serverName.isInitialized && !serverName.isNullOrEmpty()) {
                val filter = Document("serverName", serverName)
                participantsCollection.deleteOne(filter)
            }
            mongoClient.close()
        }
        logger.info("BlackBoxMC Velocity Plugin è stato disabilitato!")
    }
}