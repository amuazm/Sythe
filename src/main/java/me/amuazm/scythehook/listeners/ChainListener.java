package me.amuazm.scythehook.listeners;

import me.amuazm.scythehook.ScytheHook;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ChainListener implements Listener {
    private final ScytheHook plugin = ScytheHook.getPlugin(ScytheHook.class);

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getHand() == null) {
            return;
        }
        if (!e.getHand().equals(EquipmentSlot.OFF_HAND)) {
            return;
        }
        if (p.getEquipment() == null) {
            return;
        }
        if (!p.getEquipment().getItemInOffHand().getType().equals(Material.CHAIN)) {
            return;
        }
        e.setCancelled(true);

        Set<Material> transparent = new HashSet<Material>();
        transparent.add(Material.AIR);
        transparent.add(Material.GRASS);
        transparent.add(Material.TALL_GRASS);
        transparent.add(Material.WATER);
        int range = 50;
        eSearch:
        for (Block b : p.getLineOfSight(transparent, range)) {
            for (Entity entity : p.getNearbyEntities(range, range, range)) {
                Location bLoc = b.getLocation();
                Location eLoc = entity.getLocation();
                double bX = bLoc.getX();
                double bY = bLoc.getY();
                double bZ = bLoc.getZ();
                double eX = Math.floor(eLoc.getX());
                double eY = Math.floor(eLoc.getY());
                double eZ = Math.floor(eLoc.getZ());
                // l/L - Leniency
                int l = 2;
                for (int iX = -l; iX <= l; iX++) {
                    double lX = eX + iX;
                    for (int iY = -l; iY <= l; iY++) {
                        double lY = eY + iY;
                        for (int iZ = -l; iZ <= l; iZ++) {
                            double lZ = eZ + iZ;
                            if (bX == lX && bY == lY && bZ == lZ) {
                                // Code to run where mob in sight = entity
                                // Sound
                                p.playSound(p.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1, 1);

                                // Zoom
                                Vector hookDir = p.getLocation().subtract(entity.getLocation()).toVector();
//                        hookDir.normalize();
                                hookDir.multiply(0.3);
                                // Y Limiter
//                        hookDir.setY(Math.min(hookDir.getY(), 1.0));
                                // Velocity
                                entity.setVelocity(hookDir);

//                        Bukkit.broadcastMessage("");
//                        Bukkit.broadcastMessage("" + bX + " : " + eX + " : " + (bX == eX));
//                        Bukkit.broadcastMessage("" + bY + " : " + eY + " : " + (bY == eY));
//                        Bukkit.broadcastMessage("" + bZ + " : " + eZ + " : " + (bZ == eZ));
//                        Bukkit.broadcastMessage(entity.getName());
                                break eSearch;
                            }
                        }
                    }
                }
            }
        }
    }
}
