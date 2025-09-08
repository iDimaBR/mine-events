package com.github.idimabr.commands;

import com.github.idimabr.controllers.EventController;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.utils.ConfigUtil;
import com.github.idimabr.utils.ItemBuilder;
import com.github.idimabr.utils.ItemUtil;
import com.github.idimabr.utils.LocationUtil;
import de.tr7zw.changeme.nbtapi.NBT;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class AdminCommand implements CommandExecutor, TabCompleter {

    private EventController controller;
    private ConfigUtil config;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage("§cThis command can only be executed by a player.");
            return false;
        }

        final Player player = (Player) sender;
        if(!player.hasPermission("vitinevents.admin")){
            player.sendMessage("§cSem permissão para isso.");
            return false;
        }

        if(args.length == 0){
            player.sendMessage("§f= COMANDOS =");
            player.sendMessage("§c");
            player.sendMessage("§b/aevento start <event> §f- Inicia um evento.");
            player.sendMessage("§b/aevento stop <event> §f- Para um evento.");
            player.sendMessage("§b/aevento list §f- Lista todos os eventos.");
            player.sendMessage("§b/aevento setlobby <event> §f- Define o lobby do evento.");
            player.sendMessage("§b/aevento setspawn <event> §f- Define o spawn do evento.");
            player.sendMessage("§b/aevento setleave <event> §f- Define a localização de saída do evento.");
            player.sendMessage("§b/aevento setkit <event> §f- Define o kit do evento.");
            player.sendMessage("§b/aevento setkit2 <event> §f- Define o kit 2 do evento.");
            player.sendMessage("§b/aevento wand <event> §f- Recebe uma ferramenta de seleção para o evento.");
            player.sendMessage("§c/aevento setblue <event> §f- Define a localização do time azul do paintball.");
            player.sendMessage("§c/aevento setred <event> §f- Define a localização de time vermelho do paintball.");
            player.sendMessage("§c/aevento sethider <event> §f- Define a localização do time do hidden&seek.");
            player.sendMessage("§c/aevento setseeker <event> §f- Define a localização de time do hidden&seek.");

            return false;
        }

        if(args[0].equalsIgnoreCase("start")){
            if(args.length < 2){
                player.sendMessage("§cUtilize: /aevento start <event>");
                return false;
            }
            String eventName = args[1];
            final CustomEvent event = controller.getEvent(eventName);
            if(event == null){
                player.sendMessage("§cEvento " + eventName + " não encontrado.");
                return false;
            }

            if(!event.isSetupValid()){
                player.sendMessage("§cO Evento " + eventName + " não está configurado corretamente.");
                return false;
            }

            if(event.equals(controller.getActualEvent())){
                player.sendMessage("§cO Evento " + eventName + " ja está acontecendo.");
                return false;
            }

            if(event.isRunning()){
                player.sendMessage("§cO Evento " + eventName + " ja está acontecendo.");
                return false;
            }

            controller.setActualEvent(event);
            event.start();
            player.sendMessage("§aEvento " + eventName + " iniciado.");
            return true;
        }

        if(args[0].equalsIgnoreCase("stop")){
            if(args.length < 2){
                player.sendMessage("§cUtilize: /aevento stop <event>");
                return false;
            }
            String eventName = args[1];
            final CustomEvent event = controller.getEvent(eventName);
            if(event == null){
                player.sendMessage("§cEvento " + eventName + " não encontrado.");
                return false;
            }

            if(!event.isRunning()){
                player.sendMessage("§cO Evento " + eventName + " não está acontecendo.");
                return false;
            }

            event.setRunning(false);
            controller.setActualEvent(null);
            player.sendMessage("§aEvento " + eventName + " parado.");
            return true;
        }

        if(args[0].equalsIgnoreCase("list")){
            player.sendMessage("§aEventos:" + controller.getEvents().values().stream().map(CustomEvent::getName).collect(Collectors.joining(", ")));
            return true;
        }

        if(args[0].equalsIgnoreCase("setspawn")){
            if(args.length < 2){
                player.sendMessage("§cUtilize: /aevento setspawn <event>");
                return false;
            }
            String eventName = args[1];
            final CustomEvent event = controller.getEvent(eventName);
            if(event == null){
                player.sendMessage("§cEvento " + eventName + " não encontrado.");
                return false;
            }

            final String id = event.getId();
            event.setJoinLocation(player.getLocation());
            config.set("events." + id + ".locations.join", LocationUtil.serialize(player.getLocation()));
            config.save();
            player.sendMessage("§aLocalização do spawn para " + eventName + " definido.");
            return true;
        }

        if(args[0].equalsIgnoreCase("setlobby")){
            if(args.length < 2){
                player.sendMessage("§cUtilize: /aevento setlobby <event>");
                return false;
            }
            String eventName = args[1];
            final CustomEvent event = controller.getEvent(eventName);
            if(event == null){
                player.sendMessage("§cEvento " + eventName + " não encontrado.");
                return false;
            }

            final String id = event.getId();
            event.setLobbyLocation(player.getLocation());
            config.set("events." + id + ".locations.lobby", LocationUtil.serialize(player.getLocation()));
            config.save();
            player.sendMessage("§aLocalização do lobby para " + eventName + " definido.");
            return true;
        }

        if(args[0].equalsIgnoreCase("setleave")){
            if(args.length < 2){
                player.sendMessage("§cUtilize: /aevento setleave <event>");
                return false;
            }
            String eventName = args[1];
            final CustomEvent event = controller.getEvent(eventName);
            if(event == null){
                player.sendMessage("§cEvento " + eventName + " não encontrado.");
                return false;
            }

            final String id = event.getId();
            event.setLeaveLocation(player.getLocation());
            config.set("events." + id + ".locations.leave", LocationUtil.serialize(player.getLocation()));
            config.save();
            player.sendMessage("§aLocalização de saida para " + eventName + " definido.");
            return true;
        }

        if(args[0].equalsIgnoreCase("setblue")){
            if(args.length < 2){
                player.sendMessage("§cUtilize: /aevento setblue <event>");
                return false;
            }
            String eventName = args[1];
            final CustomEvent event = controller.getEvent(eventName);
            if(event == null){
                player.sendMessage("§cEvento " + eventName + " não encontrado.");
                return false;
            }

            final String id = event.getId();
            event.setLeaveLocation(player.getLocation());
            config.set("events." + id + ".data.blue-team", LocationUtil.serialize(player.getLocation()));
            config.save();
            player.sendMessage("§aLocalização do time azul para " + eventName + " definido.");
            return true;
        }

        if(args[0].equalsIgnoreCase("setred")){
            if(args.length < 2){
                player.sendMessage("§cUtilize: /aevento setred <event>");
                return false;
            }
            String eventName = args[1];
            final CustomEvent event = controller.getEvent(eventName);
            if(event == null){
                player.sendMessage("§cEvento " + eventName + " não encontrado.");
                return false;
            }

            final String id = event.getId();
            event.setLeaveLocation(player.getLocation());
            config.set("events." + id + ".data.red-team", LocationUtil.serialize(player.getLocation()));
            config.save();
            player.sendMessage("§aLocalização do time vermelho para " + eventName + " definido.");
            return true;
        }

        if(args[0].equalsIgnoreCase("sethider")){
            if(args.length < 2){
                player.sendMessage("§cUtilize: /aevento sethider <event>");
                return false;
            }
            String eventName = args[1];
            final CustomEvent event = controller.getEvent(eventName);
            if(event == null){
                player.sendMessage("§cEvento " + eventName + " não encontrado.");
                return false;
            }

            final String id = event.getId();
            event.setLeaveLocation(player.getLocation());
            config.set("events." + id + ".data.hider", LocationUtil.serialize(player.getLocation()));
            config.save();
            player.sendMessage("§aLocalização de time para " + eventName + " definido.");
            return true;
        }

        if(args[0].equalsIgnoreCase("setseeker")){
            if(args.length < 2){
                player.sendMessage("§cUtilize: /aevento setseeker <event>");
                return false;
            }
            String eventName = args[1];
            final CustomEvent event = controller.getEvent(eventName);
            if(event == null){
                player.sendMessage("§cEvento " + eventName + " não encontrado.");
                return false;
            }

            final String id = event.getId();
            event.setLeaveLocation(player.getLocation());
            config.set("events." + id + ".data.seeker", LocationUtil.serialize(player.getLocation()));
            config.save();
            player.sendMessage("§aLocalização de time para " + eventName + " definido.");
            return true;
        }

        if(args[0].equalsIgnoreCase("setkit")){
            if(args.length < 2){
                player.sendMessage("§cUtilize: /aevento setkit <event>");
                return false;
            }
            String eventName = args[1];
            final CustomEvent event = controller.getEvent(eventName);
            if(event == null){
                player.sendMessage("§cEvento " + eventName + " não encontrado.");
                return false;
            }

            final String id = event.getId();
            String serialize = ItemUtil.serialize(player);
            config.set("events." + id + ".kit", serialize);
            config.save();
            event.setKit(ItemUtil.deserialize(null, serialize));
            player.sendMessage("§aKit do evento " + eventName + " definido.");
            return true;
        }

        if(args[0].equalsIgnoreCase("setkit2")){
            if(args.length < 2){
                player.sendMessage("§cUtilize: /aevento setkit <event>");
                return false;
            }
            String eventName = args[1];
            final CustomEvent event = controller.getEvent(eventName);
            if(event == null){
                player.sendMessage("§cEvento " + eventName + " não encontrado.");
                return false;
            }

            final String id = event.getId();
            String serialize = ItemUtil.serialize(player);
            config.set("events." + id + ".kit2", serialize);
            config.save();
            event.setKit(ItemUtil.deserialize(null, serialize));
            player.sendMessage("§aKit 2 do evento " + eventName + " definido.");
            return true;
        }

        if(args[0].equalsIgnoreCase("wand")){
            if(args.length < 2){
                player.sendMessage("§cUtilize: /aevento wand <event>");
                return false;
            }
            String eventName = args[1];
            final CustomEvent event = controller.getEvent(eventName);
            if(event == null){
                player.sendMessage("§cEvento " + eventName + " não encontrado.");
                return false;
            }

            final String id = event.getId();
            ItemStack builder = new ItemBuilder(Material.BLAZE_ROD).setName("§aFerramenta de seleção (" + id + ")").build();
            NBT.modify(builder, nbt -> {
                nbt.setString("event_id", id);
            });
            player.getInventory().addItem(builder);
            player.sendMessage("§aFerramenta de seleção do evento " + id + " recebida.");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vitinevents.admin")) return Collections.emptyList();

        final List<String> SUBCOMMANDS = Arrays.asList(
                "start", "stop", "list", "setspawn", "setlobby", "setleave",
                "setblue", "setred", "sethider", "setseeker", "setkit", "setkit2", "wand"
        );

        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && SUBCOMMANDS.contains(args[0].toLowerCase())) {
            return controller.getEvents().values().stream()
                    .map(CustomEvent::getId)
                    .filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
