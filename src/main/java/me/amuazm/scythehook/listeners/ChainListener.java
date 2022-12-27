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
    ScytheHook plugin = ScytheHook.getPlugin(ScytheHook.class);

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

        final Location pLoc = p.getEyeLocation();
        final Vector v = pLoc.getDirection();
        final Predicate<Entity> filter = entity -> (entity != p);
        final RayTraceResult r = p.getWorld().rayTraceEntities(pLoc, v, 50, 2, filter);
        if (r == null) {
            return;
        }
        if (r.getHitEntity() == null) {
            return;
        }
        Entity entity = r.getHitEntity();

        // Code to run
        // Sound
        p.playSound(p.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1, 1);

        // Zoom
        pullEntityToLocation(entity, p.getLocation(), 1.0);
    }

    //better method for pulling COPIED FROM GRAPPLING HOOK
    private void pullEntityToLocation(final Entity entity, Location loc, double multiply){
        Location entityLoc = entity.getLocation();

        Vector boost = entity.getVelocity();
        boost.setY(0.3);
        entity.setVelocity(boost);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            double g = -0.08;
            double d = loc.distance(entityLoc);
            double t = d;
            // speed = distance / time
            // distance = (100 + 7 * blockstotravel)% * axis distance
            // time = blockstotravel
            double v_x = (1.0+0.07*t) * (loc.getX()-entityLoc.getX()) / t;
            double v_y = (1.0+0.03*t) * (loc.getY()-entityLoc.getY()) / t - 0.5*g*t;
            double v_z = (1.0+0.07*t) * (loc.getZ()-entityLoc.getZ()) / t;

            Vector v = entity.getVelocity();
            v.setX(v_x);
            v.setY(v_y);
            v.setZ(v_z);
            v.multiply(multiply);
            entity.setVelocity(v);
        }, 1L);
    }
}
