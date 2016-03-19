package thut.mobcapper;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = MobCapper.MODID, name = "Mob Capper", version = MobCapper.VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = MobCapper.MCVERSIONS)
public class MobCapper
{
    public static final String MODID      = "mobcapper";
    public static final String VERSION    = "1.0.0";
    public final static String MCVERSIONS = "[1.8.8,1.8.9]";

    @Mod.Instance("MobCapper")
    public static MobCapper instance;

    Config conf;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        MinecraftForge.EVENT_BUS.register(this);
        conf = new Config(e);

    }

    @SubscribeEvent
    public void specialEvent(LivingSpawnEvent evt)
    {
        if (evt.world.isRemote) return;
        if (evt.entity instanceof EntityLivingBase && !(evt.entity instanceof EntityPlayer))
        {
            boolean shouldCull = (evt.entity instanceof IMob);
            shouldCull = shouldCull || (evt.entity instanceof IAnimals);
            shouldCull = shouldCull && !(evt.entity instanceof IMerchant);
            shouldCull = shouldCull && !(evt.entity instanceof IBossDisplayData);
            boolean tameable = evt.entity instanceof EntityTameable;

            if (tameable || !shouldCull)
            {
                if (!tameable) return;
                EntityTameable tame = (EntityTameable) evt.entity;
                if (tame.isTamed()) { return; }
            }
            World world = evt.world;
            AxisAlignedBB box = new AxisAlignedBB(evt.x, evt.y, evt.z, evt.x, evt.y, evt.z);
            List<Entity> l;
            List<EntityPlayer> l2 = world.getEntitiesWithinAABB(EntityPlayer.class,
                    box.expand(Config.number2, Config.number2, Config.number2));

            boolean player = !l2.isEmpty();
            if (player)
            {
                l = world.getEntitiesWithinAABB(evt.entity.getClass(),
                        box.expand(Config.number2, Config.number2, Config.number2));
            }
            else
            {
                if (evt.isCancelable()) evt.setCanceled(true);
                if (evt.hasResult()) evt.setResult(Result.DENY);
                return;
            }

            if (!player || l.size() > Config.number)
            {
                if (evt.isCancelable()) evt.setCanceled(true);
                if (evt.hasResult()) evt.setResult(Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public void joinEvent(EntityJoinWorldEvent evt)
    {
        if (evt.world.isRemote) return;
        if (evt.entity instanceof EntityLivingBase && !(evt.entity instanceof EntityPlayer))
        {
            boolean shouldCull = (evt.entity instanceof IMob);
            shouldCull = shouldCull || (evt.entity instanceof IAnimals);
            shouldCull = shouldCull && !(evt.entity instanceof IMerchant);
            shouldCull = shouldCull && !(evt.entity instanceof IBossDisplayData);
            boolean tameable = evt.entity instanceof EntityTameable;
            boolean ownable = evt.entity instanceof IEntityOwnable;
            if (tameable || !shouldCull)
            {
                if (!tameable) return;
                EntityTameable tame = (EntityTameable) evt.entity;
                if (tame.isTamed()) { return; }
            }
            if (ownable || !shouldCull)
            {
                if (!ownable) return;
                IEntityOwnable tame = (IEntityOwnable) evt.entity;
                if (tame.getOwner() != null) { return; }
            }

            World world = evt.world;
            AxisAlignedBB box = new AxisAlignedBB(evt.entity.posX, evt.entity.posY, evt.entity.posZ, evt.entity.posX,
                    evt.entity.posY, evt.entity.posZ);
            List<? extends Entity> l;
            List<EntityPlayer> l2 = world.getEntitiesWithinAABB(EntityPlayer.class,
                    box.expand(Config.number2, Config.number2, Config.number2));

            boolean player = !l2.isEmpty();
            if (player)
            {
                l = world.getEntitiesWithinAABB(evt.entity.getClass(),
                        box.expand(Config.number2, Config.number2, Config.number2));
            }
            else
            {
                if (evt.isCancelable()) evt.setCanceled(true);
                if (evt.hasResult()) evt.setResult(Result.DENY);
                return;
            }

            if (!player || l.size() > Config.number)
            {
                if (evt.isCancelable()) evt.setCanceled(true);
                if (evt.hasResult()) evt.setResult(Result.DENY);
            }
        }
    }
}
