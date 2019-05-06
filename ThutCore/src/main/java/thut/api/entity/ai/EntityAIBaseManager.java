package thut.api.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import thut.api.entity.ai.AIThreadManager.AIStuff;
import thut.api.entity.genetics.IMobGenetics;

public class EntityAIBaseManager extends EntityAIBase
{
    final IAIMob           wrapped;
    final EntityLivingBase entity;

    public EntityAIBaseManager(IAIMob wrapped, EntityLivingBase mob)
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

        // If not IAIMob, or self managed, then no need to run AI stuff.
        if (wrapped.selfManaged() || ai == null) return;
        // Run the ILogicRunnables on both server and client side.
        for (ILogicRunnable logic : ai.aiLogic)
        {
            logic.doServerTick(entity.getEntityWorld());
        }
        // Run remainder if AI server side only.
        if (!entity.getEntityWorld().isRemote) updateEntityActionState((EntityLiving) entity, ai);
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

    @Override
    public boolean isInterruptible()
    {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean shouldExecute()
    {
        return true;
    }

}
