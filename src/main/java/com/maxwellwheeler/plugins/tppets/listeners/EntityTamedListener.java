package com.maxwellwheeler.plugins.tppets.listeners;

import com.maxwellwheeler.plugins.tppets.TPPets;
import com.maxwellwheeler.plugins.tppets.helpers.EntityActions;
import com.maxwellwheeler.plugins.tppets.helpers.PermissionChecker;
import com.maxwellwheeler.plugins.tppets.storage.PetType;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityTameEvent;

import java.sql.SQLException;

public class EntityTamedListener implements Listener {
    private final TPPets thisPlugin;

    public EntityTamedListener(TPPets thisPlugin) {
        this.thisPlugin = thisPlugin;
    }

    private EventStatus onNewTamedEntity(OfflinePlayer owner, Entity entity) {
        PetType.Pets petType = PetType.getEnumByEntity(entity);

        boolean canBypassPetLimit = canBypassPetLimit(owner, entity.getWorld());

        try {
            if (!canBypassPetLimit && !this.thisPlugin.getPetIndex().isWithinTotalLimit(owner)) {
                return EventStatus.TOTAL_LIMIT;
            }

            if (!canBypassPetLimit && !this.thisPlugin.getPetIndex().isWithinSpecificLimit(owner, petType)) {
                return EventStatus.TYPE_LIMIT;
            }
        } catch (SQLException ignored) {
            return EventStatus.DB_FAIL;
        }

        String generatedName = this.thisPlugin.getDatabase().insertPet(entity, owner.getUniqueId().toString());

        if (generatedName == null) {
            return EventStatus.DB_FAIL;
        }

        if (owner instanceof Player) {
            ((Player)owner).sendMessage(ChatColor.BLUE + "You've tamed a pet! Its current name is " + ChatColor.WHITE + generatedName + ChatColor.BLUE + ". You can rename it with /tpp rename " + ChatColor.WHITE + generatedName + ChatColor.BLUE + " [new name]");
        }

        return EventStatus.SUCCESS;
    }

    private boolean canBypassPetLimit(OfflinePlayer owner, World world) {
        if (owner instanceof Player) {
            return PermissionChecker.onlineHasPerms(owner, "tppets.bypasslimit");
        }
        return PermissionChecker.offlineHasPerms(owner, "tppets.bypasslimit", world, this.thisPlugin);
    }

    private void cancelTame(Tameable pet) {
        EntityActions.setStanding(pet);
        pet.setOwner(null);
        if (!(pet instanceof SkeletonHorse || pet instanceof ZombieHorse)) {
            pet.setTamed(false);
        }
    }

    private void displayStatus(AnimalTamer owner, Tameable pet, EventStatus eventStatus) {
        if (owner instanceof Player) {
            Player player = (Player) owner;

            switch(eventStatus) {
                case TOTAL_LIMIT:
                    player.sendMessage(ChatColor.RED + "You've exceeded the limit for total pets! Limit: " + this.thisPlugin.getPetIndex().getTotalLimit());
                    break;
                case TYPE_LIMIT:
                    PetType.Pets petType = PetType.getEnumByEntity(pet);
                    String petTypeString = petType.toString();
                    player.sendMessage(ChatColor.RED + "You've exceeded the limit for this pet type! " + petTypeString.charAt(0) + petTypeString.substring(1).toLowerCase() + " Limit: " + this.thisPlugin.getPetIndex().getSpecificLimit(petType));
                    break;
                case DB_FAIL:
                    player.sendMessage(ChatColor.RED + "Could not tame this pet");
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "An unknown error occurred");
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTameEvent(EntityTameEvent event) {
        if (!event.isCancelled() && PetType.isPetTracked(event.getEntity()) && event.getOwner() instanceof OfflinePlayer) {
            Tameable pet = (Tameable) event.getEntity();
            EventStatus eventStatus = onNewTamedEntity((OfflinePlayer) event.getOwner(), pet);

            if (eventStatus != EventStatus.SUCCESS) {
                cancelTame(pet);
                event.setCancelled(true);
                displayStatus(event.getOwner(), pet, eventStatus);
            }

        }
    }

    private boolean isEntityTamedByPlayer(Entity entity) {
        return PetType.isPetTracked(entity) && ((Tameable)entity).getOwner() instanceof OfflinePlayer;
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onEntityBreedEvent(EntityBreedEvent event) {
        // e.getEntity() = new animal
        // e.getMother() = one parent
        // e.getFather() = other parent
        // e.getBreeder() = entity (mostly player) that initiated the breeding by feeding
        if (!event.isCancelled() && isEntityTamedByPlayer(event.getEntity())) {
            Tameable pet = (Tameable) event.getEntity();
            OfflinePlayer owner = (OfflinePlayer) pet.getOwner();
            EventStatus eventStatus = onNewTamedEntity(owner, pet);

            if (eventStatus != EventStatus.SUCCESS) {
                cancelTame(pet);
                event.setCancelled(true);
                displayStatus(owner, pet, eventStatus);
            }

        }
    }
}
