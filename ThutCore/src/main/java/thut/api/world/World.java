package thut.api.world;

import java.util.UUID;

import thut.api.world.blocks.Block;
import thut.api.world.mobs.Mob;
import thut.api.world.utils.Vector;

/** This is a World, it is made of Blocks on a discrete grid, and is the home to
 * Mobs.
 * 
 * @author Thutmose */
public interface World
{
    /** Gets the block for the given position.
     * 
     * @param position
     * @return */
    Block getBlock(Vector<Integer> position);

    /** @param mob
     * @return if the mob added successfully. */
    boolean addMob(Mob mob);

    /** @param mob
     * @return if the mob was removed successfully */
    boolean removeMob(Mob mob);

    /** This gets the mob by UUID
     * 
     * @param id
     * @return */
    Mob getMob(UUID id);
}
