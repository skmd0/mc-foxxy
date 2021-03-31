package com.skamlic.baseplugin;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.FoxWatcher;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;

import java.util.*;

public class  Parrot {
    private static Set<Parrot> parrots = new HashSet();
    private UUID parrot;
    private UUID owner;
    private boolean sitting;
    private Fox.Type variant;

    public Parrot(UUID parrot, UUID owner, Fox.Type variant) {
        this.parrot = parrot;
        this.owner = owner;
        this.variant = variant == null ? Fox.Type.values()[(new Random()).nextInt(Fox.Type.values().length)] : variant;
        this.disguise();
        parrots.add(this);
    }

    public static Set<Parrot> getParrots() {
        return parrots;
    }

    public static Parrot fromParrot(Player player) {
        Iterator var1 = parrots.iterator();

        Parrot parrot;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            parrot = (Parrot)var1.next();
        } while(!parrot.getParrot().equals(player));

        return parrot;
    }

    public static Parrot getParrot(Player player) {
        Iterator var1 = parrots.iterator();

        Parrot parrot;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            parrot = (Parrot)var1.next();
        } while(!parrot.getParrot().equals(player) && !parrot.getOwner().equals(player));

        return parrot;
    }

    public static boolean isParrot(Player player) {
        Iterator var1 = parrots.iterator();

        Parrot parrot;
        do {
            if (!var1.hasNext()) {
                return false;
            }

            parrot = (Parrot)var1.next();
        } while(!parrot.getParrot().equals(player));

        return true;
    }

    public Fox.Type getVariant() {
        return this.variant;
    }

    public Player getParrot() {
        return Bukkit.getPlayer(this.parrot);
    }

    public Player getOwner() {
        return Bukkit.getPlayer(this.owner);
    }

    public boolean isSitting() {
        return this.sitting;
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    public void disguise() {
        this.getParrot().setFoodLevel(20);
        this.getParrot().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(12.0D);
        this.getParrot().setHealth(this.getParrot().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        this.getParrot().setAllowFlight(false);
        this.getParrot().setInvulnerable(false);
        this.getParrot().setWalkSpeed(0.4f);
        this.libsDisguise();
    }

    public void libsDisguise() {
        MobDisguise disguise = new MobDisguise(DisguiseType.FOX);
        String playerName = this.getParrot().getName().trim();
        disguise.setMultiName(playerName);
        disguise.setKeepDisguiseOnPlayerDeath(true);
        disguise.setViewSelfDisguise(true);
        disguise.setNotifyBar(DisguiseConfig.NotifyBar.NONE);
        FoxWatcher watcher = (FoxWatcher)disguise.getWatcher();
        watcher.setType(this.variant);
        disguise.setEntity(this.getParrot());
        disguise.startDisguise();
    }

    public void libsUndisguise() {
        Disguise disguise = DisguiseAPI.getDisguise(this.getParrot());
        if (disguise != null) {
            disguise.stopDisguise();
        }
    }

    public void undisguise() {
        this.getParrot().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0D);
        this.getParrot().setHealth(20.0D);
        this.getParrot().setInvulnerable(false);
        this.getParrot().setAllowFlight(false);
        this.getParrot().setWalkSpeed(0.2f);
        this.libsUndisguise();
    }

    public void remove() {
        this.undisguise();
        parrots.remove(this);
    }
}