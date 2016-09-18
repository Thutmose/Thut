package thut.permissions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@Mod(modid = ThutPerms.MODID, name = "Thut Permissions WE support", version = ThutPerms.VERSION, dependencies = "required-after:worldedit", updateJSON = ThutPerms.UPDATEURL, acceptableRemoteVersions = "*", acceptedMinecraftVersions = ThutPerms.MCVERSIONS)
public class ThutPerms
{
    public static final String          MODID            = "thutperms";
    public static final String          VERSION          = "0.1.1";
    public static final String          UPDATEURL        = "";

    public final static String          MCVERSIONS       = "[1.9.4]";

    protected static Map<UUID, Group>   groupIDMap       = Maps.newHashMap();
    protected static Map<String, Group> groupNameMap     = Maps.newHashMap();
    protected static HashSet<Group>     groups           = Sets.newHashSet();

    protected static Group              initial;
    protected static Group              mods;
    WorldEditPermissions                worldEditSupport = new WorldEditPermissions();

    static boolean                      allCommandUse    = false;
    static File                         configFile       = null;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        Configuration config = new Configuration(configFile = e.getSuggestedConfigurationFile());
        config.load();
        allCommandUse = config.getBoolean("allCommandUse", Configuration.CATEGORY_GENERAL, false,
                "Can any player use OP commands if their group is allowed to?");
        config.save();

    }

    @Optional.Method(modid = "worldedit")
    @EventHandler
    public void serverAboutToStart(FMLServerStartingEvent event)
    {
        com.sk89q.worldedit.forge.ForgeWorldEdit.inst.setPermissionsProvider(worldEditSupport);
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        if (initial == null)
        {
            initial = new Group("default");
        }
        if (mods == null)
        {
            mods = new Group("mods");
        }
        event.registerServerCommand(new Command());
        MinecraftForge.EVENT_BUS.register(this);
        loadPerms(event.getServer());
        mods.all = true;

        if (allCommandUse)
        {
            Field f = ReflectionHelper.findField(PlayerList.class, "commandsAllowedForAll", "field_72407_n", "t");
            f.setAccessible(true);
            try
            {
                f.set(event.getServer().getPlayerList(), true);
            }
            catch (IllegalArgumentException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    void commandUseEvent(CommandEvent event)
    {
        if (!event.getSender().getServer().isDedicatedServer()) return;
        if (event.getSender() instanceof EntityPlayer && !canUse(event.getCommand(), (EntityPlayer) event.getSender()))
        {
            event.getSender().addChatMessage(new TextComponentString(
                    "You do not have permission to use /" + event.getCommand().getCommandName()));
            event.setCanceled(true);
        }
    }

    @Optional.Method(modid = "thutessentials")
    @SubscribeEvent
    public void NameEvent(thut.essentials.events.NameEvent evt)
    {
        Group g = ThutPerms.groupIDMap.get(evt.toName.getUniqueID());
        if (g == null) return;
        String name = evt.getName();
        if (!g.prefix.isEmpty()) name = g.prefix + " " + name;
        if (!g.suffix.isEmpty()) name = name + " " + g.suffix;
        evt.setName(name);
    }

    @SubscribeEvent
    public void PlayerLoggin(PlayerLoggedInEvent evt)
    {
        EntityPlayer entityPlayer = evt.player;
        if (groupIDMap.get(entityPlayer.getUniqueID()) == null)
        {
            UserListOpsEntry userentry = ((EntityPlayerMP) entityPlayer).mcServer.getPlayerList().getOppedPlayers()
                    .getEntry(entityPlayer.getGameProfile());
            if (userentry != null && userentry.getPermissionLevel() >= 4)
            {
                mods.members.add(entityPlayer.getUniqueID());
                groupIDMap.put(entityPlayer.getUniqueID(), mods);
                savePerms(FMLCommonHandler.instance().getMinecraftServerInstance());
            }
            else
            {
                initial.members.add(entityPlayer.getUniqueID());
                groupIDMap.put(entityPlayer.getUniqueID(), initial);
                savePerms(FMLCommonHandler.instance().getMinecraftServerInstance());
            }
            entityPlayer.refreshDisplayName();
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
                dflt = nbttagcompound1.getCompoundTag("mods");
                name = dflt.getString("name");
                mods = addGroup(name);
                groups.remove(mods);
                mods.readFromNBT(dflt);
                for (UUID id : mods.members)
                {
                    groupIDMap.put(id, mods);
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
                dflt = new NBTTagCompound();
                dflt.setString("name", mods.name);
                mods.writeToNBT(dflt);
                nbttagcompound.setTag("mods", dflt);
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

    static Group getGroup(String name)
    {
        if (name.equals(initial.name)) return initial;
        if (name.equals(mods.name)) return mods;
        return groupNameMap.get(name);
    }

    private boolean canUse(ICommand command, EntityPlayer sender)
    {
        UUID id = sender.getUniqueID();
        Group g = groupIDMap.get(id);
        return g.canUse(command);
    }
}
