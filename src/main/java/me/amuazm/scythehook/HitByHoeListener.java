package me.amuazm.scythehook;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class HitByHoeListener implements Listener {
    @EventHandler
    public void onHitByHoe(EntityDamageByEntityEvent e) {
        Entity toHook = e.getEntity();
        LivingEntity hookedBy = (LivingEntity) e.getDamager();
        World world = toHook.getWorld();
        // Hoe checker
        if (hookedBy.getEquipment().getItemInMainHand().getType().equals(Material.IRON_HOE)) {
            // Damage multiplier
            double hookedBySpeed = toHook.getVelocity().length();
            double dmgOrg = e.getDamage();
            double dmgMod = dmgOrg * (hookedBySpeed / 0.0784);
            e.setDamage(dmgMod);
            // Sound
            hookedBy.getWorld().playSound(hookedBy, Sound.BLOCK_NOTE_BLOCK_BELL, (float) dmgMod, (float) dmgMod);
            // Debug
            String s = "";
            s += "\nOriginal Damage = " + dmgOrg;
            s += "\nSpeed = " + hookedBySpeed;
            s += "\nMultiplied = " + dmgMod;
            Bukkit.broadcastMessage(s);

            Location toHookLoc = toHook.getLocation();
            Location hookedByLoc = hookedBy.getLocation();

            Vector hookDir = hookedByLoc.subtract(toHookLoc).toVector();
            hookDir.normalize();
            hookDir.multiply(3);

            toHook.setVelocity(hookDir);
        }
    }
}
