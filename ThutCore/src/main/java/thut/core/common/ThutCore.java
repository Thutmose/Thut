package thut.core.common;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
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
import thut.api.TickHandler;
import thut.api.block.IOwnableTE;
import thut.api.maths.Cruncher;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.TerrainManager;
import thut.core.common.commands.ConfigCommand;
import thut.core.common.handlers.ConfigHandler;
import thut.reference.ThutCoreReference;

@Mod(modid = ThutCoreReference.MOD_ID, name = ThutCoreReference.MOD_NAME, version = ThutCoreReference.VERSION, updateJSON = ThutCoreReference.UPDATEURL, acceptedMinecraftVersions = ThutCoreReference.MCVERSIONS)
public class ThutCore
{

    @SidedProxy(clientSide = ThutCoreReference.CLIENT_PROXY_CLASS, serverSide = ThutCoreReference.COMMON_PROXY_CLASS)
    public static CommonProxy     proxy;

    @Instance(ThutCoreReference.MOD_ID)
    public static ThutCore        instance;

    public static final String    modid   = ThutCoreReference.MOD_ID;
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
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new TickHandler());
        new Cruncher();
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