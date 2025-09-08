package com.github.idimabr.factory;

import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.EventType;
import com.github.idimabr.models.events.*;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class EventFactory {

    public static CustomEvent createEvent(
            EventType type,
            String name,
            String id,
            boolean emptyInventory,
            int calls,
            int callTime,
            int time,
            int minPlayers,
            List<String> rewardCommands,
            Location lobbyLocation,
            Location joinLocation,
            Location leaveLocation,
            Location corner1,
            Location corner2,
            ItemStack[] kit,
            ItemStack[] kit2,
            Map<String, Object> data
    ) {
        switch (type) {
            case SIGN: return new SignEvent(name, id, type, emptyInventory, calls, callTime, time, minPlayers, rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, data);
            case PARKOUR: return new ParkourEvent(name, id, type, emptyInventory, calls, callTime, time,  minPlayers, rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, data);
            case POTATO: return new PotatoEvent(name, id, type, emptyInventory, calls, callTime, time,  minPlayers, rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, data);
            case TNTRUN: return new TNTRunEvent(name, id, type, emptyInventory, calls, callTime, time,  minPlayers, rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, data);
            case FASTCLICK: return new FastClickEvent(name, id, type, emptyInventory, calls, callTime, time,  minPlayers, rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, data);
            case MATH: return new MathEvent(name, id, type, emptyInventory, calls, callTime, time,  minPlayers, rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, data);
            case SPLEEF: return new SpleefEvent(name, id, type, emptyInventory, calls, callTime, time,  minPlayers, rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, data);
            case HIDE_AND_SEEK: return  new HiddenSeekEvent(name, id, type, emptyInventory, calls, callTime, time,  minPlayers, rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, kit2, data);
            case PAINTBALL: return  new PaintballEvent(name, id, type, emptyInventory, calls, callTime, time,  minPlayers, rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, kit2, data);
            default:
                throw new IllegalArgumentException("Tipo de evento desconhecido: " + type);
        }
    }
}
