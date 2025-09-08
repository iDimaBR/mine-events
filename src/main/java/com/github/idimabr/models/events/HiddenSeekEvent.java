package com.github.idimabr.models.events;

import com.github.idimabr.VitinEvents;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.EventType;
import com.github.idimabr.utils.ConfigUtil;
import com.github.idimabr.utils.EventUtil;
import com.github.idimabr.utils.LocationUtil;
import com.github.idimabr.utils.Task;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.*;
import java.util.stream.Collectors;

public class HiddenSeekEvent extends CustomEvent {

    private final ConfigurationSection section;

    private int counter = getTime();
    private int announceCalls = getCalls();

    private List<UUID> seekers = new ArrayList<>();
    private List<UUID> hidders = new ArrayList<>();

    @Getter
    private Location seekerLocation;
    @Getter
    private Location hiderLocation;
    private String winnerTeam;
    private String seekerName;
    private String hiderName;

    public HiddenSeekEvent(String name, String id, EventType type, boolean emptyInventory, int calls, int callTime, int time, int minPlayers, List<String> rewardCommands, Location lobbyLocation, Location joinLocation, Location leaveLocation, Location corner1, Location corner2, ItemStack[] kit, ItemStack[] kit2, Map<String, Object> data) {
        super(name, id, type, emptyInventory, calls, callTime, time, minPlayers, rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, data);
        setKit2(kit2);
        final ConfigUtil config = VitinEvents.getPlugin().getConfig();
        this.section = config.getConfigurationSection("events." + id);
        this.seekerLocation = LocationUtil.deserialize((String) data.get("seeker"));
        this.hiderLocation = LocationUtil.deserialize((String) data.get("hider"));
        this.seekerName = data.get("seeker-team-name") != null ? (String) data.get("seeker-team-name") : "Caçadores";
        this.hiderName = data.get("hider-team-name") != null ? (String) data.get("hider-team-name") : "Escondidos";
    }

    @Override
    public EventType getType() {
        return EventType.HIDE_AND_SEEK;
    }

    @Override
    public void setWinner(Player player) {
        if (player == null || winnerTeam == null) return;
        super.setWinner(player);

        for (String string : section.getStringList("messages.winner")) {
            EventUtil.message(string.replace("{team}", winnerTeam));
        }


        List<UUID> winningTeam = winnerTeam.equalsIgnoreCase(seekerName) ? seekers : hidders;
        for (UUID uuid : winningTeam) {
            Player teamPlayer = Bukkit.getPlayer(uuid);
            if (teamPlayer != null && teamPlayer.isOnline()) {
                for (String command : getRewardCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                            .replace("{player}", teamPlayer.getName())
                            .replace("{team}", winnerTeam)
                    );
                }
            }
        }

        for (UUID uuid : getParticipants()) {
            Player other = Bukkit.getPlayer(uuid);
            if (other != null && other.isOnline()) {
                other.teleport(getLeaveLocation());
                removeKit(other);
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
                Collections.shuffle(participants);

                if (participants.isEmpty()) {
                    stop();
                    task.cancel();
                    return;
                }

                UUID seekerUUID = participants.get(0);
                seekers.add(seekerUUID);
                hidders.addAll(participants.stream().filter(uuid -> !uuid.equals(seekerUUID)).collect(Collectors.toList()));

                for (UUID uuid : getParticipants()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline()) continue;

                    if (seekers.contains(uuid)) {
                        player.teleport(seekerLocation);
                        for (String string : section.getStringList("messages.choose-seeker")) {
                            player.sendMessage(string.replace("{players}", getParticipants().size() + ""));
                        }
                        applyKit(player, getKit());
                    } else {
                        player.teleport(hiderLocation);
                        for (String string : section.getStringList("messages.choose-hider")) {
                            player.sendMessage(string.replace("{players}", getParticipants().size() + ""));
                        }
                        applyKit(player, getKit2());
                    }
                }

                for (String string : section.getStringList("messages.start")) {
                    EventUtil.message(string.replace("{players}", getParticipants().size() + ""));
                }

                Task.runRepeat(eventTask -> {
                    if (!isRunning()) {
                        stop();
                        eventTask.cancel();
                        return;
                    }

                    if (counter <= 0) {
                        if (!hidders.isEmpty()) {
                            winnerTeam = hiderName;
                            setWinner(Bukkit.getPlayer(hidders.get(0)));
                        } else {
                            winnerTeam = seekerName;
                            setWinner(Bukkit.getPlayer(seekers.get(0)));
                        }
                        eventTask.cancel();
                        return;
                    }

                    if (hidders.isEmpty()) {
                        winnerTeam = seekerName;
                        setWinner(Bukkit.getPlayer(seekers.get(0)));
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
        if (getWinner() == null) {
            for (String string : section.getStringList("messages.no-winner")) {
                EventUtil.message(string);
            }
        }

        for (UUID uuid : getParticipants()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.teleport(getLeaveLocation());
                removeKit(player);
            }
        }

        reset();
    }

    public void handleElimination(Player eliminated) {
        UUID uuid = eliminated.getUniqueId();
        if (!hidders.contains(uuid)) return;

        hidders.remove(uuid);
        seekers.add(uuid);


        for (String string : section.getStringList("messages.found")) {
            eliminated.sendMessage(string.replace("{players}", getParticipants().size() + ""));
        }
        eliminated.teleport(seekerLocation);
        applyKit(eliminated, getKit());
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getParticipants().contains(player.getUniqueId())) {
                for (String string : section.getStringList("messages.found-announce")) {
                    eliminated.sendMessage(string.replace("{player}", eliminated.getName()));
                }
            }
        }

        checkForVictory();
    }

    private void checkForVictory() {
        if (hidders.isEmpty()) {
            winnerTeam = seekerName;
            setWinner(Bukkit.getPlayer(seekers.get(0)));
        }
    }

    public boolean isSameTeam(Player p1, Player p2) {
        return (seekers.contains(p1.getUniqueId()) && seekers.contains(p2.getUniqueId())) ||
                (hidders.contains(p1.getUniqueId()) && hidders.contains(p2.getUniqueId()));
    }

    public boolean isSeeker(Player player) {
        return player != null && seekers.contains(player.getUniqueId());
    }

    public boolean isHider(Player player) {
        return player != null && hidders.contains(player.getUniqueId());
    }

    public void reset() {
        setOpen(false);
        setRunning(false);
        setWinner(null);
        this.counter = getTime();
        this.announceCalls = getCalls();
        getParticipants().clear();
        seekers.clear();
        hidders.clear();
        winnerTeam = null;
    }

}