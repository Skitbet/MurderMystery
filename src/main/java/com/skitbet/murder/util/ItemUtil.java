package com.skitbet.murder.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class ItemUtil {

    public static ItemStack getMurderWeapon() {
        ItemStack itemStack = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = itemStack.getItemMeta();

        meta.setDisplayName("§cMurderer's Knife");
        meta.setUnbreakable(true);

        ArrayList<String> lore = new ArrayList<>();
        lore.add("§r ");
        lore.add("§c§lLEFT CLICK &eto kill someone.");
        lore.add("§c§lRIGHT CLICK &eto throw the weapon.");
        lore.add("§r ");

        meta.setLore(lore);

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack getDetectiveBow() {
        ItemStack itemStack = new ItemStack(Material.BOW);
        ItemMeta meta = itemStack.getItemMeta();

        meta.setDisplayName("§aDetectives Gun");
        meta.setUnbreakable(true);

        ArrayList<String> lore = new ArrayList<>();
        lore.add("§r ");
        lore.add("§eFind and shoot the murderer.");
        lore.add("§r ");

        meta.setLore(lore);
        meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        itemStack.setItemMeta(meta);
        return itemStack;
    }

}
