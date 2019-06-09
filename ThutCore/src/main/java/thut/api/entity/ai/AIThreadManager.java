package thut.api.entity.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.entity.MobEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.ThutCore;

@SuppressWarnings({ "rawtypes", "unchecked" })
/** This is the main manager for the multi-threaded mob AI code. It creates the
 * threads, and manages sorting the AI tasks to which thread they should be on.
 * It also calls the various tick methods for the AI on the AI threads. */
public class AIThreadManager
{
    /** A collecion of AITasks and AILogics for the entity to run on seperate
     * threads.
     * 
     * @author Thutmose */
    public static class AIStuff
    {
        public static int                      tickRate = 1;
        public final MobEntity              entity;
        public final ArrayList<IAIRunnable>    aiTasks  = new ArrayList<IAIRunnable>();
        public final ArrayList<ILogicRunnable> aiLogic  = new ArrayList<ILogicRunnable>();

        public AIStuff(MobEntity entity_)
        {
            entity = entity_;
        }

        public void addAILogic(ILogicRunnable logic)
        {
            aiLogic.add(logic);
        }

        public void addAITask(IAIRunnable task)
        {
            aiTasks.add(task);
        }

        /** Run all of the server-thread tasks for the IAIRunnables. The
         * ILogicRunnables are managed elsewhere.
         * 
         * @param world */
        public void runServerThreadTasks(World world)
        {
            if (!ThutCore.instance.config.multithreadedAI)
            {
                // Cancel tick based on tick rate config. Use world time so that
                // all ai ticks are done at the same time, this increases load
                // for the AI tick, but prevents issues where certain AI tasks
                // run out of sync across different mobs.
                if (world.getGameTime() % tickRate != 0) return;
                tick();
            }

            for (IAIRunnable ai : aiTasks)
            {
                ai.doMainThreadTick(world);
            }
        }

        /** Run all of the thread-safe methods for the IAIRunnables and the
         * ILogicRunnables. */
        public void tick()
        {
            ArrayList list;
            synchronized (aiTasks)
            {
                list = aiTasks;
                if (list != null) for (int i = 0; i < list.size(); i++)
                {
                    IAIRunnable ai = (IAIRunnable) list.get(i);
                    try
                    {
                        if (canRun(ai, list))
                        {
                            ai.run();
                        }
                        else
                        {
                            ai.reset();
                        }
                    }
                    catch (Exception e)
                    {
                        logger.log(Level.SEVERE, "error checking task " + ai, e);
                    }
                }
            }
            list = aiLogic;
            if (list != null) for (int i = 0; i < list.size(); i++)
            {
                ILogicRunnable runnable = (ILogicRunnable) list.get(i);
                try
                {
                    runnable.doLogic();
                }
                catch (Exception e)
                {
                    logger.log(Level.SEVERE, "error executing " + runnable, e);
                }
            }
        }
    }

    public static Logger                                           logger;

    /** Used for sorting the AI runnables for run order. */
    public static final Comparator<IAIRunnable>                    aiComparator  = new Comparator<IAIRunnable>()
                                                                                 {
                                                                                     @Override
                                                                                     public int compare(IAIRunnable o1,
                                                                                             IAIRunnable o2)
                                                                                     {
                                                                                         return o1.getPriority()
                                                                                                 - o2.getPriority();
                                                                                     }
                                                                                 };

    /** Checks if task can run, given the tasks in tasks.
     * 
     * @param task
     * @param tasks
     * @return */
    private static boolean canRun(IAIRunnable task, ArrayList<IAIRunnable> tasks)
    {
        int prior = task.getPriority();
        int mutex = task.getMutex();
        for (int i = 0; i < tasks.size(); i++)
        {
            IAIRunnable ai = tasks.get(i);
            if (ai.getPriority() < prior && (mutex & ai.getMutex()) != 0 && ai.shouldRun()) { return false; }
        }
        return task.shouldRun();
    }

    @SubscribeEvent
    public void updateGenes(LivingUpdateEvent event)
    {
        IMobGenetics genes = event.getEntity().getCapability(IMobGenetics.GENETICS_CAP, null).orElse(null);
        if (genes != null) genes.onUpdateTick(event.getEntity());
    }
}
