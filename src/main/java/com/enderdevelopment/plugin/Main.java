package com.enderdevelopment.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin implements Listener {
    private static final int MAX_ENERGY = 100;
    private final Map<UUID, Integer> energy = new HashMap<>();
    private final Map<UUID, String> techniques = new HashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, Long> domainOwners = new HashMap<>();
    private BukkitTask energyTask;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("cursed").setExecutor(new CursedCommand(this));
        energyTask = Bukkit.getScheduler().runTaskTimer(this, this::regenerateEnergy, 20L, 20L);
        getLogger().info("CursedTechniques enabled.");
    }

    @Override
    public void onDisable() {
        if (energyTask != null) energyTask.cancel();
        domainOwners.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        energy.putIfAbsent(player.getUniqueId(), MAX_ENERGY);
        techniques.putIfAbsent(player.getUniqueId(), "Limitless");
        player.sendMessage(Component.text("Cursed energy awakened. Use /cursed help.", NamedTextColor.DARK_PURPLE));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        domainOwners.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (player.hasPotionEffect(PotionEffectType.STRENGTH)) {
            event.setDamage(event.getDamage() + 2.0);
        }
    }

    private void regenerateEnergy() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            int current = energy.getOrDefault(player.getUniqueId(), MAX_ENERGY);
            energy.put(player.getUniqueId(), Math.min(MAX_ENERGY, current + 3));
            if (domainOwners.containsKey(player.getUniqueId()) && domainOwners.get(player.getUniqueId()) <= System.currentTimeMillis()) {
                domainOwners.remove(player.getUniqueId());
                player.sendMessage(Component.text("Your domain fades.", NamedTextColor.GRAY));
            }
        }
    }

    public int getEnergy(Player player) { return energy.getOrDefault(player.getUniqueId(), MAX_ENERGY); }
    public String getTechnique(Player player) { return techniques.getOrDefault(player.getUniqueId(), "Limitless"); }
    public void setTechnique(Player player, String technique) { techniques.put(player.getUniqueId(), technique); }

    public boolean spend(Player player, int amount) {
        int current = getEnergy(player);
        if (current < amount) {
            player.sendMessage(Component.text("You need " + amount + " cursed energy. Current: " + current, NamedTextColor.RED));
            return false;
        }
        energy.put(player.getUniqueId(), current - amount);
        return true;
    }

    public boolean ready(Player player, String ability, long cooldownMillis) {
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(player.getUniqueId(), ignored -> new HashMap<>());
        long remaining = playerCooldowns.getOrDefault(ability, 0L) - System.currentTimeMillis();
        if (remaining > 0) {
            player.sendMessage(Component.text(ability + " is on cooldown for " + ((remaining + 999) / 1000) + "s.", NamedTextColor.RED));
            return false;
        }
        playerCooldowns.put(ability, System.currentTimeMillis() + cooldownMillis);
        return true;
    }

    public void blackFlash(Player player) {
        if (!ready(player, "Black Flash", 8000) || !spend(player, 25)) return;
        LivingEntity target = target(player, 6);
        if (target == null) {
            player.sendMessage(Component.text("Your fist finds only empty air.", NamedTextColor.GRAY));
            return;
        }
        target.damage(12.0, player);
        target.setVelocity(player.getLocation().getDirection().multiply(1.3).setY(0.5));
        burst(target.getLocation(), Particle.DUST_PLUME, 30, 0.5);
        player.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.5f);
        player.sendMessage(Component.text("BLACK FLASH!", NamedTextColor.DARK_RED));
    }

    public void limitless(Player player) {
        if (!ready(player, "Limitless", 5000) || !spend(player, 18)) return;
        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection().normalize();
        for (int i = 1; i <= 12; i++) {
            Location point = origin.clone().add(direction.clone().multiply(i));
            burst(point, Particle.END_ROD, 4, 0.12);
            for (Entity entity : point.getWorld().getNearbyEntities(point, 0.8, 0.8, 0.8)) {
                if (entity instanceof LivingEntity living && entity != player) {
                    living.damage(3.5, player);
                    living.setVelocity(direction.clone().multiply(0.8));
                }
            }
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.8f);
    }

    public void cleave(Player player) {
        if (!ready(player, "Cleave", 4500) || !spend(player, 20)) return;
        Location center = player.getLocation();
        for (Entity entity : center.getWorld().getNearbyEntities(center, 4, 2.5, 4)) {
            if (entity instanceof LivingEntity living && entity != player) {
                living.damage(9.0, player);
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
            }
        }
        burst(center, Particle.SWEEP_ATTACK, 20, 2.2);
        player.getWorld().playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.6f);
    }

    public void reverseCursedTechnique(Player player) {
        if (!ready(player, "Reverse Cursed Technique", 12000) || !spend(player, 30)) return;
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        player.setHealth(Math.min(maxHealth, player.getHealth() + 10));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
        burst(player.getLocation(), Particle.HEART, 12, 0.8);
        player.sendMessage(Component.text("Positive energy restores your body.", NamedTextColor.LIGHT_PURPLE));
    }

    public void domain(Player player) {
        if (!ready(player, "Domain Expansion", 60000) || !spend(player, 75)) return;
        domainOwners.put(player.getUniqueId(), System.currentTimeMillis() + 15000);
        Location center = player.getLocation();
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 300, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 1));
        for (Entity entity : center.getWorld().getNearbyEntities(center, 10, 6, 10)) {
            if (entity instanceof LivingEntity living && entity != player) {
                living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 300, 3));
                living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 300, 1));
            }
        }
        burst(center, Particle.REVERSE_PORTAL, 80, 5);
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1f, 0.5f);
        player.sendMessage(Component.text("Domain Expansion: Malevolent Shrine", NamedTextColor.DARK_RED));
    }

    private LivingEntity target(Player player, double range) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        for (int i = 1; i <= range; i++) {
            for (Entity entity : eye.getWorld().getNearbyEntities(eye.clone().add(direction.clone().multiply(i)), 1, 1, 1)) {
                if (entity instanceof LivingEntity living && entity != player) return living;
            }
        }
        return null;
    }

    private void burst(Location location, Particle particle, int count, double spread) {
        location.getWorld().spawnParticle(particle, location, count, spread, spread, spread, 0.03);
    }

    public ItemStack cursedTalisman() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Cursed Talisman", NamedTextColor.DARK_PURPLE));
        meta.lore(List.of(Component.text("A focus for cursed techniques.", NamedTextColor.GRAY)));
        item.setItemMeta(meta);
        return item;
    }
}
