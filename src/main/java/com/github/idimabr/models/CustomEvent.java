package com.github.idimabr.models;

import com.github.idimabr.VitinEvents;
import com.github.idimabr.controllers.EventController;
import com.github.idimabr.utils.ConfigUtil;
import com.github.idimabr.utils.LocationUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

@Getter
@Setter
public abstract class CustomEvent {

    private String name;
    private String id;
    private EventType type;
    private int calls;
    private int callTime;
    private int time;
    private int minPlayers;
    private boolean open = false;
    private boolean running = false;
    private boolean emptyInventory = false;
    private List<String> rewardCommands;
    private List<UUID> participants;
    private Location joinLocation;
    private Location lobbyLocation;
    private Location leaveLocation;

    private Location corner1;
    private Location corner2;
    private Map<String, Object> data;
    @Getter
    private Player winner;
    private ConfigurationSection section;
    private ItemStack[] kit;
    private ItemStack[] kit2;

    protected CustomEvent(String name, String id, EventType type, boolean emptyInventory, int calls, int callTime, int time, int minPlayers, List<String> rewardCommands, Location lobbyLocation, Location joinLocation, Location leaveLocation, Location corner1, Location corner2, ItemStack[] kit, Map<String, Object> data) {
        this.name = name;
        this.id = id;
        this.type = type;
        this.emptyInventory = emptyInventory;
        this.calls = calls;
        this.callTime = callTime;
        this.time = time;
        this.minPlayers = minPlayers;
        this.rewardCommands = rewardCommands;
        this.participants = new ArrayList<>();
        this.lobbyLocation = lobbyLocation;
        this.joinLocation = joinLocation;
        this.leaveLocation = leaveLocation;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.kit = kit;
        this.data = data != null ? data : new HashMap<>();
        final ConfigUtil config = VitinEvents.getPlugin().getConfig();
        this.section = config.getConfigurationSection("events." + id);
    }

    public void addParticipant(UUID uuid){
        if(participants == null) return;
        if(!participants.contains(uuid)){
            participants.add(uuid);
        }
    }

    public boolean isParticipant(UUID uuid){
        if(participants == null) return false;
        return participants.contains(uuid);
    }

    public void removeParticipant(UUID uuid){
        if(participants == null) return;
        participants.remove(uuid);
    }

    public void setWinner(Player player){
        this.winner = player;
    }

    public void start() {
        this.running = true;
    }

    public void stop(){
        this.running = false;
        this.participants.clear();
    }

    public void setCorner1(Location corner1) {
        this.corner1 = corner1;
        section.set("locations.corner1", LocationUtil.serialize(corner1));
    }

    public void setCorner2(Location corner2) {
        this.corner2 = corner2;
        section.set("locations.corner2", LocationUtil.serialize(corner2));
    }

    public void setKit(ItemStack[] kit) {
        this.kit = kit;
    }

    public void applyKit(Player player, ItemStack[] contents) {
        if (kit == null || kit.length == 0) return;

        final PlayerInventory inventory = player.getInventory();
        inventory.setContents(Arrays.copyOfRange(contents, 0, 36));
        inventory.setArmorContents(Arrays.copyOfRange(contents, 36, 40));
        inventory.setItemInOffHand(contents[40]);
    }

    public void removeKit(Player player){
        if (kit == null || kit.length == 0) return;
        final PlayerInventory inventory = player.getInventory();
        inventory.clear();
        inventory.setArmorContents(null);
        inventory.setItemInOffHand(null);
    }

    public boolean isSetupValid(){
        switch(type) {
            case FASTCLICK:
            case MATH:
                return true;
            default:
                return lobbyLocation != null && joinLocation != null && leaveLocation != null;
        }
    }

    public boolean betweenCorners(Player player){
        if (corner1 == null || corner2 == null) return false;
        Location loc = player.getLocation();
        return loc.getX() >= Math.min(corner1.getX(), corner2.getX()) &&
               loc.getX() <= Math.max(corner1.getX(), corner2.getX()) &&
               loc.getY() >= Math.min(corner1.getY(), corner2.getY()) &&
               loc.getY() <= Math.max(corner1.getY(), corner2.getY()) &&
               loc.getZ() >= Math.min(corner1.getZ(), corner2.getZ()) &&
               loc.getZ() <= Math.max(corner1.getZ(), corner2.getZ());
    }
}
