package me.amuazm.scythehook;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class HitByHoeListener implements Listener {
    @EventHandler
    public void onHitByHoe(EntityDamageByEntityEvent e) {
        Entity toHook = e.getEntity();
        LivingEntity hookedBy = (LivingEntity) e.getDamager();
        if (hookedBy.getEquipment() == null) {
            return;
        }
        // Hoe checker
        if (hookedBy.getEquipment().getItemInMainHand().getType().equals(Material.IRON_HOE)) {
            World world = toHook.getWorld();
            Location toHookLoc = toHook.getLocation();
            Location hookedByLoc = hookedBy.getLocation();

            // Damage multiplier
            double hookedBySpeed = toHook.getVelocity().length();
            double dmgOrg = e.getDamage();
            double dmgMod = dmgOrg * (hookedBySpeed / 0.0784);
            e.setDamage(dmgMod);
            // Debug
            String s = "";
            s += "\nOriginal Damage = " + dmgOrg;
            s += "\nSpeed = " + hookedBySpeed;
            s += "\nMultiplied = " + dmgMod;
            Bukkit.broadcastMessage(s);

            // Sound
            hookedBy.getWorld().playSound(hookedBy, Sound.BLOCK_NOTE_BLOCK_BELL, (float) dmgMod, (float) dmgMod);

            // Particles
            ItemStack itemCrackData = new ItemStack(Material.REDSTONE_BLOCK);
            world.spawnParticle(Particle.ITEM_CRACK, toHookLoc, (int) dmgMod * 10, itemCrackData);

            // Hook Velocity
            Vector hookDir = hookedByLoc.subtract(toHookLoc).toVector();
            hookDir.normalize();
            hookDir.multiply(3);
            toHook.setVelocity(hookDir);
        }
    }
}
