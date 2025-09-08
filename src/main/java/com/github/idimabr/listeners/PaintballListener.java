package com.github.idimabr.listeners;

import com.github.idimabr.controllers.EventController;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.events.PaintballEvent;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AllArgsConstructor
public class PaintballListener implements Listener {

    private final EventController controller;

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        final Player victim = (Player) e.getEntity();

        final CustomEvent customEvent = controller.getActualEvent();
        if (!(customEvent instanceof PaintballEvent)) return;

        final PaintballEvent paintball = (PaintballEvent) customEvent;
        if (!paintball.isRunning()) return;
        if (!paintball.getParticipants().contains(victim.getUniqueId())) {
            e.setCancelled(true);
            return;
        }

        if (e.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getDamager();
            if (!(arrow.getShooter() instanceof Player)) {
                e.setCancelled(true);
                return;
            }

            Player shooter = (Player) arrow.getShooter();
            if (!paintball.getParticipants().contains(shooter.getUniqueId())) {
                e.setCancelled(true);
                return;
            }

            if (paintball.isSameTeam(victim, shooter)) {
                e.setCancelled(true);
                return;
            }

            e.setDamage(0);
            paintball.handleElimination(victim);
        } else {
            e.setCancelled(true);
        }
    }
}
