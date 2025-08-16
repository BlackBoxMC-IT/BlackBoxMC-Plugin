@file:Suppress("UnstableApiUsage", "unused", "PrivatePropertyName", "SpellCheckingInspection")

package it.blackboxmc.plugin

import com.google.gson.JsonObject
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import it.blackboxmc.plugin.commands.BbmcCommand
import it.blackboxmc.plugin.config.YamlConfig
import it.blackboxmc.plugin.listeners.VelocityLoginListener
import it.blackboxmc.plugin.tasks.VelocityPeriodCheck
import org.slf4j.Logger
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path

@Plugin(
    id = "blackboxmc",
    name = "BlackBoxMC",
    version = "v1.0b1rc",
    description = "BlackBoxMC protegge il tuo server 24h.",
    url = "https://blackboxmc.it",
    authors = ["StrayVibes"]
)
class VelocityPlugin @Inject constructor(
    private val server: ProxyServer,
    private val logger: Logger,
    @DataDirectory private val dataDirectory: Path
) {
    private val API_URL = "https://blackboxmc.it"
    private lateinit var serverName: String
    private lateinit var founderName: String
    private var isRegistered = false

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        val dataFolderFile = dataDirectory.toFile()
        if (!dataFolderFile.exists()) {
            dataFolderFile.mkdir()
        }
        val configFile = File(dataFolderFile, "config.yml")
        if (!configFile.exists()) {
            try {
                this::class.java.getResourceAsStream("/config.yml").use { input ->
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
            logger.warn("Impossibile avviare il plugin BlackBoxMC: il nome del server o il nome del fondatore non sono stati configurati.")
            logger.warn("Modifica il file 'config.yml' con i dati corretti e riavvia il server.")
            return
        }

        server.eventManager.register(this, VelocityLoginListener(server, logger))
        server.commandManager.register(server.commandManager.metaBuilder("bbmc").build(), BbmcCommand())
        registerParticipant(serverIconUrl)

        val periodCheckTask = VelocityPeriodCheck(server, logger)
        periodCheckTask.startTask()
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        if (isRegistered) {
            unregisterParticipant()
        }
        logger.info("BlackBoxMC Velocity Plugin è stato disabilitato!")
    }

    private fun registerParticipant(serverIconUrl: String?) {
        server.scheduler.buildTask(this, Runnable {
            try {
                val jsonPayload = JsonObject().apply {
                    addProperty("serverName", serverName)
                    addProperty("founderName", founderName)
                    addProperty("apiKey", "llHXQ7KCP3PvMObURcc24EElV68ic8ac")
                    addProperty("serverIconUrl", serverIconUrl)
                }.toString()

                val client = HttpClient.newBuilder().build()
                val request = HttpRequest.newBuilder()
                    .uri(URI.create("$API_URL/api/participants"))
                    .header("Content-Type", "application/json")
                    .header("x-secret-key", "llHXQ7KCP3PvMObURcc24EElV68ic8ac")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() == 201) {
                    isRegistered = true
                    logger.info("Server '$serverName' aggiunto con successo.")
                } else {
                    logger.error("Errore nella registrazione del server: ${response.statusCode()} - ${response.body()}")
                }
            } catch (e: Exception) {
                logger.error("Errore di rete durante la registrazione del server: ${e.message}")
            }
        }).schedule()
    }

    private fun unregisterParticipant() {
        server.scheduler.buildTask(this, Runnable {
            try {
                val jsonPayload = JsonObject().apply {
                    addProperty("serverName", serverName)
                }.toString()

                val client = HttpClient.newBuilder().build()
                val request = HttpRequest.newBuilder()
                    .uri(URI.create("$API_URL/api/participants/unregister"))
                    .header("Content-Type", "application/json")
                    .header("x-secret-key", "llHXQ7KCP3PvMObURcc24EElV68ic8ac")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build()

                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                if (response.statusCode() == 200) {
                    logger.info("Server '$serverName' rimosso con successo.")
                } else {
                    logger.error("Errore nella rimozione del server: ${response.statusCode()} - ${response.body()}")
                }
            } catch (e: Exception) {
                logger.error("Errore di rete durante la rimozione del server: ${e.message}")
            }
        }).schedule()
    }
}