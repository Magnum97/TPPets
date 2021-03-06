package com.maxwellwheeler.plugins.tppets.commands;

import com.maxwellwheeler.plugins.tppets.TPPets;
import com.maxwellwheeler.plugins.tppets.helpers.ArgValidator;
import com.maxwellwheeler.plugins.tppets.helpers.PermissionChecker;
import com.maxwellwheeler.plugins.tppets.storage.PetStorage;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CommandTeleportPet extends TeleportCommand {
    private PetStorage pet;

    public CommandTeleportPet(TPPets thisPlugin, CommandSender sender, String[] args) {
        super(thisPlugin, sender, args);
    }

    protected boolean hasPermissionToTp(Player player, OfflinePlayer petOwner, String petUUID) {
        return player.equals(petOwner) || player.hasPermission("tppets.teleportother") || thisPlugin.isAllowedToPet(petUUID, player.getUniqueId().toString());
    }

    @Override
    protected boolean hasValidForOtherPlayerFormat(String permission, int numArgs) {
        // 1) Check that the command sender exists and is a player
        if (this.sender == null) {
            this.commandStatus = CommandStatus.INVALID_SENDER;
            return false;
        }

        // 2) Check that the player has permission to execute this type of command
        if (!hasPermissionToTp(this.sender, this.commandFor, this.pet.petId)) {
            this.commandStatus = CommandStatus.INSUFFICIENT_PERMISSIONS;
            return false;
        }

        // 3) Check if there's enough arguments to make a valid command
        if (!ArgValidator.validateArgsLength(this.args, 1)) {
            this.commandStatus = CommandStatus.SYNTAX_ERROR;
            return false;
        }

        // 4) All clear for now. Set commandStatus to SUCCESS to override any earlier calls to similar methods
        this.commandStatus = CommandStatus.SUCCESS;
        return true;
    }

    public void processCommand() {
        if (this.commandStatus == CommandStatus.SUCCESS && initializePet() && isValidSyntax()) {
            processCommandGeneric();
        }

        displayStatus();
    }

    private boolean isValidSyntax() {
        // Remember that correctForSelfSyntax() will not run if correctForOtherPlayerSyntax() is true
        return (this.isIntendedForSomeoneElse && hasValidForOtherPlayerFormat("tppets.teleportother", 1)) || (!this.isIntendedForSomeoneElse && hasValidForSelfFormat(1));
    }

    private void processCommandGeneric() {
        if (!PermissionChecker.hasPermissionToTeleportType(this.pet.petType, this.sender)) {
            this.commandStatus = CommandStatus.INSUFFICIENT_PERMISSIONS;
            return;
        }

        if (!this.thisPlugin.canTpThere(this.sender)) {
            this.commandStatus = CommandStatus.CANT_TELEPORT_IN_PR;
            return;
        }

        if (!this.teleportPetsFromStorage(this.sender.getLocation(), Collections.singletonList(this.pet), this.isIntendedForSomeoneElse, !this.isIntendedForSomeoneElse || this.sender.hasPermission("tppets.teleportother"))) {
            this.commandStatus = CommandStatus.CANT_TELEPORT;
        }
    }

    private boolean initializePet() {
        if (!ArgValidator.validateArgsLength(this.args, 1)) {
            this.commandStatus = CommandStatus.SYNTAX_ERROR;
            return false;
        }

        if (!ArgValidator.softValidatePetName(this.args[0])) {
            this.commandStatus = CommandStatus.NO_PET;
            return false;
        }

        List<PetStorage> petList = this.thisPlugin.getDatabase().getPetByName(this.commandFor.getUniqueId().toString(), this.args[0]);

        if (petList == null) {
            this.commandStatus = CommandStatus.DB_FAIL;
            return false;
        }

        if (petList.size() == 0) {
            this.commandStatus = CommandStatus.NO_PET;
            return false;
        }

        this.pet = petList.get(0);
        return true;
    }

    private void displayStatus() {
        switch (this.commandStatus) {
            case SUCCESS:
                this.sender.sendMessage((this.isIntendedForSomeoneElse ? ChatColor.WHITE + this.commandFor.getName() + "'s " + ChatColor.BLUE + "pet " : ChatColor.BLUE + "Your pet ") + ChatColor.WHITE + this.pet.petName + ChatColor.BLUE + " has been teleported to you");
                break;
            case INSUFFICIENT_PERMISSIONS:
                this.sender.sendMessage(ChatColor.RED + "You don't have permission to do that");
                break;
            case NO_PLAYER:
                this.sender.sendMessage(ChatColor.RED + "Can't find player: " + ChatColor.WHITE + ArgValidator.isForSomeoneElse(this.args[0]));
                break;
            case SYNTAX_ERROR:
                this.sender.sendMessage(ChatColor.RED + "Syntax Error! Usage: /tpp tp [pet name]");
                break;
            case NO_PET:
                this.sender.sendMessage(ChatColor.RED + "Could not find pet: " + ChatColor.WHITE +  this.args[0]);
                break;
            case DB_FAIL:
                this.sender.sendMessage(ChatColor.RED + "Could not find pet to teleport");
                break;
            case CANT_TELEPORT:
                this.sender.sendMessage(ChatColor.RED + "Could not teleport pet");
                break;
            case CANT_TELEPORT_IN_PR:
                break;
            default:
                this.sender.sendMessage(ChatColor.RED + "An unknown error occurred");
                break;
        }
    }
}
