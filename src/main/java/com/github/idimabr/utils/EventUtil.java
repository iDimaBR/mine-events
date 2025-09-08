package com.github.idimabr.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.awt.*;

public class EventUtil {

    public static void message(String message){
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message.replace("&", "§"));
        }
    }

    public static void sendComponent(BaseComponent... components) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(components);
        }
    }

    public static boolean isInventoryEmpty(Player player) {
        PlayerInventory inv = player.getInventory();
        boolean empty = true;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                empty = false;
                break;
            }
        }

        for (ItemStack armor : inv.getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR) {
                empty = false;
                break;
            }
        }

        ItemStack offhand = inv.getItemInOffHand();
        if (offhand != null && offhand.getType() != Material.AIR) {
            empty = false;
        }
        return empty;
    }
}
