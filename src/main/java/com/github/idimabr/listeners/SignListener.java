package com.github.idimabr.listeners;

import com.github.idimabr.controllers.EventController;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.events.FastClickEvent;
import com.github.idimabr.models.events.MathEvent;
import com.github.idimabr.models.events.ParkourEvent;
import com.github.idimabr.models.events.SignEvent;
import com.github.idimabr.utils.Task;
import lombok.AllArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@AllArgsConstructor
public class SignListener implements Listener {

    private final EventController controller;

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        CustomEvent customEvent = controller.getActualEvent();
        if (!(customEvent instanceof SignEvent) || !customEvent.isRunning()) return;

        final Block block = e.getClickedBlock();
        if (block == null) return;
        if (!block.getType().name().contains("SIGN")) return;

        Sign sign = (Sign) block.getState();
        if (!sign.getLine(0).equalsIgnoreCase("§c[Evento]")) return;
        if (!sign.getLine(1).equalsIgnoreCase(customEvent.getName())) return;

        final Player player = e.getPlayer();
        if (!customEvent.isParticipant(player.getUniqueId())) return;

        customEvent.setWinner(player);
        e.setCancelled(true);
    }

    @EventHandler
    public void onChat(SignChangeEvent e) {
        if (!e.getLine(0).equalsIgnoreCase("[Evento]")) return;
        final Player player = e.getPlayer();
        if (!player.hasPermission("vitinevents.admin")) {
            e.setCancelled(true);
            player.sendMessage("§cVocê não tem permissão para criar placas de eventos.");
            return;
        }

        String eventName = e.getLine(1);
        CustomEvent event = controller.getEvent(eventName);
        if (event == null) {
            e.setCancelled(true);
            player.sendMessage("§cEvento " + eventName + " não encontrado.");
            return;
        }

        e.setLine(0, "§c[Evento]");
        e.setLine(1, event.getName());
    }
}
