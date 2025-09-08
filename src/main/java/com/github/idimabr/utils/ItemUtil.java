package com.github.idimabr.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class ItemUtil {

    public static final Gson GSON = new Gson();
    public static final Type LIST_TYPE = new TypeToken<List<String>>() {}.getType();

    public static String serialize(Player player) {
        try {
            // Agora 41 slots: 36 contents + 4 armor + 1 offhand
            ItemStack[] contents = new ItemStack[41];
            PlayerInventory inv = player.getInventory();

            System.arraycopy(inv.getContents(), 0, contents, 0, 36); // Itens principais
            System.arraycopy(inv.getArmorContents(), 0, contents, 36, 4); // Armaduras
            contents[40] = inv.getItemInOffHand(); // Mão secundária

            List<String> base64Items = new ArrayList<>();
            for (ItemStack item : contents) {
                base64Items.add(item != null ? serializeItem(item) : null);
            }

            String json = GSON.toJson(base64Items);
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ItemStack[] deserialize(Player player, String data) {
        if (data == null || data.isEmpty()) {
            return new ItemStack[0];
        }

        try {
            String json = new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
            List<String> base64Items = GSON.fromJson(json, LIST_TYPE);

            ItemStack[] contents = new ItemStack[41];
            for (int i = 0; i < base64Items.size() && i < contents.length; i++) {
                String entry = base64Items.get(i);
                contents[i] = (entry != null) ? deserializeItem(entry) : null;
            }

            if (player != null) {
                PlayerInventory inventory = player.getInventory();
                inventory.clear();

                ItemStack[] armor = Arrays.copyOfRange(contents, 36, 40);
                ItemStack offhand = contents[40];
                ItemStack[] mainContents = Arrays.copyOfRange(contents, 0, 36);

                inventory.setContents(mainContents);
                inventory.setArmorContents(armor);
                inventory.setItemInOffHand(offhand);
            }

            return contents;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ItemStack[0];
    }

    public static String serializeItem(ItemStack item) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             BukkitObjectOutputStream out = new BukkitObjectOutputStream(byteOut)) {
            out.writeObject(item);
            out.flush();
            return Base64.getEncoder().encodeToString(byteOut.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ItemStack deserializeItem(String base64) {
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
             BukkitObjectInputStream in = new BukkitObjectInputStream(byteIn)) {
            return (ItemStack) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
