package com.github.idimabr.utils;

import com.github.idimabr.MineEvents;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.Location;
import java.io.File;
import java.io.FileInputStream;

public class SchematicUtil {

    public static void paste(Location location, String schematicName) {
        try {
            File schematicFile = new File(MineEvents.getPlugin().getDataFolder() + "/schematics/", schematicName);

            if (!schematicFile.exists()) {
                throw new IllegalArgumentException("Schematic não encontrado: " + schematicName);
            }

            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                throw new IllegalArgumentException("Formato de schematic desconhecido: " + schematicFile.getName());
            }

            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                Clipboard clipboard = reader.read();

                World adaptedWorld = BukkitAdapter.adapt(location.getWorld());
                EditSession editSession = WorldEdit.getInstance()
                        .getEditSessionFactory()
                        .getEditSession(adaptedWorld, -1);

                BlockVector3 pasteLocation = BlockVector3.at(
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ()
                );

                Operations.complete(
                        new ClipboardHolder(clipboard)
                                .createPaste(editSession)
                                .to(pasteLocation)
                                .ignoreAirBlocks(true)
                                .build()
                );

                editSession.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}