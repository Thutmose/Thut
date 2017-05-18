package thut.mobcapper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = MobCapper.MODID, name = "Mob Capper", version = MobCapper.VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = MobCapper.MCVERSIONS)
public class MobCapper
{
    public static final class LogFormatter extends Formatter
    {
        private static final String SEP        = System.getProperty("line.separator");

        private SimpleDateFormat    dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

        @Override
        public String format(LogRecord record)
        {
            StringBuilder sb = new StringBuilder();

            sb.append(dateFormat.format(record.getMillis()));
            sb.append(" [").append(record.getLevel().getLocalizedName()).append("] ");

            sb.append(record.getMessage());
            sb.append(SEP);
            Throwable thr = record.getThrown();

            if (thr != null)
            {
                StringWriter thrDump = new StringWriter();
                thr.printStackTrace(new PrintWriter(thrDump));
                sb.append(thrDump.toString());
            }

            return sb.toString();
        }
    }

    public static final String MODID      = "mobcapper";
    public static final String VERSION    = "1.0.0";
    public final static String MCVERSIONS = "[1.9.4,1.12)";
    private final Logger       logger     = Logger.getLogger("mobcapper");

    Set<Integer>               ids        = Sets.newHashSet();

    @Mod.Instance("MobCapper")
    public static MobCapper    instance;

    Config                     conf;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        conf = new Config(e);
        if (e.getSide() == Side.CLIENT && !Config.client) return;
        MinecraftForge.EVENT_BUS.register(this);
        logger.setLevel(Level.ALL);
        try
        {
            File logfile = new File(".", "MobLog.log");
            if ((logfile.exists() || logfile.createNewFile()) && logfile.canWrite())
            {
                FileHandler logHandler = new FileHandler(logfile.getPath());
                logHandler.setFormatter(new LogFormatter());
                logger.addHandler(logHandler);
            }
        }
        catch (SecurityException | IOException e1)
        {
            e1.printStackTrace();
        }

    }

    @SubscribeEvent
    public void specialEvent(LivingSpawnEvent evt)
    {
        if (Config.client && evt.getWorld().isRemote) return;

        if (evt.getEntity() instanceof EntityLivingBase && !(evt.getEntity() instanceof EntityPlayer))
        {
            boolean shouldCull = (evt.getEntity() instanceof IMob);
            shouldCull = shouldCull || (evt.getEntity() instanceof IAnimals);
            shouldCull = shouldCull && !(evt.getEntity() instanceof IMerchant);
            shouldCull = shouldCull && (evt.getEntity().isNonBoss());
            boolean tameable = evt.getEntity() instanceof IEntityOwnable;

            if (tameable || !shouldCull)
            {
                if (!tameable)
                {
                    log(false, evt);
                    return;
                }
                IEntityOwnable tame = (IEntityOwnable) evt.getEntity();
                if (tame.getOwnerId() != null)
                {
                    log(false, evt);
                    return;
                }
            }
            World world = evt.getWorld();
            AxisAlignedBB box = new AxisAlignedBB(evt.getX(), evt.getY(), evt.getZ(), evt.getX(), evt.getY(),
                    evt.getZ());
            List<Entity> l;
            List<EntityPlayer> l2 = world.getEntitiesWithinAABB(EntityPlayer.class,
                    box.expand(Config.number2, Config.number2, Config.number2));

            boolean player = !l2.isEmpty();
            if (player)
            {
                l = world.getEntitiesWithinAABB(evt.getEntity().getClass(),
                        box.expand(Config.number2, Config.number2, Config.number2));
            }
            else
            {
                if (evt.isCancelable()) evt.setCanceled(true);
                if (evt.hasResult()) evt.setResult(Result.DENY);
                log(true, evt);
                evt.getEntity().setDead();
                return;
            }

            if (!player || l.size() > Config.number)
            {
                if (evt.isCancelable()) evt.setCanceled(true);
                if (evt.hasResult()) evt.setResult(Result.DENY);
                log(true, evt);
                evt.getEntity().setDead();
                return;
            }
            log(false, evt);
        }
    }

    void log(boolean cull, LivingSpawnEvent evt)
    {
        if (!cull)
        {
            if (Config.logspawns)
            {
                if (!ids.contains(evt.getEntity().getEntityId()))
                {
                    logger.log(Level.INFO, "|spawn|" + evt.getEntity() + "|" + evt.getEntity().getClass() + "|"
                            + EntityList.getEntityString(evt.getEntity()));
                    ids.add(evt.getEntity().getEntityId());
                }
            }
        }
        else
        {
            if (Config.logCulls)
            {
                if (!ids.contains(evt.getEntity().getEntityId()))
                {
                    logger.log(Level.INFO, "|cull|" + evt.getEntity() + "|" + evt.getEntity().getClass() + "|"
                            + EntityList.getEntityString(evt.getEntity()));
                    ids.add(evt.getEntity().getEntityId());
                }
            }
        }
    }
}
