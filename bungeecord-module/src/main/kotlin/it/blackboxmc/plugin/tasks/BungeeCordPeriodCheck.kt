@file:Suppress("PrivatePropertyName", "SpellCheckingInspection")

package it.blackboxmc.plugin.tasks

import com.google.gson.JsonParser
import it.blackboxmc.plugin.BungeeCordPlugin
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.TimeUnit

class BungeeCordPeriodCheck(private val plugin: BungeeCordPlugin) {
    private val API_URL = "https://blackboxmc.it"

    fun startTask() {
        plugin.proxy.scheduler.schedule(plugin, {
            checkOnlinePlayers()
        }, 0L, 1, TimeUnit.SECONDS)
    }

    private fun checkOnlinePlayers() {
        for (player in plugin.proxy.players) {
            checkAndKickPlayer(player)
        }
    }

    private fun checkAndKickPlayer(player: ProxiedPlayer) {
        plugin.proxy.scheduler.runAsync(plugin) {
            try {
                if (player.isConnected) {
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
                            val kickMessage = """
                                ${ChatColor.RED}${ChatColor.BOLD}--------------------------------------
                                ${ChatColor.RED}${ChatColor.BOLD}SEI STATO BLACKLISTATO GLOBALMENTE!
                                
                                ${ChatColor.GRAY}Motivazione: ${ChatColor.WHITE}${reason}
                                ${ChatColor.GRAY}Categoria: ${ChatColor.WHITE}${category}
                                
                                ${ChatColor.RED}Per contestare la blacklist, apri un ticket sul nostro Discord:
                                ${ChatColor.RED}${ChatColor.UNDERLINE}https://discord.blackboxmc.it
                                ${ChatColor.RED}${ChatColor.BOLD}--------------------------------------
                            """.trimIndent()
                            player.disconnect(TextComponent(kickMessage))
                        }
                    } else {
                        plugin.logger.severe("Errore nella chiamata API per la blacklist per ${player.name}: ${response.statusCode()} - ${response.body()}")
                    }
                }
            } catch (e: Exception) {
                plugin.logger.severe("Errore nella chiamata API per la blacklist per ${player.name}: ${e.message}")
            }
        }
    }
}