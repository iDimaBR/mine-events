package com.github.idimabr.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;

public class Task {

    private static JavaPlugin plugin;

    public static void init(JavaPlugin instance){
        plugin = instance;
    }

    public static void runLater(Call call){
        Bukkit.getScheduler().runTaskLater(plugin, call::call, 1L);
    }

    public static void runLater(Call call, int ticks){
        Bukkit.getScheduler().runTaskLater(plugin, call::call, ticks);
    }

    public static void runASync(Call call){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, call::call);
    }

    public static void run(Call call){
        Bukkit.getScheduler().runTask(plugin, call::call);
    }

    public static void runRepeat(Call call, int ticks, int repeats) {
        AtomicInteger remaining = new AtomicInteger(repeats);
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (remaining.getAndDecrement() <= 0) {
                task.cancel();
                return;
            }
            call.call();
        }, 1L, ticks);
    }

    public static BukkitTask runRepeat(CancelableCall call, int ticks) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                call.call(this);
            }
        }.runTaskTimer(plugin, 1L, ticks);
    }

    public interface Call {
        void call();
    }

    public interface CancelableCall {
        void call(BukkitRunnable task);
    }
}
