package com.maxwellwheeler.plugins.tppets.commands;

import com.maxwellwheeler.plugins.tppets.TPPets;
import com.maxwellwheeler.plugins.tppets.helpers.ArgValidator;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class CommandStorageAdd extends Command {
    CommandStorageAdd(TPPets thisPlugin, Player sender, OfflinePlayer commandFor, String[] args) {
        super(thisPlugin, sender, commandFor, args);
    }

    @Override
    public void processCommand() {
        addStorage();
        displayStatus();
    }

    private void addStorage() {
        if (!this.thisPlugin.canTpThere(this.sender)) {
            this.commandStatus = CommandStatus.CANT_TELEPORT_IN_PR;
            return;
        }

        if (!ArgValidator.validateArgsLength(this.args, 1)) {
            this.commandStatus = CommandStatus.SYNTAX_ERROR;
            return;
        }

        if (!ArgValidator.validateStorageName(this.args[0])) {
            this.commandStatus = CommandStatus.INVALID_NAME;
            return;
        }

        if (addStorageToDb()) {
            this.commandStatus = CommandStatus.SUCCESS;
        }
    }

    private boolean addStorageToDb() {
        if (this.thisPlugin.getDatabase() == null) {
            this.commandStatus = CommandStatus.DB_FAIL;
            return false;
        }

        if (this.thisPlugin.getDatabase().getStorageLocation(this.commandFor.getUniqueId().toString(), this.args[0]) != null) {
            this.commandStatus = CommandStatus.ALREADY_DONE;
            return false;
        }

        if (!isNewStorageWithinLimit()) {
            this.commandStatus = CommandStatus.LIMIT_REACHED;
            return false;
        }

        if (this.thisPlugin.getDatabase().addStorageLocation(this.commandFor.getUniqueId().toString(), this.args[0], this.sender.getLocation())) {
            this.thisPlugin.getLogWrapper().logSuccessfulAction("Player " + this.sender.getUniqueId().toString() + " has added location " + this.args[0] + " " + TeleportCommand.formatLocation(this.sender.getLocation()) + " for " + this.commandFor.getName());
            this.commandStatus = CommandStatus.SUCCESS;
            return true;
        } else {
            this.commandStatus = CommandStatus.DB_FAIL;
            return false;
        }
    }

    public boolean isNewStorageWithinLimit() {
        return this.sender.hasPermission("tppets.bypassstoragelimit") || this.thisPlugin.getStorageLimit() < 0 || this.thisPlugin.getDatabase().getStorageLocations(this.commandFor.getUniqueId().toString()).size() < this.thisPlugin.getStorageLimit();
    }

    private void displayStatus() {
        // SUCCESS, DB_FAIL, LIMIT_REACHED, INVALID_NAME, ALREADY_DONE, SYNTAX_ERROR, CANT_TP_THERE
        switch(this.commandStatus) {
            case INVALID_SENDER:
            case CANT_TELEPORT_IN_PR:
                break;
            case SUCCESS:
                this.sender.sendMessage((this.isForSelf() ? ChatColor.BLUE + "You have" : ChatColor.WHITE + this.commandFor.getName() + ChatColor.BLUE + " has") + " added storage location " + ChatColor.WHITE + this.args[0]);
                break;
            case DB_FAIL:
                this.sender.sendMessage(ChatColor.RED + "Could not add storage location");
                break;
            case LIMIT_REACHED:
                this.sender.sendMessage((this.isForSelf() ? ChatColor.RED + "You" : ChatColor.WHITE + this.commandFor.getName() + ChatColor.RED) + " can't set any more than " + ChatColor.WHITE + this.thisPlugin.getStorageLimit() + ChatColor.RED + " storage locations");
                break;
            case INVALID_NAME:
                this.sender.sendMessage(ChatColor.RED + "Invalid storage location name: " + ChatColor.WHITE + this.args[0]);
                break;
            case ALREADY_DONE:
                this.sender.sendMessage((this.isForSelf() ? ChatColor.RED + "Storage" : ChatColor.WHITE + this.commandFor.getName() + "'s" + ChatColor.RED + " storage") + " location " + ChatColor.WHITE + this.args[0] + ChatColor.RED + " already exists");
                break;
            case SYNTAX_ERROR:
                this.sender.sendMessage(ChatColor.RED + "Syntax Error! Usage: /tpp storage add [storage name]");
                break;
            default:
                this.sender.sendMessage(ChatColor.RED + "An unknown error occurred");
                break;
        }
    }
}
