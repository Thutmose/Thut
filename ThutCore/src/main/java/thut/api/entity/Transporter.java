package thut.api.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thut.api.maths.Vector3;

public class Transporter
{
    public static class TelDestination
    {

        final World   dim;

        final double  x;

        final double  y;

        final double  z;
        final Vector3 loc;
        final int     xOff;
        final int     yOff;

        final int     zOff;

        public TelDestination(int dim, Vector3 loc)
        {
            this(FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dim), loc.getAABB(),
                    loc.x, loc.y, loc.z, loc.intX(), loc.intY(), loc.intZ());
        }

        public TelDestination(World _dim, AxisAlignedBB srcBox, double _x, double _y, double _z, int tileX, int tileY,
                int tileZ)
        {
            this.dim = _dim;
            this.x = Math.min(srcBox.maxX - 0.5, Math.max(srcBox.minX + 0.5, _x + tileX));
            this.y = Math.min(srcBox.maxY - 0.5, Math.max(srcBox.minY + 0.5, _y + tileY));
            this.z = Math.min(srcBox.maxZ - 0.5, Math.max(srcBox.minZ + 0.5, _z + tileZ));
            this.xOff = tileX;
            this.yOff = tileY;
            this.zOff = tileZ;
            this.loc = Vector3.getNewVector().set(x, y, z);
        }

        public TelDestination(World _dim, Vector3 loc)
        {
            this(_dim, loc.getAABB(), loc.x, loc.y, loc.z, loc.intX(), loc.intY(), loc.intZ());
        }
    }

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

    public static Entity teleportEntity(Entity entity, Vector3 t2, int dimension, boolean destBlocked)
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
    private static Entity teleportToDimension(Entity entity, Vector3 t2, int dimension, boolean destBlocked)
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
        return null;
    }
}
