package com.github.idimabr.listeners;

import com.github.idimabr.controllers.EventController;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.events.PotatoEvent;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class PotatoListener implements Listener {

    private EventController controller;

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) return;

        Player damager = (Player) e.getDamager();
        Player target = (Player) e.getEntity();

        CustomEvent customEvent = controller.getActualEvent();
        if (!(customEvent instanceof PotatoEvent) || !customEvent.isRunning()) return;

        PotatoEvent event = (PotatoEvent) customEvent;
        if (event.isHotPotato(damager)) {
            e.setDamage(0);
            event.passPotato(damager, target);
        }
    }

    @EventHandler
    public void onArmorSlot(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        CustomEvent customEvent = controller.getActualEvent();
        if (customEvent == null || !customEvent.isRunning()) return;
        if (!customEvent.isEmptyInventory()) return;

        switch (e.getSlotType()) {
            case ARMOR:
                e.setCancelled(true);
                break;
            case QUICKBAR:
            case CONTAINER:
                if (e.getCurrentItem() != null && isArmorItem(e.getCurrentItem())) {
                    e.setCancelled(true);
                }
                break;
        }
    }


    private boolean isArmorItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;

        Material type = item.getType();
        return type.name().endsWith("_HELMET") ||
                type.name().endsWith("_CHESTPLATE") ||
                type.name().endsWith("_LEGGINGS") ||
                type.name().endsWith("_BOOTS");
    }
}
