package com.github.idimabr.listeners;

import com.github.idimabr.controllers.EventController;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.events.SpleefEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.UUID;

@RequiredArgsConstructor
public class SpleefListener implements Listener {

    private final EventController controller;

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        if(e.isCancelled()) return;

        final Player player = e.getPlayer();
        CustomEvent customEvent = controller.getActualEvent();
        if (!(customEvent instanceof SpleefEvent) || !customEvent.isRunning()) return;
        if (!customEvent.isParticipant(player.getUniqueId())) return;

        final SpleefEvent spleefEvent = (SpleefEvent) customEvent;
        if(!spleefEvent.isAllowBreak()) {
            player.playSound(player.getLocation(), Sound.BLOCK_SNOW_BREAK, 1, 1);
            e.setCancelled(true);
            return;
        }

        final Block block = e.getClickedBlock();
        if(block == null) return;

        if(block.getType() != Material.SNOW_BLOCK) return;
        block.setType(Material.AIR);
        e.setCancelled(true);
    }


    @EventHandler
    public void onBreakSpleef(BlockBreakEvent e){
        if(e.isCancelled()) return;

        final Player player = e.getPlayer();
        CustomEvent customEvent = controller.getActualEvent();
        if (!(customEvent instanceof SpleefEvent)) return;
        if (!customEvent.isParticipant(player.getUniqueId())) return;

        final SpleefEvent spleefEvent = (SpleefEvent) customEvent;
        if(spleefEvent.isAllowBreak()) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        final Player player = e.getPlayer();
        CustomEvent customEvent = controller.getActualEvent();
        if (!(customEvent instanceof SpleefEvent) || !customEvent.isRunning()) return;
        if (!customEvent.isParticipant(player.getUniqueId())) return;

        SpleefEvent tntRunEvent = (SpleefEvent) customEvent;
        final Block block = player.getLocation().getBlock();
        if (block.getType().name().contains("WATER")) {
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
