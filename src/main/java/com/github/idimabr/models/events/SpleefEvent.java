package com.github.idimabr.models.events;

import com.github.idimabr.MineEvents;
import com.github.idimabr.models.Cuboid;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.EventType;
import com.github.idimabr.utils.ConfigUtil;
import com.github.idimabr.utils.EventUtil;
import com.github.idimabr.utils.SchematicUtil;
import com.github.idimabr.utils.Task;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class SpleefEvent extends CustomEvent {

    private final ConfigurationSection section;

    private int counter = getTime();
    private int announceCalls = getCalls();

    private int breakAllowTime;
    private boolean allowBreak = false;

    private String schematic;


    public SpleefEvent(String name, String id, EventType type, boolean emptyInventory, int calls, int callTime, int time, int minPlayers, List<String> rewardCommands, Location lobbyLocation, Location joinLocation, Location leaveLocation, Location corner1, Location corner2, ItemStack[] kit, Map<String, Object> data) {
        super(name, id, type, emptyInventory, calls, callTime, time, minPlayers, rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, data);
        final ConfigUtil config = MineEvents.getPlugin().getConfig();
        this.section = config.getConfigurationSection("events." + id);
        this.breakAllowTime = (int) data.get("allow-break-time");
        this.schematic = getData().containsKey("schematic") ? (String) getData().get("schematic") : null;
    }

    @Override
    public EventType getType() {
        return EventType.SPLEEF;
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
                removeKit(other);
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
                        applyKit(player, getKit());
                    }
                }

                for (String string : section.getStringList("messages.start")) {
                    EventUtil.message(string.replace("{players}", getParticipants().size()+""));
                }

                Task.runRepeat(eventTask -> {
                    if (counter <= 0 || !isRunning()) {
                        eventTask.cancel();
                        stop();
                        return;
                    }

                    counter--;
                }, 20);

                Task.runRepeat(breakTask -> {
                    if (breakAllowTime <= 0) {
                        allowBreak = true;
                        for (String string : section.getStringList("messages.breaking")) {
                            EventUtil.message(string.replace("{players}", getParticipants().size() + "")
                                    .replace("{calls}", String.valueOf(breakAllowTime)));
                        }
                        breakTask.cancel();
                        return;
                    }

                    breakAllowTime--;
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
        MineEvents.getPlugin().getController().setActualEvent(null);
        for (UUID uuid : getParticipants()) {
            Player other = Bukkit.getPlayer(uuid);
            if (other != null && other.isOnline()) {
                removeKit(other);
                other.teleport(getLeaveLocation());
            }
        }

        if(schematic != null)
            SchematicUtil.paste(getJoinLocation(), schematic);
        setOpen(false);
        setRunning(false);
        setWinner(null);
        this.counter = getTime();
        this.announceCalls = getCalls();
        this.breakAllowTime = (int) getData().get("allow-break-time");
        this.allowBreak = false;
        getParticipants().clear();

        final Location corner1 = getCorner1();
        final Location corner2 = getCorner2();
        if(corner1 == null || corner2 == null || corner1.getWorld() == null || corner2.getWorld() == null) {
            return;
        }

        Cuboid cuboid = new Cuboid(corner1, corner2);
        for (Block block : cuboid.getBlocks()) {
            block.setType(Material.SNOW_BLOCK);
        }
    }
}
