package thut.api.entity.ai;

import net.minecraft.world.World;

/** These are used for any misc logic needed for the mob. doLogic() is called on
 * a separate thread, and must be thread safe. doServerTick is called on the
 * main thread, it is called both client and server side., sometime after
 * doLogic is called. */
public interface ILogicRunnable
{
    /** Runs this logic for the entity, this is called on the AI thread. */
    void doLogic();

    /** Runs this logic on the main thread, dispite the name, this is called on
     * both server and client threads, but not on the AI thread. */
    void doServerTick(World world);

    /** @return an identifier for use with saving this if it is supposed to be
     *         saved to capability data. */
    default String getIdentifier()
    {
        return "";
    }

    /** If this is saveable, should tag be synced to clients.
     * 
     * @return */
    default boolean sync()
    {
        return false;
    }
}
