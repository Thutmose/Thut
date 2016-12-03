package thut.core.common;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
import net.minecraftforge.fml.relauncher.Side;
import thut.api.TickHandler;
import thut.api.block.IOwnableTE;
import thut.api.entity.ai.AIThreadManager;
import thut.api.entity.ai.AIThreadManager.AIStuff;
import thut.api.entity.ai.IAIMob;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.maths.Cruncher;
import thut.api.network.PacketHandler;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.TerrainManager;
import thut.core.common.commands.ConfigCommand;
import thut.core.common.genetics.DefaultGenetics;
import thut.core.common.handlers.ConfigHandler;
import thut.core.common.handlers.PlayerDataHandler;
import thut.reference.Reference;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, updateJSON = Reference.UPDATEURL, guiFactory = "thut.core.client.config.ModGuiFactory")
public class ThutCore
{

    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.COMMON_PROXY_CLASS)
    public static CommonProxy     proxy;

    @Instance(Reference.MOD_ID)
    public static ThutCore        instance;

    public static final String    modid   = Reference.MOD_ID;
    public static CreativeTabThut tabThut = CreativeTabThut.tabThut;

    public static Block[]         blocks;
    public static Item[]          items;

    public static Biome           volcano;
    public static Biome           chalk;

    // Configuration Handler that handles the config file
    public ConfigHandler          config;

    public ThutCore()
    {
        BiomeDatabase.getNameFromType(0);
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        proxy.initClient();
        proxy.registerEntities();
        proxy.registerTEs();
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
        AIThreadManager.AIThread.threadCount = config.threadCount;
        AIThreadManager.AIThread.createThreads();
        AIThreadManager aiTicker = new AIThreadManager();
        MinecraftForge.EVENT_BUS.register(aiTicker);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new TickHandler());
        new Cruncher();
        CapabilityManager.INSTANCE.register(IAIMob.class, new Capability.IStorage<IAIMob>()
        {
            @Override
            public NBTBase writeNBT(Capability<IAIMob> capability, IAIMob instance, EnumFacing side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<IAIMob> capability, IAIMob instance, EnumFacing side, NBTBase nbt)
            {
            }
        }, new IAIMob()
        {

            @Override
            public AIStuff getAI()
            {
                return null;
            }

            @Override
            public boolean selfManaged()
            {
                return true;
            }
        }.getClass());
        CapabilityManager.INSTANCE.register(IMobGenetics.class, new Capability.IStorage<IMobGenetics>()
        {

            @Override
            public NBTBase writeNBT(Capability<IMobGenetics> capability, IMobGenetics instance, EnumFacing side)
            {
                NBTTagList genes = new NBTTagList();
                if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
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
                    try
                    {
                        alleles.load(tag.getCompoundTag("V"));
                        ResourceLocation key = new ResourceLocation(tag.getString("K"));
                        instance.getAlleles().put(key, alleles);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        }, DefaultGenetics.class);
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new ConfigCommand());
        TerrainManager.getInstance();
    }

    @EventHandler
    public void serverLoad(FMLServerStoppedEvent event)
    {
        TerrainManager.clear();
        PlayerDataHandler.clear();
        AIThreadManager.clear();
    }

    @SubscribeEvent
    public void PlayerLoggedInEvent(PlayerLoggedInEvent event)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            PacketHandler.sentTerrainValues((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public void BreakBlock(BreakEvent evt)
    {
        EntityPlayer player = evt.getPlayer();
        TileEntity tile = evt.getWorld().getTileEntity(evt.getPos());
        if (tile instanceof IOwnableTE)
        {
            IOwnableTE te = (IOwnableTE) tile;
            NBTTagCompound tag = tile.writeToNBT(new NBTTagCompound());
            if (tag.hasKey("admin") && tag.getBoolean("admin") && !te.canEdit(player))
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