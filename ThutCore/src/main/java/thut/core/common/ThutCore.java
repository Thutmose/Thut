package thut.core.common;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import thut.api.TickHandler;
import thut.api.block.IOwnableTE;
import thut.api.entity.IMultiplePassengerEntity;
import thut.api.entity.ai.AIThreadManager;
import thut.api.entity.ai.IAIMob;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.network.PacketHandler;
import thut.api.terrain.TerrainManager;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.commands.CommandConfig;
import thut.core.common.commands.CommandTerrain;
import thut.core.common.genetics.DefaultGenetics;
import thut.core.common.handlers.ConfigHandler;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.terrain.CapabilityTerrainAffected;
import thut.core.common.terrain.CapabilityTerrainAffected.DefaultAffected;
import thut.core.common.terrain.CapabilityTerrainAffected.ITerrainAffected;
import thut.core.common.world.mobs.data.DataSync_Impl;
import thut.core.common.world.mobs.data.SyncHandler;
import thut.reference.Reference;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, updateJSON = Reference.UPDATEURL, acceptableRemoteVersions = Reference.MINVERSION, guiFactory = "thut.core.client.config.ModGuiFactory")
public class ThutCore
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

    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.COMMON_PROXY_CLASS)
    public static CommonProxy     proxy;

    @Instance(Reference.MOD_ID)
    public static ThutCore        instance;

    public static final String    modid   = Reference.MOD_ID;
    public static CreativeTabThut tabThut = CreativeTabThut.tabThut;
    public static final Logger    logger  = Logger.getLogger(modid);

    // Configuration Handler that handles the config file
    public ConfigHandler          config;

    public ThutCore()
    {
        initLogger();
    }

    private void initLogger()
    {
        FileHandler logHandler = null;
        logger.setLevel(Level.ALL);
        try
        {
            File logs = new File("." + File.separator + "logs");
            logs.mkdirs();
            File logfile = new File(logs, modid + ".log");
            if ((logfile.exists() || logfile.createNewFile()) && logfile.canWrite() && logHandler == null)
            {
                logHandler = new FileHandler(logfile.getPath());
                logHandler.setFormatter(new LogFormatter());
                logger.addHandler(logHandler);
            }
        }
        catch (SecurityException | IOException e)
        {
            e.printStackTrace();
        }
        AIThreadManager.logger = logger;
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        proxy.initClient();
        proxy.registerEntities();
        proxy.registerTEs();
        new CapabilityTerrainAffected();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        proxy.preinit(e);
        config = new ConfigHandler(e.getSuggestedConfigurationFile());
        proxy.loadSounds();
        DataSerializers.registerSerializer(IMultiplePassengerEntity.SEATSERIALIZER);
        AIThreadManager.AIThread.threadCount = config.threadCount;
        if (config.multithreadedAI) AIThreadManager.AIThread.createThreads();
        AIThreadManager aiTicker = new AIThreadManager();
        MinecraftForge.EVENT_BUS.register(aiTicker);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new TickHandler());
        MinecraftForge.EVENT_BUS.register(new SyncHandler());

        CapabilityManager.INSTANCE.register(IAIMob.class, new Capability.IStorage<IAIMob>()
        {
            @Override
            public NBTBase writeNBT(Capability<IAIMob> capability, IAIMob instance, EnumFacing side)
            {
                if (instance instanceof INBTSerializable<?>) { return INBTSerializable.class.cast(instance)
                        .serializeNBT(); }
                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void readNBT(Capability<IAIMob> capability, IAIMob instance, EnumFacing side, NBTBase nbt)
            {
                if (instance instanceof INBTSerializable<?>)
                {
                    INBTSerializable.class.cast(instance).deserializeNBT(nbt);
                }
            }
        }, IAIMob.Default::new);
        CapabilityManager.INSTANCE.register(ITerrainAffected.class, new Capability.IStorage<ITerrainAffected>()
        {
            @Override
            public NBTBase writeNBT(Capability<ITerrainAffected> capability, ITerrainAffected instance, EnumFacing side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<ITerrainAffected> capability, ITerrainAffected instance, EnumFacing side,
                    NBTBase nbt)
            {
            }
        }, DefaultAffected::new);
        CapabilityManager.INSTANCE.register(DataSync.class, new Capability.IStorage<DataSync>()
        {
            @Override
            public NBTBase writeNBT(Capability<DataSync> capability, DataSync instance, EnumFacing side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<DataSync> capability, DataSync instance, EnumFacing side, NBTBase nbt)
            {
            }
        }, DataSync_Impl::new);
        CapabilityManager.INSTANCE.register(IMobGenetics.class, new Capability.IStorage<IMobGenetics>()
        {

            @Override
            public NBTBase writeNBT(Capability<IMobGenetics> capability, IMobGenetics instance, EnumFacing side)
            {
                NBTTagList genes = new NBTTagList();
                for (Map.Entry<ResourceLocation, Alleles> entry : instance.getAlleles().entrySet())
                {
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setString("K", entry.getKey().toString());
                    tag.setTag("V", entry.getValue().save());
                    genes.appendTag(tag);
                }
                return genes;
            }

            @Override
            public void readNBT(Capability<IMobGenetics> capability, IMobGenetics instance, EnumFacing side,
                    NBTBase nbt)
            {
                NBTTagList list = (NBTTagList) nbt;
                for (int i = 0; i < list.tagCount(); i++)
                {
                    NBTTagCompound tag = list.getCompoundTagAt(i);
                    Alleles alleles = new Alleles();
                    ResourceLocation key = new ResourceLocation(tag.getString("K"));
                    try
                    {
                        alleles.load(tag.getCompoundTag("V"));
                        instance.getAlleles().put(key, alleles);
                    }
                    catch (Exception e)
                    {
                        System.err.println("Error with " + key);
                    }
                }

            }
        }, DefaultGenetics::new);
        TerrainManager.getInstance();
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandConfig("thutcoresettings", config));
        event.registerServerCommand(new CommandTerrain());
    }

    @EventHandler
    public void serverLoad(FMLServerStoppedEvent event)
    {
        PlayerDataHandler.clear();
        AIThreadManager.clear();
        if (config.autoBlacklistErroredTEs)
        {
            if (config.teblacklist.length != IBlockEntity.TEBLACKLIST.size())
            {
                config.teblacklist = IBlockEntity.TEBLACKLIST.toArray(new String[0]);
                Arrays.sort(config.teblacklist);
                config.get("blockentity", "teblacklist", config.teblacklist).set(config.teblacklist);
                config.save();
            }
        }
    }

    @SubscribeEvent
    public void PlayerLoggedInEvent(PlayerLoggedInEvent event)
    {
        PacketHandler.sendTerrainValues((EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public void BreakBlock(BreakEvent evt)
    {
        EntityPlayer player = evt.getPlayer();
        TileEntity tile = evt.getWorld().getTileEntity(evt.getPos());
        if (tile instanceof IOwnableTE)
        {
            IOwnableTE te = (IOwnableTE) tile;
            if (!te.canEdit(player))
            {
                evt.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public void ExplosionEvent(ExplosionEvent.Detonate evt)
    {
        List<BlockPos> toRemove = Lists.newArrayList();
        for (BlockPos pos : evt.getAffectedBlocks())
        {
            TileEntity tile = evt.getWorld().getTileEntity(pos);
            if (tile instanceof IOwnableTE)
            {
                NBTTagCompound tag = tile.writeToNBT(new NBTTagCompound());
                if (tag.hasKey("admin") && tag.getBoolean("admin"))
                {
                    toRemove.add(pos);
                }
            }
        }
        evt.getAffectedBlocks().removeAll(toRemove);
    }
}