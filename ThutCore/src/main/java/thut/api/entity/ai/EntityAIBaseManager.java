package thut.api.entity.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.World;
import thut.api.entity.ai.AIThreadManager.AIStuff;
import thut.api.entity.genetics.IMobGenetics;

public class EntityAIBaseManager extends EntityAIBase
{
    final IAIMob           wrapped;
    final LivingEntity entity;

    public EntityAIBaseManager(IAIMob wrapped, LivingEntity mob)
    {
        this.wrapped = wrapped;
        this.entity = mob;
    }

    @Override
    public void updateTask()
    {
        AIStuff ai = wrapped.getAI();
        IMobGenetics genes = entity.getCapability(IMobGenetics.GENETICS_CAP, null);
        if (genes != null) genes.onUpdateTick(entity);
        World world = entity.getEntityWorld();

        // If not IAIMob, or self managed, then no need to run AI stuff.
        if (wrapped.selfManaged() || ai == null) return;
        world.profiler.startSection("custom_ai");
        // Run the ILogicRunnables on both server and client side.
        for (ILogicRunnable logic : ai.aiLogic)
        {
            logic.doServerTick(world);
        }
        // Run Tick results from AI stuff.
        ai.runServerThreadTasks(world);
        world.profiler.endSection();
    }

    protected void updateEntityActionState(MobEntity mob, AIStuff ai)
    {
    }

    @Override
    public boolean isInterruptible()
    {
        return false;
    }

    @Override
    public boolean shouldExecute()
    {
        return true;
    }

}
