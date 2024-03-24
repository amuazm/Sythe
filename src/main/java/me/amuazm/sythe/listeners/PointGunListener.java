package me.amuazm.sythe.listeners;

import me.amuazm.sythe.Point;
import me.amuazm.sythe.Sythe;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class PointGunListener implements Listener {
  private final Sythe sythe;
  private boolean active = false;
  private final double bounce = 0.9;
  private final double friction = 0.99;
  private final double gravity = -0.05;

  public PointGunListener(Sythe sythe) {
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

    if (!player.getEquipment().getItemInOffHand().getType().equals(Material.REDSTONE_TORCH)) {
      return;
    }

    event.setCancelled(true);

    if (active) {
      active = false;
      return;
    }

    final Location playerLocation = player.getEyeLocation();
    final Location bLoc;
    final Vector playerDirectionVector = playerLocation.getDirection();
    final RayTraceResult ray1;

    // Get ray
    ray1 = player.getWorld().rayTraceBlocks(playerLocation, playerDirectionVector, 100);
    if (ray1 == null || ray1.getHitBlock() == null) {
      // No ray feedback
      player.playSound(playerLocation, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
      return;
    }

    // Get location to anchor to
    bLoc = ray1.getHitBlock().getLocation();
    bLoc.add(0.5, 0.5, 0.5);

    // Rope
    active = !active;
    if (active) {
      // Points
      java.util.Vector<Point> points = new java.util.Vector<>();
      points.add(
          new Point(
              playerLocation,
              playerLocation
                  .clone()
                  .subtract(playerLocation.getDirection().normalize().multiply(0.4))));

      // Looped task
      Bukkit.getRegionScheduler()
          .runAtFixedRate(
              sythe,
              points.get(0).getCurrent(),
              scheduledTask -> {
                if (!active) {
                  // Code to run after reaching inactive
                  player.sendMessage("Ending.");

                  // Exit task
                  scheduledTask.cancel();
                  return;
                }

                // Code to run on loop while active
                World world = player.getWorld();
                // Update Points
                for (Point point : points) {
                  double vx = point.getCurrentX() - point.getOldX();
                  double vy = point.getCurrentY() - point.getOldY();
                  double vz = point.getCurrentZ() - point.getOldZ();
                  vx *= friction;
                  vy *= friction;
                  vz *= friction;
                  point.setOld(point.getCurrent().clone());
                  point.getCurrent().add(vx, vy, vz);
                  point.getCurrent().add(0, gravity, 0);

                  // Block collision detection
                  // point = new Point(
                  //         new Location(
                  //                 pWorld,
                  //                 794.54,
                  //                 9.89,
                  //                 974.85
                  //
                  //         ),
                  //         new Location(
                  //                 pWorld,
                  //                 794.66,
                  //                 10.01,
                  //                 974.72
                  //         )
                  // );

                  RayTraceResult ray2 =
                      world.rayTraceBlocks(
                          point.getOld(), point.getVector(), point.getVector().length());

                  if (ray2 != null
                      && ray2.getHitBlock() != null
                      && ray2.getHitBlockFace() != null) {
                    Block hitBlock = ray2.getHitBlock();
                    BoundingBox boundingBox = hitBlock.getBoundingBox();

                    Bukkit.broadcastMessage("\n" + hitBlock.getType());
                    Bukkit.broadcastMessage("" + ray2.getHitBlockFace());
                    Bukkit.broadcastMessage("" + point);

                    switch (ray2.getHitBlockFace()) {
                      case UP -> {
                        // Reflects a point (point.getCurrentY()) across a threshold (bb.getMaxY())
                        point.setCurrentY(
                            point.getCurrentY()
                                + 2 * (boundingBox.getMaxY() - point.getCurrentY()));
                        point.setOldY(point.getCurrentY() + vy * bounce);
                        // point.setOldY(point.getOldY() + (bb.getMaxY() - point.getOldY()) + 0.9 *
                        // (bb.getMaxY() - point.getOldY()));
                      }
                      case DOWN -> {
                        point.setCurrentY(
                            point.getCurrentY()
                                + 2 * (boundingBox.getMinY() - point.getCurrentY()));
                        point.setOldY(point.getCurrentY() + vy * bounce);
                      }
                      case WEST -> {
                        point.setCurrentX(
                            point.getCurrentX()
                                + 2 * (boundingBox.getMinX() - point.getCurrentX()));
                        point.setOldX(point.getCurrentX() + vx * bounce);
                      }
                      case NORTH -> {
                        point.setCurrentZ(
                            point.getCurrentZ()
                                + 2 * (boundingBox.getMinZ() - point.getCurrentZ()));
                        point.setOldZ(point.getCurrentZ() + vz * bounce);
                      }
                      case EAST -> {
                        point.setCurrentX(
                            point.getCurrentX()
                                + 2 * (boundingBox.getMaxX() - point.getCurrentX()));
                        point.setOldX(point.getCurrentX() + vx * bounce);
                      }
                      case SOUTH -> {
                        point.setCurrentZ(
                            point.getCurrentZ()
                                + 2 * (boundingBox.getMaxZ() - point.getCurrentZ()));
                        point.setOldZ(point.getCurrentZ() + vz * bounce);
                      }
                      default -> Bukkit.broadcastMessage("It hit none?");
                    }
                  }
                }

                // Particles / Render
                Particle.DustOptions dustOptions =
                    new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.0F);
                for (Point point : points) {
                  player.spawnParticle(Particle.REDSTONE, point.getCurrent(), 1, dustOptions);
                }
              },
              1,
              1);
    }
  }
}
