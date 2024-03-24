package me.amuazm.sythe.listeners;

import me.amuazm.sythe.Sythe;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class StringListener implements Listener {
  private final Sythe sythe;

  public StringListener(Sythe sythe) {
    this.sythe = sythe;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    final Player player = event.getPlayer();

    if (event.getHand() == null) {
      return;
    }

    if (!event.getHand().equals(EquipmentSlot.OFF_HAND)) {
      return;
    }

    if (!player.getEquipment().getItemInOffHand().getType().equals(Material.STRING)) {
      return;
    }

    event.setCancelled(true);

    // TODO: Cooldown

    final Location playerEyeLocation = player.getEyeLocation();
    final Vector playerDirectionVector = playerEyeLocation.getDirection();
    final RayTraceResult ray;
    final Location locationHook;

    // Hook to blocks
    // Get ray
    ray = player.getWorld().rayTraceBlocks(playerEyeLocation, playerDirectionVector, 100);

    if (ray == null || ray.getHitBlock() == null) {
      // No ray feedback
      player.playSound(playerEyeLocation, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
      return;
    }

    // Get location to hook to
    locationHook = ray.getHitBlock().getLocation();
    locationHook.add(0.5, 0.5, 0.5);

    // Zoom
    pullEntityToLocation(player, locationHook, 1.0);

    // Sound
    player.playSound(playerEyeLocation, Sound.ENTITY_MAGMA_CUBE_JUMP, 1, 1);

    // Particles
    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 255, 255), 1.0F);
    double x = playerEyeLocation.getX();
    double y = playerEyeLocation.getY();
    double z = playerEyeLocation.getZ();
    double distance = playerEyeLocation.distance(locationHook);
    double x_interval = (locationHook.getX() - x) / distance;
    double y_interval = (locationHook.getY() - y) / distance;
    double z_interval = (locationHook.getZ() - z) / distance;
    double particles_per_block = 2;

    for (double i = 0; i <= distance; i += (1 / particles_per_block)) {
      player.spawnParticle(
          Particle.REDSTONE,
          x + x_interval * i,
          y + y_interval * i,
          z + z_interval * i,
          1,
          dustOptions);
    }

    // TODO: durability
  }

  private void pullEntityToLocation(final LivingEntity entity, Location loc, double multiply) {
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

              Vector v = new Vector();
              v.setX(v_x);
              v.setY(v_y);
              v.setZ(v_z);
              v.multiply(multiply);

              Vector playerDirection = entity.getEyeLocation().getDirection();

              double magnitude = v.length();
              v.normalize();
              playerDirection.normalize();
              v = v.add(playerDirection);
              v.multiply(magnitude);

              entity.setVelocity(v);
            },
            null,
            5L);
  }
}
