package com.github.idimabr.listeners;

import com.github.idimabr.controllers.EventController;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.events.FastClickEvent;
import com.github.idimabr.models.events.MathEvent;
import com.github.idimabr.utils.Task;
import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@AllArgsConstructor
public class ChatListener implements Listener {

    private final EventController controller;

    @EventHandler
    public void onMath(AsyncPlayerChatEvent event) {
        CustomEvent customEvent = controller.getActualEvent();
        if (!(customEvent instanceof MathEvent) || !customEvent.isRunning()) return;

        final MathEvent chatEvent = (MathEvent) customEvent;
        if (chatEvent.isCorrect(event.getMessage())) {
            event.setCancelled(true);
            Task.run(() -> customEvent.setWinner(event.getPlayer()));
        }
    }

    @EventHandler
    public void onFastclick(PlayerCommandPreprocessEvent event) {
        CustomEvent customEvent = controller.getActualEvent();
        if (!(customEvent instanceof FastClickEvent) || !customEvent.isRunning()) return;

        final FastClickEvent chatEvent = (FastClickEvent) customEvent;
        final String message = event.getMessage();
        if(!message.contains(" ")) return;
        if(!message.startsWith("/eventocheck")) return;
        event.setCancelled(true);

        chatEvent.checkArgument(event.getPlayer(), message.split(" "));
    }
}
