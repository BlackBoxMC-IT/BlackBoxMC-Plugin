package it.blackboxmc.plugin.listeners

import it.blackboxmc.plugin.BungeeCordPlugin
import net.md_5.bungee.api.connection.ProxiedPlayer

class BungeeCordBlacklistChecker(private val plugin: BungeeCordPlugin) : Runnable {

    override fun run() {
        for (player: ProxiedPlayer in plugin.proxy.players) {
            plugin.proxy.scheduler.runAsync(plugin) {
                try {
                    plugin.checkAndKickPlayer(player.uniqueId, player)
                } catch (e: Exception) {
                    plugin.logger.severe("Errore durante il controllo periodico della blacklist per il giocatore ${player.name}: ${e.message}")
                }
            }
        }
    }
}