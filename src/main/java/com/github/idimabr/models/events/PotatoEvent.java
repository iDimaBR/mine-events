package com.github.idimabr.models.events;

import com.github.idimabr.MineEvents;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.EventType;
import com.github.idimabr.utils.*;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PotatoEvent extends CustomEvent {

    private final ConfigurationSection section;
    private int counter = getTime();
    private int announceCalls = getCalls();
    private int initialTime;

    private int currentTime = initialTime;
    private Player hotPotato;
    private Sound hotPotatoSound = Sound.BLOCK_NOTE_BLOCK_PLING;

    public PotatoEvent(String name, String id, EventType type, boolean emptyInventory, int calls, int callTime, int time, int minPlayers, List<String> rewardCommands, Location lobbyLocation, Location joinLocation, Location leaveLocation, Location corner1, Location corner2, ItemStack[] kit, Map<String, Object> data) {
        super(name, id, type, emptyInventory, calls, callTime, time, minPlayers, rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, data);
        final ConfigUtil config = MineEvents.getPlugin().getConfig();
        this.section = config.getConfigurationSection("events." + id);
        this.initialTime = (int) data.get("explode-time");
        try {
            this.hotPotatoSound = Sound.valueOf((String) data.getOrDefault("sound", "BLOCK_NOTE_BLOCK_PLING"));
        } catch (Exception ignored){}
    }

    @Override
    public EventType getType() {
        return EventType.POTATO;
    }

    @Override
    public void setWinner(Player player) {
        if (player == null) return;
        super.setWinner(player);

        for (String string : section.getStringList("messages.winner")) {
            EventUtil.message(string.replace("{player}", player.getName()));
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
            final List<UUID> participants = getParticipants();
            if (announceCalls <= 0) {
                setOpen(false);
                setRunning(true);

                for (UUID uuid : participants) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        player.teleport(getJoinLocation());
                    }
                }

                if(!participants.isEmpty()) {
                    newRound();
                }

                for (String string : section.getStringList("messages.start")) {
                    EventUtil.message(string.replace("{players}", participants.size() + "").replace("{potato}", hotPotato.getName()));
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
                EventUtil.message(string.replace("{players}", participants.size() + "").replace("{calls}", String.valueOf(announceCalls)));
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

        for (UUID uuid : getParticipants()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.teleport(getLeaveLocation());
            }
        }

        reset();
    }

    public void newRound() {
        currentTime = initialTime;

        List<UUID> list = new ArrayList<>(getParticipants());
        if (list.isEmpty()) return;

        UUID random = list.get(new Random().nextInt(list.size()));
        hotPotato = Bukkit.getPlayer(random);
        if (hotPotato != null) {
            for (String string : section.getStringList("messages.new-potato")) {
                EventUtil.message(string.replace("{players}", getParticipants().size() + "").replace("{potato}", hotPotato.getName()));
            }
            hotPotato.getInventory().setHelmet(new ItemStack(Material.TNT));
        }

        final int[] currentTimeTicks = {0};
        Task.runRepeat(task -> {
            if (!isRunning() || hotPotato == null) {
                task.cancel();
                return;
            }

            if (currentTimeTicks[0] % 20 == 0) {
                currentTime--;
                if (currentTime <= 0) {
                    Location loc = hotPotato.getLocation();
                    loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 1);
                    loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.2f);
                    for (String string : section.getStringList("messages.explode")) {
                        EventUtil.message(string.replace("{players}", getParticipants().size() + "").replace("{potato}", hotPotato.getName()));
                    }

                    hotPotato.setDisplayName(hotPotato.getName());
                    hotPotato.setCustomName(hotPotato.getName());
                    removeParticipant(hotPotato.getUniqueId());
                    if (getParticipants().size() <= 1) {
                        if (!getParticipants().isEmpty()) {
                            setWinner(Bukkit.getPlayer(getParticipants().get(0)));
                        } else {
                            stop();
                        }
                    } else {
                        newRound();
                    }

                    task.cancel();
                    return;
                }
            }

            int ticksBetweenBeeps = Math.max(2, currentTime * 20 / initialTime);
            if (currentTimeTicks[0] % ticksBetweenBeeps == 0) {
                float pitch = 0.5f + ((initialTime - currentTime) * 0.1f);
                hotPotato.getWorld().playSound(hotPotato.getLocation(), hotPotatoSound, 0.5f, pitch);
            }
            currentTimeTicks[0]++;
        }, 1);
    }

    public boolean isHotPotato(Player player) {
        return player != null && player.equals(hotPotato);
    }

    public void passPotato(Player from, Player to) {
        if (from == null || to == null || !isHotPotato(from)) return;

        this.hotPotato = to;
        from.getInventory().setHelmet(null);
        to.getInventory().setHelmet(new ItemStack(Material.TNT));
        for (String string : section.getStringList("messages.new-potato")) {
            EventUtil.message(string.replace("{players}", getParticipants().size() + "").replace("{potato}", hotPotato.getName()));
        }
    }

    public void reset() {
        MineEvents.getPlugin().getController().setActualEvent(null);
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
        this.hotPotato = null;
        currentTime = initialTime;
    }
}
