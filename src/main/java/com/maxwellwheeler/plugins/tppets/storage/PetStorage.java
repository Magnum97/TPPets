package com.maxwellwheeler.plugins.tppets.storage;

/**
 * A small object representing the data of a pet in storage from the database.
 * @author Max
 *
 */
public class PetStorage {
    public String petId;
    public PetType.Pets petType;
    public int petX;
    public int petY;
    public int petZ;
    public String petWorld;
    public String ownerId;
    
    /**
     * General constructor, using data from the database table tpp_unloaded_pets
     * @param petId A string version of the pet's UUID, trimmed.
     * @param petTypeIndex The type of the pet represented by the integer from PetType.getIndex-type methods.
     * @param petX The x coordinate of the pet.
     * @param petY The y coordinate of the pet. Not necessarily used by this plugin, but potentially useful for others.
     * @param petZ The z coordinate of the pet.
     * @param petWorld The name of the world.
     * @param ownerId A string version of the pet owner's UUID, trimmed.
     */
    public PetStorage(String petId, int petTypeIndex, int petX, int petY, int petZ, String petWorld, String ownerId) {
        this.petId = petId;
        this.petType = PetType.getPetFromIndex(petTypeIndex);
        this.petX = petX;
        this.petY = petY;
        this.petZ = petZ;
        this.petWorld = petWorld;
        this.ownerId = ownerId;
    }
}