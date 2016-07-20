package thut.permissions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

@Mod(modid = ThutPerms.MODID, name = "Thut Permissions", version = ThutPerms.VERSION, dependencies = "", updateJSON = ThutPerms.UPDATEURL, acceptableRemoteVersions = "*", acceptedMinecraftVersions = ThutPerms.MCVERSIONS)
public class ThutPerms
{
    public static final String          MODID        = "thutperms";
    public static final String          VERSION      = "0.0.1";
    public static final String          UPDATEURL    = "";

    public final static String          MCVERSIONS   = "[1.9.4]";

    protected static Map<UUID, Group>   groupIDMap   = Maps.newHashMap();
    protected static Map<String, Group> groupNameMap = Maps.newHashMap();
    protected static HashSet<Group>     groups       = Sets.newHashSet();

    protected static Group              initial;

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        if (initial == null)
        {
            initial = new Group("default");
        }
        event.registerServerCommand(new Command());
        MinecraftForge.EVENT_BUS.register(this);
        loadPerms(event.getServer());
    }

    @SubscribeEvent
    void commandUseEvent(CommandEvent event)
    {
        if (event.getSender() instanceof EntityPlayer && !canUse(event.getCommand(), (EntityPlayer) event.getSender()))
        {
            event.getSender().addChatMessage(new TextComponentString(
                    "You do not have permission to use /" + event.getCommand().getCommandName()));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void PlayerLoggin(PlayerLoggedInEvent evt)
    {
        EntityPlayer entityPlayer = evt.player;
        if (groupIDMap.get(entityPlayer.getUniqueID()) == null)
        {
            initial.members.add(entityPlayer.getUniqueID());
            groupIDMap.put(entityPlayer.getUniqueID(), initial);
            savePerms(FMLCommonHandler.instance().getMinecraftServerInstance());
        }
    }

    static void loadPerms(MinecraftServer server)
    {
        File file = server.getFile("thutperms.dat");

        if (file != null && file.exists())
        {
            try
            {
                FileInputStream fileinputstream = new FileInputStream(file);
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();
                NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Data");
                NBTTagList groupsTag = (NBTTagList) nbttagcompound1.getTag("groups");
                for (int i = 0; i < groupsTag.tagCount(); i++)
                {
                    NBTTagCompound tag = groupsTag.getCompoundTagAt(i);
                    String name = tag.getString("name");
                    if (!name.isEmpty())
                    {
                        Group g = addGroup(name);
                        g.readFromNBT(tag);
                        for (UUID id : g.members)
                        {
                            groupIDMap.put(id, g);
                        }
                        groups.add(g);
                    }
                }
                NBTTagCompound dflt = nbttagcompound1.getCompoundTag("default");
                String name = dflt.getString("name");
                initial = addGroup(name);
                groups.remove(initial);
                initial.readFromNBT(dflt);
                for (UUID id : initial.members)
                {
                    groupIDMap.put(id, initial);
                }
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }

    static void savePerms(MinecraftServer server)
    {
        File file = server.getFile("thutperms.dat");
        if (file != null)
        {
            try
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                NBTTagList groupTags = new NBTTagList();
                for (Group g : groups)
                {
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setString("name", g.name);
                    g.writeToNBT(tag);
                    groupTags.appendTag(tag);
                }
                NBTTagCompound dflt = new NBTTagCompound();
                dflt.setString("name", initial.name);
                initial.writeToNBT(dflt);
                nbttagcompound.setTag("default", dflt);
                nbttagcompound.setTag("groups", groupTags);
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setTag("Data", nbttagcompound);
                FileOutputStream fileoutputstream = new FileOutputStream(file);
                CompressedStreamTools.writeCompressed(nbttagcompound1, fileoutputstream);
                fileoutputstream.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }

    static Group addGroup(String name)
    {
        Group ret = new Group(name);
        groupNameMap.put(name, ret);
        groups.add(ret);
        return ret;
    }

    static void addToGroup(UUID id, String name)
    {
        Group group = groupNameMap.get(name);
        group.members.add(id);
        groupIDMap.put(id, group);
    }

    private boolean canUse(ICommand command, EntityPlayer sender)
    {
        UUID id = sender.getUniqueID();
        Group g = groupIDMap.get(id);
        return g.canUse(command);
    }
}
