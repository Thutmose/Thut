package thut.wearables.inventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

public class WearableHandler
{

    public static class PlayerDataManager
    {
        public final PlayerWearables wearables;
        final String                 uuid;

        public PlayerDataManager(String uuid)
        {
            this.uuid = uuid;
            wearables = new PlayerWearables();
        }
    }

    private static WearableHandler INSTANCESERVER;
    private static WearableHandler INSTANCECLIENT;

    public static WearableHandler getInstance()
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) { return INSTANCECLIENT != null
                ? INSTANCECLIENT : (INSTANCECLIENT = new WearableHandler()); }
        return INSTANCESERVER != null ? INSTANCESERVER : (INSTANCESERVER = new WearableHandler());
    }

    public static void clear()
    {
        if (INSTANCECLIENT != null) MinecraftForge.EVENT_BUS.unregister(INSTANCECLIENT);
        if (INSTANCESERVER != null) MinecraftForge.EVENT_BUS.unregister(INSTANCESERVER);
        INSTANCECLIENT = INSTANCESERVER = null;
    }

    private Map<String, PlayerDataManager> data = Maps.newHashMap();

    public WearableHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public PlayerWearables getPlayerData(EntityPlayer player)
    {
        return getPlayerData(player.getCachedUniqueIdString());
    }

    public PlayerWearables getPlayerData(UUID uniqueID)
    {
        return getPlayerData(uniqueID.toString());
    }

    public PlayerWearables getPlayerData(String uuid)
    {
        PlayerDataManager manager = data.get(uuid);
        if (manager == null)
        {
            manager = load(uuid);
        }
        return manager.wearables;
    }

    @SubscribeEvent
    public void cleanupOfflineData(WorldEvent.Save event)
    {
        // Whenever overworld saves, check player list for any that are not
        // online, and remove them. This is done here, and not on logoff, as
        // something may have requested the manager for an offline player, which
        // would have loaded it.
        if (event.getWorld().provider.getDimension() == 0)
        {
            Set<String> toUnload = Sets.newHashSet();
            for (String uuid : data.keySet())
            {
                EntityPlayerMP player = event.getWorld().getMinecraftServer().getPlayerList()
                        .getPlayerByUUID(UUID.fromString(uuid));
                if (player == null)
                {
                    toUnload.add(uuid);
                }
            }
            for (String s : toUnload)
            {
                save(s);
                data.remove(s);
            }
        }
    }

    public PlayerDataManager load(String uuid)
    {
        PlayerDataManager manager = new PlayerDataManager(uuid);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            String fileName = manager.wearables.dataFileName();
            File file = null;
            try
            {
                file = getFileForUUID(uuid, fileName);
            }
            catch (Exception e)
            {

            }
            if (file != null && file.exists())
            {
                try
                {
                    FileInputStream fileinputstream = new FileInputStream(file);
                    NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                    fileinputstream.close();
                    manager.wearables.readFromNBT(nbttagcompound.getCompoundTag("Data"));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        data.put(uuid, manager);
        return manager;
    }

    public void save(String uuid)
    {
        PlayerDataManager manager = data.get(uuid);
        if (manager != null)
        {
            String fileName = manager.wearables.dataFileName();
            File file = getFileForUUID(uuid, fileName);
            if (file != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                manager.wearables.writeToNBT(nbttagcompound);
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setTag("Data", nbttagcompound);
                try
                {
                    FileOutputStream fileoutputstream = new FileOutputStream(file);
                    CompressedStreamTools.writeCompressed(nbttagcompound1, fileoutputstream);
                    fileoutputstream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

        }
    }

    public static File getFileForUUID(String uuid, String fileName)
    {
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
        ISaveHandler saveHandler = world.getSaveHandler();
        String seperator = System.getProperty("file.separator");
        File file = saveHandler.getMapFileFromName(uuid + seperator + fileName);
        File dir = new File(file.getParentFile().getAbsolutePath());
        if (!file.exists())
        {
            dir.mkdirs();
        }
        return file;
    }
}
