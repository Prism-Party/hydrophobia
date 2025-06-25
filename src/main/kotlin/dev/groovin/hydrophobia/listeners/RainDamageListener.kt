package dev.groovin.hydrophobia.listeners

import dev.groovin.hydrophobia.Hydrophobia
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.block.Biome
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.potion.PotionEffectType
import java.util.concurrent.ConcurrentHashMap

class RainDamageListener : Listener {
    private val lastDamageTime = ConcurrentHashMap<Player, Long>()
    private val damageCooldown = 1000L // 1 second in milliseconds

    @EventHandler
    fun onWeatherChange(event: WeatherChangeEvent) {
        val world = event.world

        // Check if the world is configured for water damage
        if (!Hydrophobia.instance.config.getStringList("worlds").contains(world.name)) {
            return
        }

        // Check if rain damage is enabled
        if (!Hydrophobia.instance.config.getBoolean("rain-as-water-damage")) {
            return
        }

        // If it's starting to rain
        if (event.toWeatherState()) {
            // Broadcast rain warning message
            val message = Hydrophobia.instance.config.getString("messages.rain-started", "")
            if (message != null && message.isNotEmpty()) {
                world.players.forEach { player ->
                    player.sendMessage(message.replace("&", "§"))
                }
            }
        }
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val world = player.world

        // Check if the world is configured for water damage
        if (!Hydrophobia.instance.config.getStringList("worlds").contains(world.name)) {
            return
        }

        // Check if player has bypass permission
        if (player.hasPermission("hydrophobia.bypass")) {
            return
        }

        // Check if rain damage is enabled
        if (!Hydrophobia.instance.config.getBoolean("rain-as-water-damage")) {
            return
        }

        // Check if it's raining in the world
        if (!world.hasStorm()) {
            return
        }

        // Check if player is in a biome where it doesn't rain
        if (!canRainAt(player.location)) {
            return
        }

        // Check if player has water breathing effect and it's enabled in config
        if (Hydrophobia.instance.config.getBoolean("safe-with-potion") &&
            player.hasPotionEffect(PotionEffectType.WATER_BREATHING)
        ) {
            return
        }

        // Check if player is under a block
        if (player.location.block.lightFromSky < 15) {
            return
        }

        // Apply damage with cooldown
        if (Hydrophobia.instance.config.getBoolean("water-damage-players")) {
            val currentTime = System.currentTimeMillis()
            val lastDamage = lastDamageTime.getOrDefault(player, 0L)

            if (currentTime - lastDamage >= damageCooldown) {
                val damage = Hydrophobia.instance.config.getDouble("water-damage-per-tick")
                player.damage(damage)

                // Play damage sound
                val soundName = Hydrophobia.instance.config.getString("damage-sound", "ENTITY_PLAYER_HURT_DROWN")
                try {
                    val soundKey = NamespacedKey.fromString(soundName?.lowercase() ?: "ENTITY_PLAYER_HURT_DROWN")
                        ?: NamespacedKey.minecraft("entity.player.hurt_drown")
                    val sound = Registry.SOUNDS.get(soundKey) ?: Sound.ENTITY_PLAYER_HURT_DROWN
                    player.playSound(player.location, sound, 1.0f, 1.0f)
                } catch (e: IllegalArgumentException) {
                    Hydrophobia.instance.logger.warning("Invalid sound name: $soundName")
                }

                lastDamageTime[player] = currentTime
            }
        }
    }

    fun canRainAt(location: Location): Boolean {
        val world = location.world ?: return false
        val biome = world.getBiome(location.blockX, location.blockY, location.blockZ)

        // Check for biomes where it doesn't rain
        val biomeAllowsRain = when (biome) {
            // Desert biomes - no rain
            Biome.DESERT,
            // Nether biomes - no rain
            Biome.NETHER_WASTES,
            Biome.SOUL_SAND_VALLEY,
            Biome.CRIMSON_FOREST,
            Biome.WARPED_FOREST,
            Biome.BASALT_DELTAS,
            // End biomes - no rain
            Biome.THE_END,
            Biome.END_HIGHLANDS,
            Biome.END_MIDLANDS,
            Biome.END_BARRENS,
            Biome.SMALL_END_ISLANDS,
            // Cold biomes that have snow instead of rain
            Biome.SNOWY_BEACH,
            Biome.SNOWY_TAIGA,
            Biome.ICE_SPIKES,
            Biome.FROZEN_RIVER,
            Biome.FROZEN_OCEAN,
            Biome.DEEP_FROZEN_OCEAN,
            -> false
            // All other biomes support rain
            else -> true
        }

        // If biome doesn't allow rain, return false
        if (!biomeAllowsRain) {
            return false
        }

        // Check if there are any blocks above the player
        return location.blockY >= world.getHighestBlockYAt(location)
    }
}
