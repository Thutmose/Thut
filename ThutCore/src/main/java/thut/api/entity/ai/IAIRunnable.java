package thut.api.entity.ai;

import net.minecraft.world.World;

/** This is a custom version of minecraft's EntityAI, it is capable of being run
 * on separate threads to improve performance. Every method here other than
 * doMainThreadTick() must be made such that it is thread safe. <br>
 * These methods are only ever called on the server, they are never called from
 * client side. */
public interface IAIRunnable
{
    /** called to execute the needed non-threadsafe tasks on the main thread. */
    void doMainThreadTick(World world);

    /** Will only run an AI if it is higher priority (ie lower number) or a
     * bitwise AND of the two mutex is 0.
     * 
     * @return */
    int getMutex();

    /** @return the priority of this AIRunnable. Lower numbers run first. */
    int getPriority();

    /** Resets the task. */
    void reset();

    /** runs the task */
    void run();

    /** Sets the mutex.
     * 
     * @param mutex
     * @return */
    IAIRunnable setMutex(int mutex);

    /** Sets the priority.
     * 
     * @param prior
     * @return */
    IAIRunnable setPriority(int prior);

    /** Should the task start running. if true, will call run next.
     * 
     * @return */
    boolean shouldRun();
}
