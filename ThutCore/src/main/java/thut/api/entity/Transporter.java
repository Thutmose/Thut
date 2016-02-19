package thut.api.entity;

import java.util.Iterator;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.network.ForgeMessage;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;
import thut.api.network.PacketHandler.MessageClient;

public class Transporter
{

    public static class TelDestination
    {

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

        public TelDestination(int dim, Vector3 loc)
        {
            this(FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dim), loc.getAABB(),
                    loc.x, loc.y, loc.z, loc.intX(), loc.intY(), loc.intZ());
        }

        final World  dim;
        final double x;
        final double y;
        final double z;

        final Vector3 loc;

        final int xOff;
        final int yOff;
        final int zOff;
    }

    /** Mostly from dimensional doors.. which mostly got it form X-Comp.
     *
     * @param entity
     *            to be teleported entity
     * @param link
     *            destination
     * @return teleported entity */
    public static Entity teleportEntity(Entity entity, TelDestination link)
    {
        WorldServer oldWorld, newWorld;
        EntityPlayerMP player;

        try
        {
            oldWorld = (WorldServer) entity.worldObj;
            newWorld = (WorldServer) link.dim;
            player = (entity instanceof EntityPlayerMP) ? (EntityPlayerMP) entity : null;
        }
        catch (Throwable e)
        {
            return entity;
        }

        if (oldWorld == null) return entity;
        if (newWorld == null) return entity;

        // Is something riding? Handle it first.
        if (entity.riddenByEntity != null) { return teleportEntity(entity.riddenByEntity, link); }
        // Are we riding something? Dismount and tell the mount to go first.
        Entity cart = entity.ridingEntity;
        if (cart != null)
        {
            entity.mountEntity(null);
            cart = teleportEntity(cart, link);
            // We keep track of both so we can remount them on the other side.
        }

        // load the chunk!
        WorldServer.class.cast(newWorld).getChunkProvider().provideChunk(MathHelper.floor_double(link.x) >> 4,
                MathHelper.floor_double(link.z) >> 4);

        boolean diffDestination = newWorld != oldWorld;
        // if ( diffDestination )
        {
            if (player != null) // && diffDestination)
            {
                if (diffDestination) player.mcServer.getConfigurationManager().transferPlayerToDimension(player,
                        link.dim.provider.getDimensionId(), new METeleporter(newWorld, link));
                else teleportWithinDimension(player, link.loc, false);
            }
            else
            {
                int entX = entity.chunkCoordX;
                int entZ = entity.chunkCoordZ;

                if ((entity.addedToChunk) && (oldWorld.getChunkProvider().chunkExists(entX, entZ)))
                {
                    oldWorld.getChunkFromChunkCoords(entX, entZ).removeEntity(entity);
                    oldWorld.getChunkFromChunkCoords(entX, entZ).setChunkModified();// .isModified
                                                                                    // =
                                                                                    // true;
                }

                Entity newEntity = EntityList.createEntityByName(EntityList.getEntityString(entity), newWorld);
                if (newEntity != null)
                {
                    entity.lastTickPosX = entity.prevPosX = entity.posX = link.x;
                    entity.lastTickPosY = entity.prevPosY = entity.posY = link.y;
                    entity.lastTickPosZ = entity.prevPosZ = entity.posZ = link.z;

                    if (entity instanceof EntityHanging)
                    {
                        // EntityHanging h = (EntityHanging) entity;
                        // h.field_146063_b += link.xOff;
                        // h.field_146064_c += link.yOff;//TODO get frames
                        // teleporting if needed
                        // h.field_146062_d += link.zOff;
                    }

                    newEntity.copyDataFromOld(entity);

                    newEntity.dimension = newWorld.provider.getDimensionId();
                    newEntity.forceSpawn = true;

                    entity.isDead = true;
                    entity = newEntity;
                }
                else return null;

                // myChunk.addEntity( entity );
                // newWorld.loadedEntityList.add( entity );
                // newWorld.onEntityAdded( entity );
                newWorld.spawnEntityInWorld(entity);
            }
        }

        entity.worldObj.updateEntityWithOptionalForce(entity, false);

        if (cart != null)
        {
            if (player != null) entity.worldObj.updateEntityWithOptionalForce(entity, true);

            entity.mountEntity(cart);
        }

        return entity;
    }

    static class METeleporter extends Teleporter
    {

        final TelDestination destination;

        public METeleporter(WorldServer par1WorldServer, TelDestination d)
        {
            super(par1WorldServer);
            this.destination = d;
        }

        @Override
        public void placeInPortal(Entity par1Entity, float par8)
        {
            par1Entity.setLocationAndAngles(this.destination.x, this.destination.y, this.destination.z,
                    par1Entity.rotationYaw, 0.0F);
            par1Entity.motionX = par1Entity.motionY = par1Entity.motionZ = 0.0D;
        }

        @Override
        public boolean makePortal(Entity par1Entity)
        {
            return false;
        }

        @Override
        public boolean placeInExistingPortal(Entity par1Entity, float par8)
        {
            return false;
        }

        @Override
        public void removeStalePortalLocations(long par1)
        {

        }

    }

    public static Entity teleportEntityAndRider(Entity entity, Vector3 t2, int dimension, boolean destBlocked)
    {

        Entity rider = entity.riddenByEntity;
        if (rider != null)
        {
            rider.mountEntity(null);
            rider = teleportEntityAndRider(rider, t2, dimension, destBlocked);
        }
        entity = teleportEntity(entity, t2, dimension, destBlocked);
        if (entity != null && !entity.isDead && rider != null && !rider.isDead)
        {
            rider.mountEntity(entity);
        }
        return entity;
    }

    public static Entity teleportEntity(Entity entity, Vector3 t2, int dimension, boolean destBlocked)
    {
        Entity newEntity = null;

        if (!destBlocked)
        {
            if (entity.dimension == dimension) newEntity = teleportWithinDimension(entity, t2, destBlocked);
            else
            {
                newEntity = teleportToOtherDimension(entity, t2, dimension, destBlocked);
                if (newEntity != null) newEntity.dimension = dimension;
            }
        }
        else
        {

        }
        return newEntity;
    }

    static Entity teleportWithinDimension(Entity entity, Vector3 p, boolean destBlocked)
    {
        if (entity instanceof EntityPlayerMP) return teleportPlayerWithinDimension((EntityPlayerMP) entity, p);
        else return teleportEntityToWorld(entity, p, (WorldServer) entity.worldObj, destBlocked);
    }

    static Entity teleportPlayerWithinDimension(EntityPlayerMP entity, Vector3 p)
    {
        entity.setPositionAndUpdate(p.x, p.y, p.z);
        entity.worldObj.updateEntityWithOptionalForce(entity, false);
        return entity;
    }

    static Entity teleportToOtherDimension(Entity entity, Vector3 p, int dimension, boolean destBlocked)
    {
        if (entity instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            transferPlayerToDimension(player, dimension, p);
            return player;
        }
        else return teleportEntityToDimension(entity, p, dimension, destBlocked);
    }

    static void sendDimensionRegister(EntityPlayerMP player, int dimensionID)
    {
        int providerID = DimensionManager.getProviderType(dimensionID);
        ForgeMessage msg = new ForgeMessage.DimensionRegisterMessage(dimensionID, providerID);
        FMLEmbeddedChannel channel = NetworkRegistry.INSTANCE.getChannel("FORGE", Side.SERVER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        channel.writeAndFlush(msg).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    static void transferPlayerToDimension(EntityPlayerMP player, int newDimension, Vector3 p)
    {
        MinecraftServer server = MinecraftServer.getServer();
        ServerConfigurationManager scm = server.getConfigurationManager();
        int oldDimension = player.dimension;
        player.dimension = newDimension;
        WorldServer oldWorld = server.worldServerForDimension(oldDimension);
        WorldServer newWorld = server.worldServerForDimension(newDimension);
        sendDimensionRegister(player, newDimension);
        player.closeScreen();
        player.playerNetServerHandler.sendPacket(new S07PacketRespawn(player.dimension, player.worldObj.getDifficulty(),
                newWorld.getWorldInfo().getTerrainType(), player.theItemInWorldManager.getGameType()));

        oldWorld.removePlayerEntityDangerously(player); // Removes player right
                                                        // now instead of
                                                        // waiting for next tick
        player.isDead = false;
        player.setLocationAndAngles(p.x, p.y, p.z, player.rotationYaw, player.rotationPitch);
        newWorld.spawnEntityInWorld(player);
        player.setWorld(newWorld);
        scm.preparePlayer(player, oldWorld);
        player.playerNetServerHandler.setPlayerLocation(p.x, p.y, p.z, player.rotationYaw, player.rotationPitch);
        player.theItemInWorldManager.setWorld(newWorld);
        scm.updateTimeAndWeatherForPlayer(player, newWorld);
        scm.syncPlayerInventory(player);
        Iterator<?> var6 = player.getActivePotionEffects().iterator();
        while (var6.hasNext())
        {
            PotionEffect effect = (PotionEffect) var6.next();
            player.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), effect));
        }
        player.playerNetServerHandler.sendPacket(
                new S1FPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel));
        FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, oldDimension, newDimension);

    }

    static Entity teleportEntityToDimension(Entity entity, Vector3 p, int dimension, boolean destBlocked)
    {
        MinecraftServer server = MinecraftServer.getServer();
        WorldServer world = server.worldServerForDimension(dimension);
        return teleportEntityToWorld(entity, p, world, destBlocked);
    }

    static Entity teleportEntityToWorld(Entity oldEntity, Vector3 p, WorldServer newWorld, boolean destBlocked)
    {
        Vector3 v = Vector3.getNewVector().setToVelocity(oldEntity);
        WorldServer oldWorld = (WorldServer) oldEntity.worldObj;
        NBTTagCompound nbt = new NBTTagCompound();
        oldEntity.writeToNBT(nbt);
        extractEntityFromWorld(oldWorld, oldEntity);
        if (destBlocked)
        {
            if (!(oldEntity instanceof EntityLivingBase)) { return null; }
        }
        Entity newEntity = instantiateEntityFromNBT(oldEntity.getClass(), nbt, newWorld);
        if (newEntity != null)
        {
            if (oldEntity instanceof EntityLiving)
                copyMoreEntityData((EntityLiving) oldEntity, (EntityLiving) newEntity);
            setVelocity(newEntity, v);
            newEntity.setLocationAndAngles(p.x, p.y, p.z, oldEntity.rotationYaw, oldEntity.rotationPitch);
            checkChunk(newWorld, newEntity);
            newEntity.forceSpawn = true; // Force spawn packet to be sent as
                                         // soon as possible
            oldEntity.setDead();

            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeByte(MessageClient.TELEPORTID);
            buffer.writeInt(oldEntity.getEntityId());

            MessageClient message = new MessageClient(buffer);
            PacketHandler.sendToAllNear(message, v.set(oldEntity), oldEntity.dimension, 100);

            newWorld.spawnEntityInWorld(newEntity);
            newEntity.setWorld(newWorld);

        }
        oldWorld.resetUpdateEntityTick();
        if (oldWorld != newWorld) newWorld.resetUpdateEntityTick();
        return newEntity;
    }

    static Entity instantiateEntityFromNBT(Class<? extends Entity> cls, NBTTagCompound nbt, WorldServer world)
    {
        try
        {
            Entity entity = (Entity) cls.getConstructor(World.class).newInstance(world);
            entity.readFromNBT(nbt);
            return entity;
        }
        catch (Exception e)
        {
            System.out.printf("Could not instantiate %s: %s\n", cls, e);
            e.printStackTrace();
            return null;
        }
    }

    static void copyMoreEntityData(EntityLiving oldEntity, EntityLiving newEntity)
    {
        float s = oldEntity.getAIMoveSpeed();
        if (s != 0) newEntity.setAIMoveSpeed(s);
    }

    static void setVelocity(Entity entity, Vector3 v)
    {
        entity.motionX = v.x;
        entity.motionY = v.y;
        entity.motionZ = v.z;
    }

    static void extractEntityFromWorld(World world, Entity entity)
    {
        // Immediately remove entity from world without calling setDead(), which
        // has
        // undesirable side effects on some entities.
        if (entity instanceof EntityPlayer)
        {
            world.playerEntities.remove(entity);
            world.updateAllPlayersSleepingFlag();
        }
        int i = entity.chunkCoordX;
        int j = entity.chunkCoordZ;
        if (entity.addedToChunk && world.getChunkProvider().chunkExists(i, j))
            world.getChunkFromChunkCoords(i, j).removeEntity(entity);
        world.loadedEntityList.remove(entity);
        ((WorldServer) world).getEntityTracker().untrackEntity(entity);
    }

    static void checkChunk(World world, Entity entity)
    {
        int cx = MathHelper.floor_double(entity.posX / 16.0D);
        int cy = MathHelper.floor_double(entity.posZ / 16.0D);
        world.getChunkFromChunkCoords(cx, cy);
    }
}
