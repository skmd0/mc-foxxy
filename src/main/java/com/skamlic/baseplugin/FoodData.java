package com.skamlic.baseplugin;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class FoodData {
    private static Map<Material, FoodData> map = new HashMap();
    private int health;

    public FoodData(int health) {
        this.health = health;
    }

    public static FoodData get(Material material) {
        return (FoodData)map.getOrDefault(material, null);
    }

    public int getHealth() {
        return this.health;
    }

    static {
        map.put(Material.SWEET_BERRIES, new FoodData(1));
    }
}
