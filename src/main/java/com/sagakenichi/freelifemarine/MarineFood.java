package com.sagakenichi.freelifemarine;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public final class MarineFood {

    private final NamespacedKey key;

    public MarineFood(JavaPlugin plugin) {
        this.key = new NamespacedKey(plugin, "marine_food");
    }

    public ItemStack create(int amount) {
        ItemStack stack = new ItemStack(Material.COD, Math.max(1, Math.min(64, amount)));
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "海の餌");
        meta.setLore(List.of(
                ChatColor.GRAY + "海洋生物を引き寄せる特別な餌",
                ChatColor.DARK_GRAY + "手に持つか、水中へ投げ入れて使います"
        ));
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        stack.setItemMeta(meta);
        return stack;
    }

    public boolean isMarineFood(ItemStack stack) {
        if (stack == null || stack.getType().isAir() || !stack.hasItemMeta()) {
            return false;
        }
        Byte value = stack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        return value != null && value == (byte) 1;
    }

    public boolean isHeldBy(Player player) {
        return isMarineFood(player.getInventory().getItemInMainHand())
                || isMarineFood(player.getInventory().getItemInOffHand());
    }

    public boolean give(Player player, int amount) {
        ItemStack stack = create(amount);
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(stack);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
        return true;
    }

    public boolean consumeOne(Player player) {
        if (consumeOne(player, EquipmentSlot.HAND)) {
            return true;
        }
        return consumeOne(player, EquipmentSlot.OFF_HAND);
    }

    private boolean consumeOne(Player player, EquipmentSlot slot) {
        ItemStack stack = slot == EquipmentSlot.HAND
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();
        if (!isMarineFood(stack)) {
            return false;
        }
        if (stack.getAmount() <= 1) {
            stack = new ItemStack(Material.AIR);
        } else {
            stack.setAmount(stack.getAmount() - 1);
        }
        if (slot == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(stack);
        } else {
            player.getInventory().setItemInOffHand(stack);
        }
        return true;
    }

    public boolean consumeOne(Item item) {
        if (item == null || !item.isValid()) {
            return false;
        }
        ItemStack stack = item.getItemStack();
        if (!isMarineFood(stack)) {
            return false;
        }
        if (stack.getAmount() <= 1) {
            item.remove();
        } else {
            stack.setAmount(stack.getAmount() - 1);
            item.setItemStack(stack);
        }
        return true;
    }
}
