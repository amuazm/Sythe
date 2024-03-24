package me.amuazm.sythe.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class ShieldListener implements Listener {
  @EventHandler
  public void onHitByShield(EntityDamageByEntityEvent e) {
    if (!(e.getEntity() instanceof LivingEntity entity)) {
      return;
    }
    if (!(e.getDamager() instanceof LivingEntity damager)) {
      return;
    }
    if (damager.getEquipment() == null) {
      return;
    }
    if (!(damager.getEquipment().getItemInMainHand().getType().equals(Material.SHIELD))) {
      return;
    }

    // Potion
    entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 0));
    entity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60, 0));
    entity.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0));

    // Sound
    damager.getWorld().playSound(damager, Sound.ITEM_SHIELD_BLOCK, 1, 1);
  }

  @EventHandler
  public void onAttackOnShield(EntityDamageByEntityEvent e) {
    if (!(e.getEntity() instanceof HumanEntity human)) {
      return;
    }
    if (!(e.getDamager() instanceof LivingEntity damager)) {
      return;
    }
    if (human.getEquipment() == null) {
      return;
    }
    if (!(human.getEquipment().getItemInMainHand().getType().equals(Material.SHIELD))) {
      return;
    }
    if (!human.isBlocking()) {
      return;
    }

    final Location hLoc = human.getLocation();
    final Location dLoc = damager.getLocation();

    // Check if damage was blocked by shield
    final Vector attackDirection = dLoc.clone().subtract(hLoc).toVector();
    final Vector humanDirection = hLoc.getDirection();
    double angle = humanDirection.angle(attackDirection);
    if (angle > Math.PI / 2) {
      return;
    }

    // Potion
    human.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60, 0));

    // Sound
    human.getWorld().playSound(human, Sound.ENTITY_ENDER_DRAGON_HURT, 1, 1);

    // Knockback
    final Vector knockback = dLoc.clone().subtract(hLoc).toVector();
    knockback.normalize();
    knockback.multiply(1);
    damager.setVelocity(knockback);
  }
}
