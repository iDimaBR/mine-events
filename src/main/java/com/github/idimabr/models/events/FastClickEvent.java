package com.github.idimabr.models.events;

import com.github.idimabr.MineEvents;
import com.github.idimabr.models.CustomEvent;
import com.github.idimabr.models.EventType;
import com.github.idimabr.utils.ConfigUtil;
import com.github.idimabr.utils.EventUtil;
import com.github.idimabr.utils.Task;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class FastClickEvent extends CustomEvent {

    private final ConfigurationSection section;
    private final String clickable;
    private final String not_clickable;
    private final int lines;
    private final int quantity;

    private int correct_line;
    private int correct_index;

    private int counter = getCalls();

    public FastClickEvent(String name, String id, EventType type, boolean emptyInventory, int calls, int callTime, int time, int minPlayers, List<String> rewardCommands, Location lobbyLocation, Location joinLocation, Location leaveLocation, Location corner1, Location corner2, ItemStack[] kit, Map<String, Object> data) {
        super(name, id, type, emptyInventory, calls, callTime, time, minPlayers, rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, data);

        final ConfigUtil config = MineEvents.getPlugin().getConfig();
        this.section = config.getConfigurationSection("events." + id);

        this.clickable = (String) data.get("click");
        this.not_clickable = (String) data.get("no-click");
        this.lines = (int) data.get("lines");
        this.quantity = (int) data.get("quantity");

        reset();
    }

    @Override
    public EventType getType() {
        return EventType.FASTCLICK;
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
        reset();
    }

    @Override
    public void start() {
        setRunning(true);
        Task.runRepeat(task -> {
            if (counter <= 0 || !isRunning()) {
                stop();
                task.cancel();
                return;
            }

            counter--;
        }, getCallTime() * 20);

        for (String string : section.getStringList("messages.start")) {
            for(int i = 1; i <= lines; i++) {
                if(string.contains("{line" + i + "}")) {
                    string = string.replace("{line" + i + "}", "");
                    TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', string)));
                    for (int z = 1; z <= quantity; z++) {

                        String button = "";
                        if (i == correct_line && z == correct_index) {
                            button += clickable;
                        } else {
                            button += not_clickable;
                        }

                        TextComponent click = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', button)));
                        click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/eventocheck " + i + " " + z));
                        component.addExtra(click);
                    }
                    EventUtil.sendComponent(component);
                }
            }
            EventUtil.message(ChatColor.translateAlternateColorCodes('&', string));
        }
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

    public void checkArgument(Player player, String[] args){
        int line;
        int index;
        try {
            line = Integer.parseInt(args[1]);
            index = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }

        if(line == correct_line && index == correct_index) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            setWinner(player);
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
    }

    public void reset(){
        MineEvents.getPlugin().getController().setActualEvent(null);
        setOpen(false);
        setRunning(false);
        setWinner(null);
        this.counter = getTime();
        this.correct_line = ThreadLocalRandom.current().nextInt(1, lines + 1);
        this.correct_index = ThreadLocalRandom.current().nextInt(1, quantity + 1);
        getParticipants().clear();
    }
}
