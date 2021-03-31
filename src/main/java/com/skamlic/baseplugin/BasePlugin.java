package com.skamlic.baseplugin;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.watchers.ParrotWatcher;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

public class BasePlugin extends JavaPlugin implements Listener {
    private static BasePlugin instance;

    public BasePlugin() {
    }

    public static BasePlugin getInstance() {
        return instance;
    }

    public void onEnable() {
        instance = this;
        this.getServer().getPluginManager().registerEvents(this, this);
        Iterator var1 = Bukkit.getOnlinePlayers().iterator();

        while(var1.hasNext()) {
            Player player1 = (Player)var1.next();
            Iterator var3 = Bukkit.getOnlinePlayers().iterator();

            while(var3.hasNext()) {
                Player player2 = (Player)var3.next();
                if (!player1.equals(player2) && player1.canSee(player2)) {
                    player1.hidePlayer(getInstance(), player2);
                    player1.showPlayer(getInstance(), player2);
                }
            }
        }

        (new BukkitRunnable() {
            public void run() {
                Iterator var1 = Parrot.getParrots().iterator();

                while(var1.hasNext()) {
                    Parrot fox = (Parrot)var1.next();
                    Player parrotPlayer = fox.getParrot();
                    if (!parrotPlayer.isOnGround()) {
                        parrotPlayer.setGliding(false);
                    }

                    if (parrotPlayer.getLocation().add(0.0D, 1.0D, 0.0D).getBlock().getType().isSolid()) {
                        parrotPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2147483647, 7, true, false, false));
                    } else {
                        parrotPlayer.removePotionEffect(PotionEffectType.SPEED);
                    }
                }

            }
        }).runTaskTimer(this, 0L, 1L);
    }

    public void onDisable() {
        Iterator var1 = Parrot.getParrots().iterator();

        while(var1.hasNext()) {
            Parrot fox = (Parrot)var1.next();
            fox.remove();
        }

        instance = null;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("fox")) {
            Player parrotPlayer;
            if (args.length != 3 && args.length != 4) {
                if (args.length == 2) {
                    parrotPlayer = Bukkit.getPlayer(args[1]);
                    if (parrotPlayer == null) {
                        sender.sendMessage(ChatColor.RED + "Player " + args[1] + " is not online.");
                        return false;
                    }

                    if (args[0].equalsIgnoreCase("remove")) {
                        Parrot fox = Parrot.getParrot(parrotPlayer);
                        if (fox != null) {
                            sender.sendMessage(ChatColor.GREEN + fox.getParrot().getName() + " is no longer a fox!");
                            fox.remove();
                        } else {
                            sender.sendMessage(ChatColor.RED + parrotPlayer.getName() + " was not a fox or owner!");
                        }

                        return true;
                    }
                }
            } else {
                parrotPlayer = Bukkit.getPlayer(args[1]);
                Player owner = Bukkit.getPlayer(args[2]);
                if (parrotPlayer == null) {
                    sender.sendMessage(ChatColor.RED + "Player " + args[1] + " is not online.");
                    return false;
                }

                if (owner == null) {
                    sender.sendMessage(ChatColor.RED + "Player " + args[2] + " is not online.");
                    return false;
                }

                if (args[0].equalsIgnoreCase("add")) {
                    if (Parrot.isParrot(parrotPlayer)) {
                        sender.sendMessage(ChatColor.RED + parrotPlayer.getName() + " is already a fox!");
                        return false;
                    }

                    if (Parrot.isParrot(owner)) {
                        sender.sendMessage(ChatColor.RED + owner.getName() + " is already a fox owner!");
                        return false;
                    }

                    Parrot fox;
                    try {
                        Fox.Type variant = org.bukkit.entity.Fox.Type.valueOf(args[3].toUpperCase());
                        fox = new Parrot(parrotPlayer.getUniqueId(), owner.getUniqueId(), variant);
                    } catch (IllegalArgumentException var9) {
                        sender.sendMessage(ChatColor.RED + args[3] + " is not a valid fox color. Please use one of [" + this.getFoxColors() + "].");
                        return true;
                    } catch (IndexOutOfBoundsException var10) {
                        fox = new Parrot(parrotPlayer.getUniqueId(), owner.getUniqueId(), (org.bukkit.entity.Fox.Type)null);
                    }

                    sender.sendMessage(ChatColor.GREEN + parrotPlayer.getName() + " is now a fox!");
                    return true;
                }
            }
            sender.sendMessage(ChatColor.RED + "Invalid usage. Please use:");
            sender.sendMessage(ChatColor.RED + "/fox set <fox> <owner> [optional: color (" + this.getFoxColors() + ")]");
            sender.sendMessage(ChatColor.RED + "/fox remove <player>");
        }

        return false;
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (Parrot.isParrot(event.getPlayer())) {
            event.getPlayer().sendMessage(ChatColor.RED + "You are a fox, you cannot break blocks!");
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (Parrot.isParrot(event.getPlayer())) {
            event.getPlayer().sendMessage(ChatColor.RED + "You are a fox, you cannot place blocks!");
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        if (Parrot.isParrot(event.getPlayer())) {
            event.getPlayer().sendMessage(ChatColor.RED + "You are a fox, you eat by yourself!");
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Parrot fox = Parrot.getParrot(event.getPlayer());
        if (fox != null) {
            fox.remove();
        }

    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (event.getHand() == EquipmentSlot.HAND) {
            if (event.getRightClicked() instanceof Player) {
                Player parrotPlayer = (Player)event.getRightClicked();
                Parrot fox = Parrot.fromParrot(parrotPlayer);
                if (fox == null) {
                    return;
                }

                Player player = event.getPlayer();
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                Material type = itemStack.getType();
                FoodData foodData = FoodData.get(type);
                if (foodData != null) {
                    if (player.getGameMode() != GameMode.CREATIVE) {
                        if (itemStack.getAmount() == 1) {
                            player.getInventory().setItemInMainHand((ItemStack)null);
                        } else {
                            itemStack.setAmount(itemStack.getAmount() - 1);
                        }
                    }

                    parrotPlayer.getWorld().playSound(parrotPlayer.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.0F, 1.0F);
                    parrotPlayer.getWorld().spawnParticle(Particle.HEART, parrotPlayer.getLocation(), 5, 0.5D, 0.25D, 0.5D);
                    parrotPlayer.setHealth(Math.min(parrotPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), parrotPlayer.getHealth() + (double)foodData.getHealth()));
                } else {
                    Disguise disguise = DisguiseAPI.getDisguise(parrotPlayer);
                    ParrotWatcher watcher = (ParrotWatcher)disguise.getWatcher();
                    watcher.setSitting(false);
                    if (fox.isSitting()) {
                        watcher.setSitting(false);
                        fox.setSitting(false);
                    } else {
                        watcher.setSitting(true);
                        fox.setSitting(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SHOULDER_ENTITY && event.getEntity().hasMetadata("fakeParrot")) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onEntityRegainHealthEvent(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player && Parrot.isParrot((Player)event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player && Parrot.isParrot((Player)event.getEntity())) {
            Player player = (Player)event.getEntity();
            boolean cancel = false;
            int firstSlot = this.findFirstSlot(player);
            if (firstSlot != -1) {
                ItemStack itemStack = player.getInventory().getItem(firstSlot);
                ItemStack pickedUp = event.getItem().getItemStack();
                if (itemStack.getType() != pickedUp.getType() || itemStack.getType().getMaxStackSize() < itemStack.getAmount() + pickedUp.getAmount()) {
                    cancel = true;
                }
            }

            if (cancel) {
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player && Parrot.isParrot((Player)event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
        final Parrot fox = Parrot.fromParrot(event.getPlayer());
        if (fox != null) {
            (new BukkitRunnable() {
                public void run() {
                    fox.libsUndisguise();
                    fox.libsDisguise();
                }
            }).runTaskLater(this, 2L);
        }

    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() <= event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ()) {
            Parrot fox = Parrot.getParrot(event.getPlayer());
            if (fox != null) {
                if (fox.getParrot().equals(event.getPlayer()) && fox.isSitting()) {
                    Location clone = event.getFrom().clone();
                    clone.setYaw(event.getTo().getYaw());
                    clone.setPitch(event.getTo().getPitch());
                    event.setTo(clone);
                    return;
                }

                if (!fox.getParrot().getWorld().equals(fox.getOwner().getWorld()) || fox.getParrot().getLocation().distanceSquared(fox.getOwner().getLocation()) > 22500.0D) {
                    fox.getParrot().teleport(fox.getOwner());
                }
            }
        }
    }

    @EventHandler
    public void onEntityToggleGlideEvent(EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player)event.getEntity();
            if (player.isSneaking()) {
                event.setCancelled(true);
            }

            if (!player.isGliding() && player.hasPotionEffect(PotionEffectType.SPEED) && player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() == 7) {
                player.removePotionEffect(PotionEffectType.SPEED);
            }
        }

    }

    @EventHandler
    public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent event) {

        Player player = event.getPlayer();
        Parrot fox = Parrot.fromParrot(player);
        if (fox != null && !fox.isSitting()) {
            if (event.isSneaking()) {
                player.setGliding(true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 2147483647, 7, true, false, false));
            } else {
                player.setGliding(false);
                player.removePotionEffect(PotionEffectType.SPEED);
            }

            Disguise disguise = DisguiseAPI.getDisguise(player);
            if (disguise != null) {
                ParrotWatcher watcher = (ParrotWatcher)disguise.getWatcher();
                watcher.setSitting(event.isSneaking());
            }
        }
    }

    @EventHandler
    public void onEntityDismountEvent(EntityDismountEvent event) {
//        if (event.getDismounted() instanceof Player && event.getEntity() instanceof Player) {
//            Player player = (Player)event.getEntity();
//            Parrot fox = Parrot.fromParrot(player);
//            if (fox != null && fox.isShoulder()) {
//                if (player.isSneaking()) {
//                    fox.unshoulder();
//                } else {
//                    Location clone = fox.getParrot().getLocation().clone();
//
//                    for(int x = -3; x <= 3; ++x) {
//                        for(int y = -3; y <= 3; ++y) {
//                            for(int z = -3; z <= 3; ++z) {
//                                Material type = clone.clone().add((double)x, (double)y, (double)z).getBlock().getType();
//                                if (type == Material.LAVA || type == Material.WATER) {
//                                    event.setCancelled(true);
//                                    return;
//                                }
//                            }
//                        }
//                    }
//
//                }
//            }
//        }
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        final Parrot fox = Parrot.fromParrot(player);
        if (fox != null) {
            fox.undisguise();
            (new BukkitRunnable() {
                public void run() {
                    fox.disguise();
                }
            }).runTaskLater(this, 2L);
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Parrot fox = Parrot.fromParrot(player);
        if (fox != null) {
            fox.setSitting(false);
        }
    }

//    @EventHandler
//    public void onEntityDamageEvent(EntityDamageEvent event) {
//        if (event.getEntity() instanceof Player) {
//            Parrot fox = Parrot.fromParrot((Player)event.getEntity());
//            if (fox == null) {
//                return;
//            }
//
//            event.setCancelled(true);
//        }
//    }

    private int findFirstSlot(Player player) {
        for(int i = 0; i < player.getInventory().getSize(); ++i) {
            if (player.getInventory().getItem(i) != null) {
                return i;
            }
        }

        return -1;
    }

    private String getFoxColors() {
        return (String)Arrays.stream(org.bukkit.entity.Fox.Type.values()).map((variant) -> {
            return variant.toString().toLowerCase();
        }).collect(Collectors.joining(", "));
    }
}