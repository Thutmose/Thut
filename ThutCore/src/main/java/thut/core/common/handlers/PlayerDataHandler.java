package thut.core.common.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.netty.buffer.ByteBuf;
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

public class PlayerDataHandler
{
    private static interface IPlayerData
    {
        String getIdentifier();

        String dataFileName();

        boolean shouldSync();

        void writeToNBT(NBTTagCompound tag);

        void readFromNBT(NBTTagCompound tag);

        void readSync(ByteBuf data);

        void writeSync(ByteBuf data);
    }

    public static abstract class PlayerData implements IPlayerData
    {
        @Override
        public void readSync(ByteBuf data)
        {
        }

        @Override
        public void writeSync(ByteBuf data)
        {
        }
    }

    public static class PlayerDataManager
    {
        public Map<Class<? extends PlayerData>, PlayerData> data  = Maps.newHashMap();
        Map<String, PlayerData>                             idMap = Maps.newHashMap();
        final String                                        uuid;

        public PlayerDataManager(String uuid)
        {
            this.uuid = uuid;
            for (Class<? extends PlayerData> type : PlayerDataHandler.dataMap)
            {
                try
                {
                    PlayerData toAdd = type.newInstance();
                    data.put(type, toAdd);
                    idMap.put(toAdd.getIdentifier(), toAdd);
                }
                catch (InstantiationException e)
                {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }

        @SuppressWarnings("unchecked")
        public <T extends PlayerData> T getData(Class<T> type)
        {
            return (T) data.get(type);
        }

        public PlayerData getData(String dataType)
        {
            return idMap.get(dataType);
        }
    }

    public static Set<Class<? extends PlayerData>> dataMap = Sets.newHashSet();
    private static PlayerDataHandler INSTANCESERVER;
    private static PlayerDataHandler INSTANCECLIENT;
    public static PlayerDataHandler getInstance()
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) { return INSTANCECLIENT != null
                ? INSTANCECLIENT : (INSTANCECLIENT = new PlayerDataHandler()); }
        return INSTANCESERVER != null ? INSTANCESERVER : (INSTANCESERVER = new PlayerDataHandler());
    }

    public static void clear()
    {
        if (INSTANCECLIENT != null) MinecraftForge.EVENT_BUS.unregister(INSTANCECLIENT);
        if (INSTANCESERVER != null) MinecraftForge.EVENT_BUS.unregister(INSTANCESERVER);
        INSTANCECLIENT = INSTANCESERVER = null;
    }

    public static void saveCustomData(EntityPlayer player)
    {
        saveCustomData(player.getCachedUniqueIdString());
    }

    public static void saveCustomData(String cachedUniqueIdString)
    {
        getInstance().save(cachedUniqueIdString, "pokecube-custom");
    }

    private Map<String, PlayerDataManager> data = Maps.newHashMap();

    public PlayerDataHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public PlayerDataManager getPlayerData(EntityPlayer player)
    {
        return getPlayerData(player.getCachedUniqueIdString());
    }

    public PlayerDataManager getPlayerData(UUID uniqueID)
    {
        return getPlayerData(uniqueID.toString());
    }

    public PlayerDataManager getPlayerData(String uuid)
    {
        PlayerDataManager manager = data.get(uuid);
        if (manager == null)
        {
            manager = load(uuid);
        }
        return manager;
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
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) for (PlayerData data : manager.data.values())
        {
            String fileName = data.dataFileName();
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
                    data.readFromNBT(nbttagcompound.getCompoundTag("Data"));
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

    public void save(String uuid, String dataType)
    {
        PlayerDataManager manager = data.get(uuid);
        if (manager != null)
        {
            for (PlayerData data : manager.data.values())
            {
                if (!data.getIdentifier().equals(dataType)) continue;
                String fileName = data.dataFileName();
                File file = getFileForUUID(uuid, fileName);
                if (file != null)
                {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    data.writeToNBT(nbttagcompound);
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
    }

    public void save(String uuid)
    {
        PlayerDataManager manager = data.get(uuid);
        if (manager != null)
        {
            for (PlayerData data : manager.data.values())
            {
                String fileName = data.dataFileName();
                File file = getFileForUUID(uuid, fileName);
                if (file != null)
                {
                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                    data.writeToNBT(nbttagcompound);
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
