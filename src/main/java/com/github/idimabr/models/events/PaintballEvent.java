package com.github.idimabr.models.events;

import com.github.idimabr.VitinEvents;
import com.github.idimabr.models.Cuboid;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.EventType;
import com.github.idimabr.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PaintballEvent extends CustomEvent {

    private final ConfigurationSection section;

    private int counter = getTime();
    private int announceCalls = getCalls();

    private List<UUID> redTeam = new ArrayList<>();
    private List<UUID> blueTeam = new ArrayList<>();

    private Location blueLocation;
    private Location redLocation;
    private String winnerTeam;

    public PaintballEvent(String name, String id, EventType type, boolean emptyInventory, int calls, int callTime, int time, int minPlayers, List<String> rewardCommands, Location lobbyLocation, Location joinLocation, Location leaveLocation, Location corner1, Location corner2, ItemStack[] kit, ItemStack[] kit2, Map<String, Object> data) {
        super(name, id, type, emptyInventory, calls, callTime, time, minPlayers, rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, data);
        setKit2(kit2);
        final ConfigUtil config = VitinEvents.getPlugin().getConfig();
        this.section = config.getConfigurationSection("events." + id);
        this.blueLocation = LocationUtil.deserialize((String) data.get("blue-team"));
        this.redLocation = LocationUtil.deserialize((String) data.get("red-team"));
    }

    @Override
    public EventType getType() {
        return EventType.PAINTBALL;
    }

    @Override
    public void setWinner(Player player) {
        if(player == null || winnerTeam == null) return;
        super.setWinner(player);

        for (String string : section.getStringList("messages.winner")) {
            string = string.replace("{team}", winnerTeam);
            EventUtil.message(string);
        }

        for (String command : getRewardCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{team}", winnerTeam));
        }
        for (UUID uuid : getParticipants()) {
            Player other = Bukkit.getPlayer(uuid);
            if (other != null && other.isOnline()) {
                other.teleport(getLeaveLocation());
                removeKit(player);
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

                List<UUID> participants = new ArrayList<>(getParticipants());
                int half = participants.size() / 2;

                for (int i = 0; i < participants.size(); i++) {
                    final UUID uuid = participants.get(i);
                    final Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline()) continue;

                    if (i < half) {
                        redTeam.add(uuid);
                        player.teleport(redLocation);
                        applyKit(player, getKit());
                        player.sendMessage("§cVocê está no time vermelho!");
                    } else {
                        blueTeam.add(uuid);
                        player.teleport(blueLocation);
                        applyKit(player, getKit2());
                        player.sendMessage("§9Você está no time azul!");
                    }
                }

                for (String string : section.getStringList("messages.start")) {
                    EventUtil.message(string.replace("{players}", getParticipants().size() + ""));
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
                EventUtil.message(string.replace("{players}", getParticipants().size() + "").replace("{calls}", String.valueOf(announceCalls)));
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
        this.redTeam.clear();
        this.blueTeam.clear();
        this.winnerTeam = null;
    }

    public boolean isSameTeam(Player p1, Player p2) {
        return (redTeam.contains(p1.getUniqueId()) && redTeam.contains(p2.getUniqueId())) ||
                (blueTeam.contains(p1.getUniqueId()) && blueTeam.contains(p2.getUniqueId()));
    }

    public void handleElimination(Player eliminated) {
        final UUID uuid = eliminated.getUniqueId();
        redTeam.remove(uuid);
        blueTeam.remove(uuid);
        getParticipants().remove(uuid);

        removeKit(eliminated);
        eliminated.sendMessage("§cVocê foi eliminado!");
        eliminated.teleport(getLeaveLocation());

        if (redTeam.isEmpty() && !blueTeam.isEmpty()) {
            winnerTeam = "Azul";
            setWinner(Bukkit.getPlayer(blueTeam.get(0)));
        } else if (blueTeam.isEmpty() && !redTeam.isEmpty()) {
            winnerTeam = "Vermelho";
            setWinner(Bukkit.getPlayer(redTeam.get(0)));
        } else if (blueTeam.isEmpty() && redTeam.isEmpty()) {
            stop();
        }
    }
}
