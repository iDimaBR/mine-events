package com.github.idimabr.tasks;

import com.github.idimabr.controllers.EventController;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.events.TNTRunEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class TNTTask extends BukkitRunnable {

    private final EventController controller;

    private final Map<UUID, Location> lastStepPositions = new HashMap<>();
    private final Map<UUID, Long> lastRemoveTime = new HashMap<>();
    private static final long REMOVE_COOLDOWN_MS = 200L;
    private static double[] OFFSETS = { -0.3, 0.3 };

    @Override
    public void run() {
        CustomEvent customEvent = controller.getActualEvent();
        if (!(customEvent instanceof TNTRunEvent) || !customEvent.isRunning()) return;

        long now = System.currentTimeMillis();
        for (UUID uuid : new ArrayList<>(customEvent.getParticipants())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline() || !player.isOnGround()) continue;

            if (now - lastRemoveTime.getOrDefault(uuid, 0L) < REMOVE_COOLDOWN_MS) continue;
            boolean isMoving = player.getVelocity().lengthSquared() > 0.001;
            Location currentLoc = player.getLocation().clone();
            if (isMoving) {
                Location lastLoc = lastStepPositions.get(uuid);
                if (lastLoc != null) {
                    removeBelow(lastLoc);
                }
                lastStepPositions.put(uuid, currentLoc.clone());
            } else {
                for (double dx : OFFSETS) {
                    for (double dz : OFFSETS) {
                        Location offsetLoc = currentLoc.clone().add(dx, 0, dz);
                        removeBelow(offsetLoc);
                    }
                }
            }

            lastRemoveTime.put(uuid, now);
        }
    }

    private boolean isRemovable(Material material) {
        switch (material) {
            case TNT:
            case SAND:
            case GRAVEL:
                return true;
            default:
                return false;
        }
    }

    private void removeBelow(Location location) {
        Block block1 = location.clone().subtract(0, 1, 0).getBlock();
        Block block2 = location.clone().subtract(0, 2, 0).getBlock();

        if (block1.getType().isSolid() && isRemovable(block1.getType())) {
            block1.setType(Material.AIR);
        }
        if (block2.getType().isSolid() && isRemovable(block2.getType())) {
            block2.setType(Material.AIR);
        }
    }
}
