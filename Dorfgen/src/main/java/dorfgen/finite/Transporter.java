package dorfgen.finite;

import javax.vecmath.Vector3f;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class Transporter
{
    // From RFTools.
    public static class TTeleporter extends Teleporter
    {
        private final WorldServer worldServerInstance;

        private double            x;
        private double            y;
        private double            z;

        public TTeleporter(WorldServer world, double x, double y, double z)
        {
            super(world);
            this.worldServerInstance = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void placeInPortal(Entity pEntity, float rotationYaw)
        {
            this.worldServerInstance.getBlockState(new BlockPos((int) this.x, (int) this.y, (int) this.z));
            pEntity.setPosition(this.x, this.y, this.z);
            pEntity.motionX = 0.0f;
            pEntity.motionY = 0.0f;
            pEntity.motionZ = 0.0f;
        }

    }

    public static Entity teleportEntity(Entity entity, Vector3f t2, int dimension, boolean destBlocked)
    {
        if (dimension != entity.dimension)
        {
            return teleportToDimension(entity, t2, dimension, destBlocked);
        }
        else if (entity instanceof EntityPlayer)
        {
            entity.setPositionAndUpdate(t2.x, t2.y, t2.z);
            return entity;
        }
        return entity;
    }

    // From RFTools.
    private static Entity teleportToDimension(Entity entity, Vector3f t2, int dimension, boolean destBlocked)
    {
        int oldDimension = entity.worldObj.provider.getDimension();
        EntityPlayerMP entityPlayerMP = (EntityPlayerMP) entity;
        MinecraftServer server = ((EntityPlayerMP) entity).worldObj.getMinecraftServer();
        WorldServer worldServer = server.worldServerForDimension(dimension);
        entityPlayerMP.addExperienceLevel(0);

        worldServer.getMinecraftServer().getPlayerList().transferPlayerToDimension(entityPlayerMP, dimension,
                new TTeleporter(worldServer, t2.x, t2.y, t2.z));
        entityPlayerMP.setPositionAndUpdate(t2.x, t2.y, t2.z);
        if (oldDimension == 1)
        {
            // For some reason teleporting out of the end does weird things.
            entityPlayerMP.setPositionAndUpdate(t2.x, t2.y, t2.z);
            worldServer.spawnEntityInWorld(entityPlayerMP);
            worldServer.updateEntityWithOptionalForce(entityPlayerMP, false);
        }
        return entityPlayerMP;
    }
}
