package dev.groovin.hydrophobia

import dev.groovin.hydrophobia.commands.HydrophobiaCommand
import dev.groovin.hydrophobia.listeners.RainDamageListener
import dev.groovin.hydrophobia.listeners.WaterDamageListener
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class Hydrophobia : JavaPlugin() {
    companion object {
        lateinit var instance: Hydrophobia
            private set
    }

    override fun onEnable() {
        instance = this

        // Save default config if it doesn't exist
        saveDefaultConfig()

        // Register listeners
        registerListeners()

        // Register commands
        registerCommands()

        logger.info("Hydrophobia has been enabled!")
    }

    override fun onDisable() {
        logger.info("Hydrophobia has been disabled!")
    }

    private fun registerListeners() {
        val listeners: List<Listener> = listOf(
            WaterDamageListener(),
            RainDamageListener(),
        )

        listeners.forEach { listener ->
            server.pluginManager.registerEvents(listener, this)
        }
    }

    private fun registerCommands() {
        getCommand("hydrophobia")?.setExecutor(HydrophobiaCommand())
    }
}
