package com.github.idimabr.listeners;

import com.github.idimabr.controllers.EventController;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.utils.ConfigUtil;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class EventListener implements Listener {

    private EventController controller;
    private ConfigUtil config;

    @EventHandler
    public void onSetup(PlayerInteractEvent e) {
        if (e.getHand() != null && e.getHand() != EquipmentSlot.HAND) return;
        final Player player = e.getPlayer();
        final Block block = e.getClickedBlock();
        if (block == null) return;

        final ItemStack hand = player.getItemInHand();
        if (hand == null) return;
        if (hand.getType() == Material.AIR) return;
        final ReadableNBT nbt = NBT.readNbt(hand);
        if (!nbt.hasTag("event_id")) return;

        e.setCancelled(true);
        final String eventID = nbt.getString("event_id");
        final CustomEvent event = controller.getEvent(eventID);
        if (event == null) {
            player.sendMessage("§cEvent not found.");
            return;
        }

        switch (e.getAction()) {
            case RIGHT_CLICK_BLOCK:
                event.setCorner2(block.getLocation());
                config.save();
                player.sendMessage("§aPosição 2 setada com sucesso!");
                break;
            case LEFT_CLICK_BLOCK:
                event.setCorner1(block.getLocation());
                config.save();
                player.sendMessage("§aPosição 1 setada com sucesso!");
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) return;

        final Player damager = (Player) e.getDamager();
        final Player target = (Player) e.getEntity();
        final CustomEvent customEvent = controller.getActualEvent();
        if(customEvent == null) return;
        if (!customEvent.isOpen()) {
            return;
        }

        boolean damagerIsParticipant = customEvent.isParticipant(damager.getUniqueId());
        boolean targetIsParticipant = customEvent.isParticipant(target.getUniqueId());
        if (!damagerIsParticipant || !targetIsParticipant) return;

        e.setCancelled(true);
    }
}
