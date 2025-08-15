package it.blackboxmc.plugin.commands

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import net.md_5.bungee.api.plugin.Command

class BbmcCommand : Command("bbmc") {

    override fun execute(sender: CommandSender, args: Array<String>) {
        val header = TextComponent("--------------------------------------")
        header.color = ChatColor.RED
        header.isBold = true
        sender.sendMessage(header)

        val message = TextComponent("Questo server è protetto da ")
        message.color = ChatColor.GRAY

        val link = TextComponent("BlackBoxMC")
        link.color = ChatColor.RED
        link.isBold = true
        link.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, "https://blackboxmc.it")
        link.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Clicca per visitare il sito"))

        message.addExtra(link)
        message.addExtra(TextComponent("!"))
        sender.sendMessage(message)

        val footer = TextComponent("--------------------------------------")
        footer.color = ChatColor.RED
        footer.isBold = true
        sender.sendMessage(footer)
    }
}