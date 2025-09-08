package com.github.idimabr.listeners;

import com.github.idimabr.controllers.EventController;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.events.TNTRunEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class TNTListener implements Listener {

    private final EventController controller;

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();

        CustomEvent customEvent = controller.getActualEvent();
        if (!(customEvent instanceof TNTRunEvent) || !customEvent.isRunning()) return;
        if (!customEvent.isParticipant(player.getUniqueId())) return;

        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        final Player player = e.getPlayer();
        CustomEvent customEvent = controller.getActualEvent();
        if (!(customEvent instanceof TNTRunEvent) || !customEvent.isRunning()) return;
        if (!customEvent.isParticipant(player.getUniqueId())) return;

        TNTRunEvent tntRunEvent = (TNTRunEvent) customEvent;
        final Map<String, Object> data = tntRunEvent.getData();
        final int eliminateY = (int) data.get("eliminate-y");
        if (player.getLocation().getY() <= eliminateY) {
            tntRunEvent.removeParticipant(player.getUniqueId());
            player.teleport(tntRunEvent.getLeaveLocation());
            checkForWinner();
        }
    }

    private void checkForWinner() {
        CustomEvent customEvent = controller.getActualEvent();
        if (customEvent.getParticipants().size() == 1) {
            UUID winnerUUID = customEvent.getParticipants().get(0);
            Player winner = Bukkit.getPlayer(winnerUUID);
            if (winner != null && winner.isOnline()) {
                customEvent.setWinner(winner);
            }
        }
    }
}
