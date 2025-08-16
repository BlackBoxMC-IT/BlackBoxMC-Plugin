@file:Suppress("SpellCheckingInspection")

package it.blackboxmc.plugin.commands

import com.velocitypowered.api.command.SimpleCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class BbmcCommand : SimpleCommand {

    override fun execute(invocation: SimpleCommand.Invocation) {
        val sender = invocation.source()

        val header = Component.text("--------------------------------------")
            .color(NamedTextColor.RED)
            .decorate(TextDecoration.BOLD)
        sender.sendMessage(header)

        val message = Component.text("Questo server è protetto da ")
            .color(NamedTextColor.GRAY)
            .append(
                Component.text("BlackBoxMC")
                    .color(NamedTextColor.RED)
                    .decorate(TextDecoration.BOLD)
                    .clickEvent(ClickEvent.openUrl("https://blackboxmc.it"))
                    .hoverEvent(HoverEvent.showText(Component.text("Clicca per visitare il sito")))
            )
            .append(Component.text("!"))
        sender.sendMessage(message)

        val footer = Component.text("--------------------------------------")
            .color(NamedTextColor.RED)
            .decorate(TextDecoration.BOLD)
        sender.sendMessage(footer)
    }
}