package com.github.idimabr.listeners;

import com.github.idimabr.controllers.EventController;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.events.ParkourEvent;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@AllArgsConstructor
public class ParkourListener implements Listener {

    private final EventController controller;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        final Player player = event.getPlayer();
        CustomEvent customEvent = controller.getActualEvent();
        if (!(customEvent instanceof ParkourEvent) || !customEvent.isRunning()) return;
        if (!customEvent.isParticipant(player.getUniqueId())) return;
        if (customEvent.getWinner() != null) return;
        if (!customEvent.betweenCorners(player)) return;

        customEvent.setWinner(player);
    }
}
