package dev.groovin.hydrophobia.commands

import dev.groovin.hydrophobia.Hydrophobia
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class HydrophobiaCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("hydrophobia.admin")) {
            sender.sendMessage("${ChatColor.RED}You don't have permission to use this command!")
            return true
        }

        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
            "reload" -> {
                Hydrophobia.instance.reloadConfig()
                sender.sendMessage("${ChatColor.GREEN}Configuration reloaded!")
            }
            "help" -> sendHelp(sender)
            else -> {
                sender.sendMessage("${ChatColor.RED}Unknown command. Use /hydrophobia help for help.")
            }
        }

        return true
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("${ChatColor.GOLD}=== Hydrophobia Help ===")
        sender.sendMessage("${ChatColor.YELLOW}/hydrophobia reload ${ChatColor.WHITE}- Reload the configuration")
        sender.sendMessage("${ChatColor.YELLOW}/hydrophobia help ${ChatColor.WHITE}- Show this help message")
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>,
    ): List<String> {
        if (!sender.hasPermission("hydrophobia.admin")) {
            return emptyList()
        }

        return when (args.size) {
            1 -> listOf("reload", "help"),
            else -> emptyList(),
        }
    }
} 