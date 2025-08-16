@file:Suppress("PrivatePropertyName", "SpellCheckingInspection")

package it.blackboxmc.plugin
import com.google.gson.JsonObject
import it.blackboxmc.plugin.commands.BbmcCommand
import it.blackboxmc.plugin.config.YamlConfig
import it.blackboxmc.plugin.listeners.BungeeCordLoginListener
import it.blackboxmc.plugin.tasks.BungeeCordPeriodCheck
import net.md_5.bungee.api.plugin.Plugin
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class BungeeCordPlugin : Plugin() {
    private val API_URL = "https://blackboxmc.it"
    private lateinit var serverName: String
    private lateinit var founderName: String
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
        serverName = config.getString("server-name") ?: ""
        founderName = config.getString("founder-name") ?: ""
        val serverIconUrl = config.getString("server-icon-url")
        if (serverName.isEmpty() || founderName.isEmpty()) {
            logger.warning("Impossibile avviare il plugin BlackBoxMC: il nome del server o il nome del fondatore non sono stati configurati.")
            logger.warning("Modifica il file 'config.yml' con i dati corretti e riavvia il server.")
            return
        }

        proxy.pluginManager.registerListener(this, BungeeCordLoginListener(this))
        proxy.pluginManager.registerCommand(this, BbmcCommand())
        registerParticipant(serverIconUrl)

        val periodCheckTask = BungeeCordPeriodCheck(this)
        periodCheckTask.startTask()
    }

    override fun onDisable() {
        if (isRegistered) {
            unregisterParticipant()
        }
        logger.info("BlackBoxMC BungeeCord Plugin è stato disabilitato!")
    }

    private fun registerParticipant(serverIconUrl: String?) {
        proxy.scheduler.runAsync(this) {
            try {
                val jsonPayload = JsonObject().apply {
                    addProperty("serverName", serverName)
                    addProperty("founderName", founderName)
                    addProperty("apiKey", "")
                    addProperty("serverIconUrl", serverIconUrl)
                }.toString()
                val client = HttpClient.newBuilder().build()
                val request = HttpRequest.newBuilder()
                    .uri(URI.create("$API_URL/api/participants"))
                    .header("Content-Type", "application/json")
                    .header("x-secret-key", "")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                if (response.statusCode() == 201) {
                    isRegistered = true
                    logger.info("Server '$serverName' aggiunto con successo.")
                } else {
                    logger.severe("Errore nella registrazione del server: ${response.statusCode()} - ${response.body()}")
                }
            } catch (e: Exception) {
                logger.severe("Errore di rete durante la registrazione del server: ${e.message}")
            }
        }
    }

    private fun unregisterParticipant() {
        try {
            val jsonPayload = JsonObject().apply {
                addProperty("serverName", serverName)
            }.toString()
            val client = HttpClient.newBuilder().build()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$API_URL/api/participants/unregister"))
                .header("Content-Type", "application/json")
                .header("x-secret-key", "")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) {
                logger.info("Server '$serverName' rimosso con successo.")
            } else {
                logger.severe("Errore nella rimozione del server: ${response.statusCode()} - ${response.body()}")
            }
        } catch (e: Exception) {
            logger.severe("Errore di rete durante la rimozione del server: ${e.message}")
        }
    }
}
