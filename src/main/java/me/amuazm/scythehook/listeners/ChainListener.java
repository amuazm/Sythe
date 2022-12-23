package me.amuazm.scythehook.listeners;

import me.amuazm.scythehook.ScytheHook;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public class ChainListener implements Listener {
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

        Location pLoc = p.getEyeLocation();
        Vector v = pLoc.getDirection();
        Predicate<Entity> filter = entity -> (entity != p);
        RayTraceResult r = p.getWorld().rayTraceEntities(pLoc, v, 50, 2, filter);
        Entity entity = r.getHitEntity();

        // Code to run
        // Sound
        p.playSound(p.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1, 1);

        // Zoom
        Vector hookDir = p.getLocation().subtract(entity.getLocation()).toVector();
        hookDir.multiply(0.25);
        entity.setVelocity(hookDir);
        // TODO: Check how grapplinghook hooks in entities
    }
}
