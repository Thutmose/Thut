package thut.api.world.mobs;

import java.util.UUID;

import thut.api.world.World;
import thut.api.world.utils.Info;
import thut.api.world.utils.Vector;

/** This is a Mob, it is located at a point, which is a Vector, and lives in a
 * World, it can move, with a velocity (which is also a Vector).<br>
 * <br>
 * In other words, this mob lives on the tangent bundle of the configuration
 * manifold represented by the World.
 * 
 * @author Thutmose */
public interface Mob
{
    /** The world this mob lives in.
     * 
     * @return */
    World world();

    void setWorld(World world);

    /** Where this mob is on the world grid.
     * 
     * @return */
    Vector<Integer> worldPos();

    /** Where the mob is
     * 
     * @return */
    Vector<Double> position();

    /** How fast this mob is moving
     * 
     * @return */
    Vector<Double> velocity();

    /** Stored info for this mob.
     * 
     * @return */
    Info info();

    /** UUID of this mob.
     * 
     * @return */
    UUID id();

    void setID(UUID id);
}
