package thut.api.entity.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import thut.api.TickHandler;
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
        public final EntityLiving              entity;
        public final ArrayList<IAIRunnable>    aiTasks  = new ArrayList<IAIRunnable>();
        public final ArrayList<ILogicRunnable> aiLogic  = new ArrayList<ILogicRunnable>();

        public AIStuff(EntityLiving entity_)
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
                if (world.getTotalWorldTime() % tickRate != 0) return;
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

    public static class AIThread extends Thread
    {
        public static int                        threadCount = 1;

        public static HashMap<Integer, AIThread> threads     = Maps.newHashMap();

        public static void createThreads()
        {
            threadCount = Math.min(threadCount, Runtime.getRuntime().availableProcessors());
            aiStuffLists = new Queue[threadCount];
            logger.log(Level.INFO, "Creating and starting " + threadCount + " Mob AI Threads.");
            for (int i = 0; i < threadCount; i++)
            {
                Queue<AIStuff> set = Queues.newConcurrentLinkedQueue();
                AIThread thread = new AIThread(i, set, new Object());
                aiStuffLists[i] = set;
                thread.setPriority(8);
                thread.start();
            }
        }

        public final Queue<AIStuff> aiStuff;
        public final Object         lock;
        final int                   id;

        public AIThread(final int number, final Queue<AIStuff> aiStuff, final Object lock)
        {// TODO see if adding the threadgroup breaks anything...
            super(SidedThreadGroups.SERVER, new Runnable()
            {
                @Override
                public void run()
                {
                    int id;
                    Thread thread = Thread.currentThread();
                    if (!(thread instanceof AIThread))
                    {
                        new ClassCastException("wrong thread type").printStackTrace();
                        return;
                    }
                    id = number;
                    logger.log(Level.INFO, "This is Thread " + id);
                    while (true)
                    {
                        // Wait for the lock to be notified from the main
                        // thread.
                        synchronized (lock)
                        {
                            try
                            {
                                lock.wait();
                            }
                            catch (Exception e)
                            {
                                logger.log(Level.SEVERE, "Error waiting on lock", e);
                            }
                        }
                        // After being notified, run though all of the scheduled
                        // AIStuffs to tick.
                        synchronized (aiStuff)
                        {
                            while (!aiStuff.isEmpty())
                                aiStuff.remove().tick();
                        }
                    }
                }
            });
            this.lock = lock;
            id = number;
            this.aiStuff = aiStuff;
            this.setName("Netty Server IO - Mob AI Thread-" + id);
            threads.put(id, this);
        }

    }

    public static Logger                                           logger;

    /** Lists of the AI stuff for each thread. */
    private static Queue<AIStuff>[]                                aiStuffLists;
    /** Map of dimension to players, used for thread-safe player access. */
    public static final HashMap<Integer, Vector<Object>>           worldPlayers  = new HashMap<Integer, Vector<Object>>();

    public static final ConcurrentHashMap<Integer, Vector<Entity>> worldEntities = new ConcurrentHashMap<Integer, Vector<Entity>>();

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

    /** Clears things for world unload */
    public static void clear()
    {
        /** This can be null if server was started with multithreaded AI
         * disabled. */
        if (aiStuffLists != null) for (Queue v : aiStuffLists)
        {
            v.clear();
        }
        worldPlayers.clear();
        TickHandler.getInstance().worldCaches.clear();
    }

    /** Sets the AIStuff to tick on correct thread. the Queue is locked when
     * reading, so this will wait instead of causing a CME
     * 
     * @param blockEntity
     * @param task */
    public static void scheduleAITick(AIStuff ai)
    {
        if (!ThutCore.instance.config.multithreadedAI) { return; }

        int id = ai.entity.getEntityId() % AIThread.threadCount;
        AIThread thread = AIThread.threads.get(id);
        thread.aiStuff.add(ai);
    }

    @SubscribeEvent
    public void tickEvent(WorldTickEvent evt)
    {
        // At the start, refresh the player lists.
        if (evt.phase == Phase.START)
        {
            Vector players = worldPlayers.get(evt.world.provider.getDimension());
            if (players == null)
            {
                players = new Vector();
            }
            players.clear();
            players.addAll(evt.world.playerEntities);
            worldPlayers.put(evt.world.provider.getDimension(), players);
            Vector<Entity> entities = worldEntities.get(evt.world.provider.getDimension());
            if (entities == null)
            {
                entities = new Vector<Entity>();
            }
            entities.clear();
            entities.addAll(evt.world.loadedEntityList);
            worldEntities.put(evt.world.provider.getDimension(), entities);
        }
    }

    /** AI Ticks at the end of the server tick.
     * 
     * @param evt */
    @SubscribeEvent
    public void tickEventServer(ServerTickEvent evt)
    {
        if (evt.phase == Phase.END)
        {
            // Loop through the threads, and notify the locks. this makes them
            // run the AI Ticks that were scheduled this tick
            for (AIThread thread : AIThread.threads.values())
            {
                try
                {
                    synchronized (thread.lock)
                    {
                        thread.lock.notify();
                    }
                }
                catch (Exception e)
                {
                    logger.log(Level.SEVERE, "Error ticking AI stuff", e);
                }
            }
        }
    }

    @SubscribeEvent
    public void LivingUpdate(LivingUpdateEvent event)
    {
        IAIMob mob = null;
        AIStuff ai = null;
        if (event.getEntity() instanceof IAIMob)
        {
            mob = ((IAIMob) event.getEntity());
            ai = mob.getAI();
        }
        else if (event.getEntity().hasCapability(IAIMob.THUTMOBAI, null))
        {
            mob = event.getEntity().getCapability(IAIMob.THUTMOBAI, null);
            ai = mob.getAI();
        }
        IMobGenetics genes = event.getEntity().getCapability(IMobGenetics.GENETICS_CAP, null);
        if (genes != null) genes.onUpdateTick(event.getEntityLiving());

        // If not IAIMob, or self managed, then no need to run AI stuff.
        if (mob == null || mob.selfManaged() || ai == null) return;
        // Run the ILogicRunnables on both server and client side.
        for (ILogicRunnable logic : ai.aiLogic)
        {
            logic.doServerTick(event.getEntity().getEntityWorld());
        }
        // Run remainder if AI server side only.
        if (!event.getEntity().getEntityWorld().isRemote)
            updateEntityActionState((EntityLiving) event.getEntityLiving(), ai);
    }

    protected void updateEntityActionState(EntityLiving mob, AIStuff ai)
    {
        mob.getEntityWorld().profiler.startSection("custom_ai");
        // Run Tick results from AI stuff.
        ai.runServerThreadTasks(mob.getEntityWorld());
        // Schedule AIStuff to tick for next tick.
        AIThreadManager.scheduleAITick(ai);
        mob.getEntityWorld().profiler.endSection();
    }
}
