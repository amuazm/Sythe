package me.amuazm.sythe.listeners;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import me.amuazm.sythe.Sythe;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class ChainListener implements Listener {
  private final Sythe sythe;
  private final Map<UUID, Long> lastActionMap = new HashMap<>();
  private final Map<UUID, Long> lastClickMap = new HashMap<>();
  private final Map<UUID, ScheduledTask> taskMap = new HashMap<>();
  private final Map<UUID, Integer> rapidClickCounterMap = new HashMap<>();
  private static final int ACTION_COOLDOWN_MS = 1500;
  private static final int RAPID_CLICK_DELAY_MS = 250;

  public ChainListener(Sythe sythe) {
    this.sythe = sythe;
  }

  @EventHandler
  public void multiClickListener(PlayerInteractEvent event) {
    final Player player = event.getPlayer();
    final Location playerEyeLocation = player.getEyeLocation();

    if (event.getHand() == null) {
      return;
    }

    if (!event.getHand().equals(EquipmentSlot.OFF_HAND)) {
      return;
    }

    if (!player.getEquipment().getItemInOffHand().getType().equals(Material.CHAIN)) {
      return;
    }

    event.setCancelled(true);

    // Initialise Situation for Player p
    final long lastAction = lastActionMap.getOrDefault(player.getUniqueId(), -1L);
    final long currentTime = System.currentTimeMillis();
    final long lastClick = lastClickMap.getOrDefault(player.getUniqueId(), -1L);
    int rapidClickCounter = rapidClickCounterMap.getOrDefault(player.getUniqueId(), 1);
    ScheduledTask currentTask = taskMap.getOrDefault(player.getUniqueId(), null);

    // Check cooldown
    if (lastAction != -1) {
      if ((currentTime - lastAction) < ACTION_COOLDOWN_MS) {
        player.sendMessage(ChatColor.RED + "Your chain is on cooldown!");

        // Cooldown feedback
        player.playSound(player, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1, 1);
        return;
      }
    }

    // Check if player has a lastClick
    if (lastClick != -1) {
      if ((currentTime - lastClick) < RAPID_CLICK_DELAY_MS) {
        rapidClickCounter += 1;
        currentTask.cancel();

        // Click feedback
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1, 1);
      }
    }

    // Execute code based on clicks
    currentTask =
        player
            .getScheduler()
            .runDelayed(
                sythe,
                (scheduledTask) -> {
                  // Get entity to interact with
                  final Entity entity = getRayTracedEntity(player);
                  final Location entityLocation;

                  if (entity == null) {
                    // No entity feedback
                    player.playSound(playerEyeLocation, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                    return;
                  }

                  // Entity Location
                  if (entity instanceof LivingEntity livingEntity) {
                    entityLocation = livingEntity.getEyeLocation();
                  } else {
                    entityLocation = entity.getLocation();
                  }

                  // Sound
                  player.playSound(player, Sound.BLOCK_CHAIN_PLACE, 1, 1);

                  // Particles
                  chainParticles(player, playerEyeLocation, entityLocation);

                  // Pull
                  int clicks = rapidClickCounterMap.get(player.getUniqueId());
                  switch (clicks) {
                    case 1 -> pullEntityToLocation(player, entityLocation, 1.0);
                    case 2 -> {
                      pullEntityToLocation(player, entityLocation, 0.6);
                      pullEntityToLocation(entity, playerEyeLocation, 0.6);
                    }
                    case 3 -> pullEntityToLocation(entity, playerEyeLocation, 1.0);
                    default -> {
                      // Too much feedback
                      player.playSound(player, Sound.ENTITY_GOAT_HORN_BREAK, 1, 1);
                    }
                  }

                  // For next time
                  clicks = 1;
                  rapidClickCounterMap.put(player.getUniqueId(), clicks);
                  lastActionMap.put(player.getUniqueId(), currentTime);
                },
                null,
                (long) (RAPID_CLICK_DELAY_MS / (1 / 20.0 * 1000)));

    // For next time
    lastClickMap.put(player.getUniqueId(), currentTime);
    rapidClickCounterMap.put(player.getUniqueId(), rapidClickCounter);
    taskMap.put(player.getUniqueId(), currentTask);
  }

  public Entity getRayTracedEntity(Player p) {
    final Location pLoc = p.getEyeLocation();
    final Vector v = pLoc.getDirection();
    final Predicate<Entity> filter = entity -> (entity != p);
    final RayTraceResult r;
    // Get ray
    r = p.getWorld().rayTraceEntities(pLoc, v, 50, 2, filter);
    // Get Entity
    return r != null ? r.getHitEntity() : null;
  }

  private void pullEntityToLocation(final Entity entity, Location loc, double multiply) {
    Location entityLoc = entity.getLocation();

    Vector boost = entity.getVelocity();
    boost.setY(0.3);
    entity.setVelocity(boost);

    entity
        .getScheduler()
        .runDelayed(
            sythe,
            (scheduledTask) -> {
              double g = -0.08;
              double d = loc.distance(entityLoc);
              double t = d;
              // speed = distance / time
              // distance = (100 + 7 * blockstotravel)% * axis distance
              // time = blockstotravel
              double v_x = (1.0 + 0.07 * t) * (loc.getX() - entityLoc.getX()) / t;
              double v_y = (1.0 + 0.03 * t) * (loc.getY() - entityLoc.getY()) / t - 0.5 * g * t;
              double v_z = (1.0 + 0.07 * t) * (loc.getZ() - entityLoc.getZ()) / t;

              Vector v = entity.getVelocity();
              v.setX(v_x);
              v.setY(v_y);
              v.setZ(v_z);
              v.multiply(multiply);
              entity.setVelocity(v);
            },
            null,
            1L);
  }

  public void chainParticles(Player p, Location pLoc, Location eLoc) {
    // Particles
    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(19, 40, 56), 1.0F);
    double x = pLoc.getX();
    double y = pLoc.getY();
    double z = pLoc.getZ();
    double distance = pLoc.distance(eLoc);
    double x_interval = (eLoc.getX() - x) / distance;
    double y_interval = (eLoc.getY() - y) / distance;
    double z_interval = (eLoc.getZ() - z) / distance;
    double particles_per_block = 2;
    for (double i = 0; i <= distance; i += (1 / particles_per_block)) {
      p.spawnParticle(
          Particle.REDSTONE,
          x + x_interval * i,
          y + y_interval * i,
          z + z_interval * i,
          1,
          dustOptions);
    }
  }
}
