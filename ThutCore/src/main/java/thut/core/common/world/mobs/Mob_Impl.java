package thut.core.common.world.mobs;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import thut.api.world.World;
import thut.api.world.mobs.Mob;
import thut.api.world.mobs.ai.AI;
import thut.api.world.utils.Info;
import thut.api.world.utils.Vector;
import thut.core.common.world.WorldManager;
import thut.core.common.world.mobs.ai.AI_Impl;
import thut.core.common.world.utils.Info_Impl;
import thut.core.common.world.utils.Vector_D;
import thut.core.common.world.utils.Vector_I;

public class Mob_Impl implements Mob, ICapabilityProvider
{
    World           world;
    Entity          entity;
    String          key;
    final AI_Impl   ai       = new AI_Impl();
    final Info_Impl info     = new Info_Impl();
    final Vector_I  worldPos = new Vector_I();
    final Vector_D  position = new Vector_D();
    final Vector_D  velocity = new Vector_D();

    public void setEntity(Entity entity)
    {
        if (this.world != null) this.world.removeMob(this);
        this.entity = entity;
        this.world = WorldManager.instance().getWorld(this.entity.dimension);
        this.world.addMob(this);
        this.key = EntityList.getEntityString(entity);
    }

    @Override
    public World world()
    {
        return world;
    }

    @Override
    public void setWorld(World world)
    {
        this.world = world;
    }

    @Override
    public Vector<Integer> worldPos()
    {
        ((Vector_D) position()).toInts(worldPos);
        return worldPos;
    }

    @Override
    public Vector<Double> position()
    {
        position.setValue(0, entity.posX);
        position.setValue(1, entity.posY);
        position.setValue(2, entity.posZ);
        return position;
    }

    @Override
    public Vector<Double> velocity()
    {
        velocity.setValue(0, entity.motionX);
        velocity.setValue(1, entity.motionY);
        velocity.setValue(2, entity.motionZ);
        return velocity;
    }

    @Override
    public Info info()
    {
        return info;
    }

    @Override
    public UUID id()
    {
        return entity.getUniqueID();
    }

    @Override
    public void setID(UUID id)
    {
        entity.setUniqueId(id);
    }

    @Override
    public boolean onClient()
    {
        return entity.getEntityWorld().isRemote;
    }

    @Override
    public boolean inWorld()
    {
        return entity.addedToChunk;
    }

    @Override
    public AI getAI()
    {
        return ai;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, Direction facing)
    {
        return entity.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction facing)
    {
        return entity.getCapability(capability, facing);
    }

    @Override
    public boolean isDead()
    {
        return entity.isDead;
    }

    @Override
    public String key()
    {
        return key;
    }

    @Override
    public Object wrapped()
    {
        return entity;
    }

    @Override
    public void setDead()
    {
        this.entity.setDead();
    }

    @Override
    public float getMaxHealth()
    {
        float num = 0;
        if (this.entity instanceof MobEntity) num = ((MobEntity) entity).getMaxHealth();
        return num;
    }

    @Override
    public void setHealth(float health)
    {
        if (this.entity instanceof MobEntity) ((MobEntity) this.entity).setHealth(health);
    }

}
