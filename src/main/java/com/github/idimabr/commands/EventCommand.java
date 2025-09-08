package com.github.idimabr.commands;

import com.github.idimabr.controllers.EventController;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.utils.EventUtil;
import lombok.AllArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class EventCommand implements CommandExecutor, TabCompleter {

    private EventController controller;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(!(sender instanceof Player)){
            sender.sendMessage("§cThis command can only be executed by a player.");
            return false;
        }

        final Player player = (Player) sender;
        final CustomEvent actualEvent = controller.getActualEvent();
        if(actualEvent == null){
            sender.sendMessage("§cNão há eventos acontecendo.");
            return false;
        }

        if(actualEvent.isRunning()){
            sender.sendMessage("§cO evento ja está acontecendo.");
            return false;
        }

        if(args.length == 0) {
            if(controller.isChatEvent()){
                sender.sendMessage("§cNão há eventos acontecendo.");
                return false;
            }

            if (actualEvent.isParticipant(player.getUniqueId())) {
                sender.sendMessage("§cVocê ja está participando!");
                return false;
            }

            if (actualEvent.isEmptyInventory() && !EventUtil.isInventoryEmpty(player)) {
                sender.sendMessage("§cVocê precisa estar com inventário vazio!");
                return false;
            }

            actualEvent.addParticipant(player.getUniqueId());
            player.teleport(actualEvent.getLobbyLocation());
            player.sendMessage("§aVocê entrou no evento.");
            return true;
        }

        if(args.length == 1 && args[0].equalsIgnoreCase("leave")){
            if (!actualEvent.isParticipant(player.getUniqueId())) {
                sender.sendMessage("§cVocê não está participando!");
                return false;
            }

            actualEvent.removeParticipant(player.getUniqueId());
            player.teleport(actualEvent.getLeaveLocation());
            player.sendMessage("§cVocê saiu do evento.");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if(args.length == 0) return Collections.singletonList("leave");
        return Collections.emptyList();
    }
}
