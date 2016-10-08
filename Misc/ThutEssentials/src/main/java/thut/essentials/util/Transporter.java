package thut.essentials.util;

import javax.vecmath.Vector3f;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Transporter
{
    @SuppressWarnings("serial")
    public static class Vector3 extends Vector3f
    {

        public Vector3(double x, double y, double z)
        {
            super((float) x, (float) y, (float) z);
        }

        public Vector3(BlockPos moveTo)
        {
            this(moveTo.getX(), moveTo.getY(), moveTo.getZ());
        }

        public int intY()
        {
            return MathHelper.floor_double(y);
        }

        public int intX()
        {
            return MathHelper.floor_double(x);
        }

        public int intZ()
        {
            return MathHelper.floor_double(z);
        }

        public AxisAlignedBB getAABB()
        {
            return new AxisAlignedBB(x, y, z, x, y, z);
        }
    }

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
            this.loc = new Vector3(x, y, z);
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

    public static class ReMounter
    {
        final Entity theEntity;
        final Entity theMount;
        final int    tick;

        public ReMounter(Entity entity, Entity mount)
        {
            theEntity = entity;
            theMount = mount;
            tick = entity.ticksExisted;
        }

        @SubscribeEvent
        public void tick(LivingUpdateEvent evt)
        {
            int diff = theEntity.ticksExisted - tick;
            if (theEntity.isDead) MinecraftForge.EVENT_BUS.unregister(this);
            if (evt.getEntity().getUniqueID().equals(theEntity.getUniqueID()) && diff > 10)
            {
                // TODO fix this remounter.
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }

    public static Entity teleportEntity(Entity entity, Vector3 t2, int dimension)
    {
        if (entity.isRiding())
        {
            entity.dismountRidingEntity();
        }
        if (dimension != entity.dimension)
        {
            entity = transferToDimension(entity, t2, dimension);
            for (Entity e : entity.getRecursivePassengers())
            {
                transferToDimension(e, t2, dimension);
            }
        }
        int x = t2.intX() >> 4;
        int z = t2.intZ() >> 4;
        for (int i = x - 1; i <= x + 1; i++)
            for (int j = z - 1; j <= z + 1; j++)
            {
                entity.worldObj.getChunkFromChunkCoords(x, z);
            }
        entity.setPositionAndUpdate(t2.x, t2.y, t2.z);
        WorldServer world = entity.getServer().worldServerForDimension(dimension);
        EntityTracker tracker = world.getEntityTracker();
        if (tracker.getTrackingPlayers(entity).getClass().getSimpleName().equals("EmptySet"))
        {
            tracker.trackEntity(entity);
            if (entity instanceof EntityPlayerMP)
            {
                EntityPlayerMP playerIn = (EntityPlayerMP) entity;
                tracker.updateVisibility(playerIn);
            }
        }
        return entity;
    }

    // From RFTools.
    private static Entity transferToDimension(Entity entity, Vector3 t2, int dimension)
    {
        int oldDimension = entity.worldObj.provider.getDimension();

        if (oldDimension == dimension) return entity;

        MinecraftServer server = entity.worldObj.getMinecraftServer();
        WorldServer worldServer = server.worldServerForDimension(dimension);
        Teleporter teleporter = new TTeleporter(worldServer, t2.x, t2.y, t2.z);
        if (!(entity instanceof EntityPlayerMP))
        {
            server.getPlayerList().transferEntityToWorld(entity, dimension, (WorldServer) entity.worldObj, worldServer,
                    teleporter);
            return entity;
        }
        EntityPlayerMP entityPlayerMP = (EntityPlayerMP) entity;
        entityPlayerMP.addExperienceLevel(0);
        worldServer.getMinecraftServer().getPlayerList().transferPlayerToDimension(entityPlayerMP, dimension,
                teleporter);
        if (oldDimension == 1)
        {
            // For some reason teleporting out of the end does weird things.
            worldServer.spawnEntityInWorld(entityPlayerMP);
            worldServer.updateEntityWithOptionalForce(entityPlayerMP, false);
        }
        return entityPlayerMP;
    }
}
