package com.crayonsmp.neomUtilities.commands;

import com.crayonsmp.neomUtilities.NeomUtilities;
import com.crayonsmp.neomUtilities.items.hatchableblock.HatchService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("neomutilities.reload")) {
            sender.sendMessage("§cKeine Berechtigung.");
            return true;
        }

        NeomUtilities.getInstance().reloadConfig();

        var config = NeomUtilities.getInstance().getConfig();
        NeomUtilities.getVariableService().loadVariables(config);

        NeomUtilities.getBiomChangerService().loadConfig();
        NeomUtilities.getSequencerService().loadRecipes();
        HatchService.reload();


        sender.sendMessage("§a[NeomUtilities] Reload abgeschlossen!");
        return true;
    }
}