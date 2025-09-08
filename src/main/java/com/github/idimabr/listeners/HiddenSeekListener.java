package com.github.idimabr.listeners;

import com.github.idimabr.controllers.EventController;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.events.HiddenSeekEvent;
import com.github.idimabr.utils.Task;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

@AllArgsConstructor
public class HiddenSeekListener implements Listener {

    private final EventController controller;

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;

        final Player damager = (Player) event.getDamager();
        final Player target = (Player) event.getEntity();
        final CustomEvent customEvent = controller.getActualEvent();
        if (!(customEvent instanceof HiddenSeekEvent) ) return;
        if (!customEvent.isRunning()) return;
        final HiddenSeekEvent game = (HiddenSeekEvent) customEvent;
        if (!game.isSameTeam(damager, target)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();

        final CustomEvent customEvent = controller.getActualEvent();
        if (!(customEvent instanceof HiddenSeekEvent)) return;

        final HiddenSeekEvent game = (HiddenSeekEvent) customEvent;
        if (!game.isRunning()) return;

        Task.runLater(() -> {
            player.spigot().respawn();
            if (game.isHider(player)) {
                game.handleElimination(player);
            }
        }, 2);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();

        final CustomEvent customEvent = controller.getActualEvent();
        if (!(customEvent instanceof HiddenSeekEvent)) return;

        final HiddenSeekEvent game = (HiddenSeekEvent) customEvent;
        if (!game.isRunning()) return;
        if (game.isSeeker(player)) {
            event.setRespawnLocation(game.getSeekerLocation());
        } else if (game.isHider(player)) {
            event.setRespawnLocation(game.getHiderLocation());
        }

        Task.runLater(() -> {
            if (game.isSeeker(player)) {
                game.applyKit(player, game.getKit());
            }
        }, 2);
    }

}
