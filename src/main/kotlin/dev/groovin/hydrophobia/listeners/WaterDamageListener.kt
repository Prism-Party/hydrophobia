package dev.groovin.hydrophobia.listeners

import dev.groovin.hydrophobia.Hydrophobia
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.entity.Boat
import org.bukkit.entity.Damageable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffectType
import java.util.concurrent.ConcurrentHashMap

class WaterDamageListener : Listener {
    private val lastMessageTime = ConcurrentHashMap<Player, Long>()
    private val lastDamageTime = ConcurrentHashMap<Player, Long>()
    private val messageCooldown = 60000L // 1 minute in milliseconds
    private val damageCooldown = 1000L // 1 second in milliseconds
    private val boatHealthMap = ConcurrentHashMap<Boat, Double>()
    private val maxBoatHealth = 20.0

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

        // Check if player is in water
        if (player.location.block.type == Material.WATER) {
            // Check if player has water breathing effect and it's enabled in config
            if (Hydrophobia.instance.config.getBoolean("safe-with-potion") &&
                player.hasPotionEffect(PotionEffectType.WATER_BREATHING)
            ) {
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

                    // Send message with cooldown
                    val lastMessage = lastMessageTime.getOrDefault(player, 0L)
                    if (currentTime - lastMessage >= messageCooldown) {
                        val message = Hydrophobia.instance.config.getString("messages.touched-water", "")
                        if (message != null && message.isNotEmpty()) {
                            player.sendMessage(message.replace("&", "§"))
                        }
                        lastMessageTime[player] = currentTime
                    }
                }
            }
        }
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        val entity = event.entity
        val world = entity.world

        // Check if the world is configured for water damage
        if (!Hydrophobia.instance.config.getStringList("worlds").contains(world.name)) {
            return
        }

        when {
            // Handle boat damage
            entity is Boat && Hydrophobia.instance.config.getBoolean("water-damage-boats") -> {
                if (entity.location.block.type == Material.WATER) {
                    handleBoatDamage(entity)
                }
            }
            // Handle damageable entities
            entity is Damageable && Hydrophobia.instance.config.getBoolean("water-damage-entities") -> {
                if (entity.location.block.type == Material.WATER) {
                    val damage = Hydrophobia.instance.config.getDouble("water-damage-per-tick")
                    val newHealth = entity.health - damage

                    if (newHealth <= 0) {
                        entity.remove()
                    } else {
                        entity.health = newHealth
                    }
                }
            }
        }
    }

    private fun handleBoatDamage(boat: Boat) {
        val currentHealth = boatHealthMap.getOrDefault(boat, maxBoatHealth)
        val damage = Hydrophobia.instance.config.getDouble("water-damage-per-tick")
        val newHealth = currentHealth - damage

        if (newHealth <= 0) {
            // Create break effect
            boat.world.spawnParticle(
                org.bukkit.Particle.BLOCK_CRUMBLE,
                boat.location,
                20,
                boat.type.toString().lowercase().replace("_boat", "").let {
                    Material.valueOf("${it.uppercase()}_PLANKS")
                }.createBlockData(),
            )

            // Drop boat item
            boat.world.dropItemNaturally(
                boat.location,
                org.bukkit.inventory.ItemStack(
                    boat.type.name.let {
                        Material.valueOf(it)
                    },
                ),
            )

            boat.remove()
            boatHealthMap.remove(boat)
        } else {
            boatHealthMap[boat] = newHealth

            // Show damage indicator
            boat.world.spawnParticle(
                org.bukkit.Particle.DAMAGE_INDICATOR,
                boat.location.add(0.0, 1.0, 0.0),
                1,
            )
        }
    }
}
