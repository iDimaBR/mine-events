package com.github.idimabr.controllers;

import com.github.idimabr.factory.EventFactory;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.EventType;
import com.github.idimabr.utils.ConfigUtil;
import com.github.idimabr.utils.ItemUtil;
import com.github.idimabr.utils.LocationUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class EventController {

    private CustomEvent actualEvent;
    private Map<String, CustomEvent> events = new HashMap<>();

    public void load(ConfigUtil config){
        ConfigurationSection section = config.getConfigurationSection("events");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            ConfigurationSection eventSection = section.getConfigurationSection(id);
            if (eventSection == null) continue;

            String typeStr = eventSection.getString("type");
            if (typeStr == null) {
                Bukkit.getLogger().warning("[VitinEvents] Evento '" + id + "' não possui o campo 'type' definido!");
                continue;
            }
            EventType type;
            try {
                type = EventType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("[VitinEvents] Tipo de evento inválido: '" + typeStr + "' no evento '" + id + "'");
                continue;
            }
            String name = eventSection.getString("name");
            boolean emptyInventory = eventSection.getBoolean("empty-inventory", false);
            int calls = eventSection.getInt("calls");
            int callTime = eventSection.getInt("calls-time");
            int time = eventSection.getInt("time");
            int minPlayers = eventSection.getInt("min-players", 0);
            List<String> rewardCommands = eventSection.getStringList("rewards");
            Location lobbyLocation = LocationUtil.deserialize(eventSection.getString("locations.lobby"));
            Location joinLocation = LocationUtil.deserialize(eventSection.getString("locations.join"));
            Location leaveLocation = LocationUtil.deserialize(eventSection.getString("locations.leave"));
            Location corner1 = LocationUtil.deserialize(eventSection.getString("locations.corner1"));
            Location corner2 = LocationUtil.deserialize(eventSection.getString("locations.corner2"));
            ItemStack[] kit = ItemUtil.deserialize(null, eventSection.getString("kit"));
            ItemStack[] kit2 = ItemUtil.deserialize(null, eventSection.getString("kit2"));
            Map<String, Object> data = eventSection.getConfigurationSection("data") != null ? eventSection.getConfigurationSection("data").getValues(false) : new HashMap<>();
            CustomEvent event = EventFactory.createEvent(
                    type, name, id, emptyInventory, calls, callTime, time, minPlayers,rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, kit2, data
            );
            events.put(id, event);
            Bukkit.getLogger().info("[VitinEvents] Evento '" + id + "' carregado com sucesso! Tipo: " + type.name() + ", Nome: " + name);
        }
    }

    public CustomEvent getEvent(String id) {
        return events.get(id);
    }

    public boolean isChatEvent() {
        if(actualEvent == null) return false;
        switch(actualEvent.getType()) {
            case FASTCLICK:
            case MATH:
                return true;
            default:
                return false;
        }
    }

}
