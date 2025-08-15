package it.blackboxmc.plugin.listeners

import it.blackboxmc.plugin.BungeeCordPlugin
import net.md_5.bungee.api.connection.PendingConnection
import net.md_5.bungee.api.event.PreLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class BungeeCordLoginListener(private val plugin: BungeeCordPlugin) : Listener {

    @EventHandler
    fun onPreLogin(event: PreLoginEvent) {
        val connection: PendingConnection = event.connection

        event.registerIntent(plugin)

        plugin.proxy.scheduler.runAsync(plugin) {
            try {
                val playerUuid = connection.uniqueId ?: run {
                    return@runAsync
                }
                plugin.checkAndKickPlayer(playerUuid, connection)
            } finally {
                event.completeIntent(plugin)
            }
        }
    }
}