package it.blackboxmc.plugin.listeners

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PreLoginEvent
import it.blackboxmc.plugin.VelocityPlugin
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bson.Document
import java.util.concurrent.CompletableFuture

class VelocityLoginListener(private val plugin: VelocityPlugin) {

    @Subscribe
    fun onPreLogin(event: PreLoginEvent) {
        val playerUuid = event.uniqueId
        val playerName = event.username

        CompletableFuture.runAsync {
            try {
                val query = Document("uuid", playerUuid.toString())
                val blacklistDocument = plugin.blacklistCollection.find(query).first()

                if (blacklistDocument != null) {
                    val reason = blacklistDocument.getString("reason") ?: "Nessun motivo specificato."
                    val issuer = blacklistDocument.getString("issuer") ?: "Sconosciuto."
                    val expirationDate = blacklistDocument.getString("expirationDate")

                    val kickMessageString = if (expirationDate != null) {
                        """
                        §cSei stato blacklistato dal network!
                        §cMotivo: §f$reason
                        §cAmminsitratore: §f$issuer
                        §cScadenza: §f$expirationDate
                        """.trimIndent()
                    } else {
                        """
                        §cSei stato blacklistato dal network!
                        §cMotivo: §f$reason
                        §cAmminsitratore: §f$issuer
                        §cScadenza: §fPermanente
                        """.trimIndent()
                    }

                    val kickMessageComponent = LegacyComponentSerializer.legacySection().deserialize(kickMessageString)
                    event.result = PreLoginEvent.PreLoginComponentResult.denied(kickMessageComponent)
                }
            } catch (e: Exception) {
                plugin.logger.error("Errore durante la verifica della blacklist per ${playerName}: ${e.message}", e)
            }
        }
    }
}