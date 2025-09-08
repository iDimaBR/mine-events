package com.github.idimabr;

import com.github.idimabr.commands.AdminCommand;
import com.github.idimabr.commands.EventCommand;
import com.github.idimabr.controllers.EventController;
import com.github.idimabr.listeners.*;
import com.github.idimabr.tasks.TNTTask;
import com.github.idimabr.utils.ConfigUtil;
import com.github.idimabr.utils.Task;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Getter
public final class VitinEvents extends JavaPlugin {

    @Getter
    private static VitinEvents plugin;
    private EventController controller = new EventController();
    private ConfigUtil config;

    @Override
    public void onLoad() {
        this.config = new ConfigUtil(this, "config.yml");
    }

    @Override
    public void onEnable() {
        plugin = this;
        Task.init(this);
        controller.load(config);
        createFolder();
        loadCommands();
        loadListeners();
        loadTasks();
    }

    @Override
    public void onDisable() {

    }

    private void loadCommands(){
        getCommand("aevento").setExecutor(new AdminCommand(controller, config));
        getCommand("evento").setExecutor(new EventCommand(controller));
    }

    private void loadListeners(){
        final PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new EventListener(controller, config), this);

        pluginManager.registerEvents(new ParkourListener(controller), this);
        pluginManager.registerEvents(new HiddenSeekListener(controller), this);
        pluginManager.registerEvents(new PaintballListener(controller), this);
        pluginManager.registerEvents(new PotatoListener(controller), this);
        pluginManager.registerEvents(new SpleefListener(controller), this);
        pluginManager.registerEvents(new ChatListener(controller), this);
        pluginManager.registerEvents(new SignListener(controller), this);
        pluginManager.registerEvents(new TNTListener(controller), this);
    }

    private void loadTasks() {
        new TNTTask(controller).runTaskTimer(this, 0L, 2L);
    }

    public void createFolder() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File schematicsFolder = new File(getDataFolder(), "schematics");
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }
    }
}
