package com.github.idimabr.models.events;

import com.github.idimabr.VitinEvents;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.EventType;
import com.github.idimabr.utils.ConfigUtil;
import com.github.idimabr.utils.EventUtil;
import com.github.idimabr.utils.Task;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ParkourEvent extends CustomEvent {

    private final ConfigurationSection section;

    private int counter = getTime();

    private int announceCalls = getCalls();

    public ParkourEvent(String name, String id, EventType type, boolean emptyInventory, int calls, int callTime, int time, int minPlayers, List<String> rewardCommands, Location lobbyLocation, Location joinLocation, Location leaveLocation, Location corner1, Location corner2, ItemStack[] kit, Map<String, Object> data) {
        super(name, id, type, emptyInventory, calls, callTime, time, minPlayers, rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, data);
        final ConfigUtil config = VitinEvents.getPlugin().getConfig();
        this.section = config.getConfigurationSection("events." + id);
    }

    @Override
    public EventType getType() {
        return EventType.PARKOUR;
    }

    @Override
    public void setWinner(Player player) {
        if(player == null) return;
        super.setWinner(player);

        for (String string : section.getStringList("messages.winner")) {
            string = string.replace("{player}", player.getName());
            EventUtil.message(string);
        }

        for (String command : getRewardCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
        }
        for (UUID uuid : getParticipants()) {
            Player other = Bukkit.getPlayer(uuid);
            if (other != null && other.isOnline()) {
                other.teleport(getLeaveLocation());
            }
        }
        reset();
    }

    @Override
    public void start() {
        setOpen(true);
        setRunning(false);

        Task.runRepeat(task -> {
            if (announceCalls <= 0) {
                setOpen(false);
                setRunning(true);

                for (UUID uuid : getParticipants()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        player.teleport(getJoinLocation());
                    }
                }

                for (String string : section.getStringList("messages.start")) {
                    EventUtil.message(string.replace("{players}", getParticipants().size()+""));
                }

                Task.runRepeat(eventTask -> {
                    if (counter <= 0 || !isRunning()) {
                        stop();
                        eventTask.cancel();
                        return;
                    }

                    counter--;
                }, 20);

                task.cancel();
                return;
            }

            for (String string : section.getStringList("messages.broadcast")) {
                EventUtil.message(string.replace("{players}", getParticipants().size()+"").replace("{calls}", String.valueOf(announceCalls)));
            }

            announceCalls--;
        }, getCallTime() * 20);
    }

    @Override
    public void stop() {
        if(getWinner() == null) {
            for (String string : section.getStringList("messages.no-winner")) {
                EventUtil.message(string);
            }
        }
        reset();
    }

    public void reset(){
        VitinEvents.getPlugin().getController().setActualEvent(null);
        for (UUID uuid : getParticipants()) {
            Player other = Bukkit.getPlayer(uuid);
            if (other != null && other.isOnline()) {
                other.teleport(getLeaveLocation());
            }
        }


        setOpen(false);
        setRunning(false);
        setWinner(null);
        this.counter = getTime();
        this.announceCalls = getCalls();
        getParticipants().clear();
    }
}
