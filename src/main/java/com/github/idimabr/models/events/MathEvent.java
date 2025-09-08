package com.github.idimabr.models.events;

import com.github.idimabr.MineEvents;
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
import java.util.concurrent.ThreadLocalRandom;

public class MathEvent extends CustomEvent {

    private final ConfigurationSection section;
    private String sum;
    private int result;

    private int counter = getCalls();

    public MathEvent(String name, String id, EventType type, boolean emptyInventory, int calls, int callTime, int time, int minPlayers, List<String> rewardCommands, Location lobbyLocation, Location joinLocation, Location leaveLocation, Location corner1, Location corner2, ItemStack[] kit, Map<String, Object> data) {
        super(name, id, type, emptyInventory, calls, callTime, time, minPlayers, rewardCommands, lobbyLocation, joinLocation, leaveLocation, corner1, corner2, kit, data);
        final ConfigUtil config = MineEvents.getPlugin().getConfig();
        this.section = config.getConfigurationSection("events." + id);
        reset();
    }

    @Override
    public EventType getType() {
        return EventType.MATH;
    }

    @Override
    public void setWinner(Player player) {
        if(player == null) return;
        super.setWinner(player);

        for (String string : section.getStringList("messages.winner")) {
            string = string.replace("{sum}", sum)
                    .replace("{result}", result+"")
                    .replace("{player}", player.getName())
                    .replace("{time}", String.valueOf(getTime()));
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
            string = string.replace("{sum}", sum)
                    .replace("{result}", result+"")
                    .replace("{time}", String.valueOf(getTime()));
            EventUtil.message(string);
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

    public boolean isCorrect(String input) {
        return input.equalsIgnoreCase(result+"");
    }

    public void reset(){
        MineEvents.getPlugin().getController().setActualEvent(null);
        setOpen(false);
        setRunning(false);
        setWinner(null);
        this.counter = getTime();
        final int min = (int) getData().get("min");
        final int max = (int) getData().get("max");
        int account_type = ThreadLocalRandom.current().nextInt(0, 1 + 1);
        int number1 = ThreadLocalRandom.current().nextInt(min, max + 1);
        int number2 = ThreadLocalRandom.current().nextInt(min, max + 1);
        switch(account_type) {
            case 0:
                sum = number1  + " + " + number2;
                result = Math.round(number1 + number2);
                break;
            case 1:
                sum = number1  + " - " + number2;
                result = Math.round(number1 - number2);
                break;
        }
        getParticipants().clear();
    }
}
