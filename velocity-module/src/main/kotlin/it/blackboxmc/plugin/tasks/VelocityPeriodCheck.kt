@file:Suppress("PrivatePropertyName", "SpellCheckingInspection")

package it.blackboxmc.plugin.tasks

import com.google.gson.JsonParser
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.slf4j.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.TimeUnit

class VelocityPeriodCheck(private val server: ProxyServer, private val logger: Logger) {
    private val API_URL = "https://blackboxmc.it"

    fun startTask() {
        server.scheduler.buildTask(this, Runnable {
            checkOnlinePlayers()
        }).repeat(1, TimeUnit.SECONDS).schedule()
    }

    private fun checkOnlinePlayers() {
        for (player in server.allPlayers) {
            checkAndKickPlayer(player)
        }
    }

    private fun checkAndKickPlayer(player: Player) {
        server.scheduler.buildTask(this, Runnable {
            try {
                val uuidWithoutDashes = player.uniqueId.toString().replace("-", "")
                val client = HttpClient.newBuilder().build()
                val request = HttpRequest.newBuilder()
                    .uri(URI.create("$API_URL/api/blacklist/check/$uuidWithoutDashes"))
                    .GET()
                    .build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() == 200) {
                    val jsonResponse = JsonParser.parseString(response.body()).asJsonObject
                    if (jsonResponse.has("blacklisted") && jsonResponse.get("blacklisted").asBoolean) {
                        val reason = jsonResponse.get("reason").asString
                        val category = jsonResponse.get("category").asString
                        val kickMessage = Component.text()
                            .append(Component.text("--------------------------------------").color(NamedTextColor.RED).decorate(TextDecoration.BOLD))
                            .append(Component.newline())
                            .append(Component.text("SEI STATO BLACKLISTATO GLOBALMENTE!").color(NamedTextColor.RED).decorate(TextDecoration.BOLD))
                            .append(Component.newline())
                            .append(Component.newline())
                            .append(Component.text("Motivazione: ").color(NamedTextColor.GRAY))
                            .append(Component.text(reason).color(NamedTextColor.WHITE))
                            .append(Component.newline())
                            .append(Component.text("Categoria: ").color(NamedTextColor.GRAY))
                            .append(Component.text(category).color(NamedTextColor.WHITE))
                            .append(Component.newline())
                            .append(Component.newline())
                            .append(Component.text("Per contestare la blacklist, apri un ticket sul nostro Discord:").color(NamedTextColor.RED))
                            .append(Component.newline())
                            .append(Component.text("https://discord.blackboxmc.it").color(NamedTextColor.RED).decorate(TextDecoration.UNDERLINED))
                            .append(Component.newline())
                            .append(Component.text("--------------------------------------").color(NamedTextColor.RED).decorate(TextDecoration.BOLD))
                            .build()
                        player.disconnect(kickMessage)
                    }
                }
            } catch (e: Exception) {
                logger.error("Errore nella chiamata API per la blacklist per ${player.username}: ${e.message}")
            }
        }).schedule()
    }
}