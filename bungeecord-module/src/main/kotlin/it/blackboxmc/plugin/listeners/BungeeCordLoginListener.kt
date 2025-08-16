@file:Suppress("PrivatePropertyName", "SpellCheckingInspection")

package it.blackboxmc.plugin.listeners

import com.google.gson.JsonParser
import it.blackboxmc.plugin.BungeeCordPlugin
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class BungeeCordLoginListener(private val plugin: BungeeCordPlugin) : Listener {
    private val API_URL = "https://blackboxmc.it"

    @EventHandler
    fun onPostLogin(event: PostLoginEvent) {
        val player = event.player
        plugin.proxy.scheduler.runAsync(plugin) {
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
                }
            } catch (e: Exception) {
                plugin.logger.severe("Errore nella chiamata API per la blacklist per ${player.name}: ${e.message}")
            }
        }
    }
}