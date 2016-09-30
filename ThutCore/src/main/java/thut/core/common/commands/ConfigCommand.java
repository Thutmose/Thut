package thut.core.common.commands;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import scala.actors.threadpool.Arrays;
import thut.core.common.ThutCore;
import thut.core.common.config.Configure;
import thut.core.common.handlers.ConfigHandler;

public class ConfigCommand extends CommandBase
{
    private List<String>   aliases;

    ArrayList<String>      fields   = Lists.newArrayList();

    HashMap<String, Field> fieldMap = Maps.newHashMap();

    public ConfigCommand()
    {
        this.aliases = new ArrayList<String>();
        this.aliases.add("thutcoresettings");
        populateFields();
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        boolean op = isOp(sender);
        if (args.length == 0) { throw new CommandException("arguments error, press tab for options"); }
        boolean check = args.length <= 1;
        Field field = fieldMap.get(args[0]);

        if (field == null) { throw new CommandException("arguments error, press tab for options"); }
        try
        {
            String text = "";
            Object o = field.get(ThutCore.instance.config);
            if (o instanceof String[])
            {
                text += Arrays.toString((Object[]) o);
            }
            else if (o instanceof int[])
            {
                text += Arrays.toString((int[]) o);
            }
            else
            {
                text += o;
            }
            ITextComponent mess = new TextComponentTranslation("thutcore.command.settings.check", args[0], text);
            if (check)
            {
                sender.addChatMessage(mess);
                return;
            }
            if (!op) { throw new CommandException("No permission to do that"); }
            try
            {
                String val = args[1];
                if (args.length > 2)
                {
                    for (int i = 2; i < args.length; i++)
                    {
                        val = val + " " + args[i];
                    }
                }
                ThutCore.instance.config.updateField(field, val);
            }
            catch (Exception e)
            {
                throw new CommandException("invalid options");
            }
            text = "";
            o = field.get(ThutCore.instance.config);
            if (o instanceof String[])
            {
                text += Arrays.toString((Object[]) o);
            }
            else if (o instanceof int[])
            {
                text += Arrays.toString((int[]) o);
            }
            else
            {
                text += o;
            }
            mess = new TextComponentTranslation("thutcore.command.settings.set", args[0], text);
            sender.addChatMessage(mess);
            return;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new CommandException("unknown error");
        }
    }

    @Override
    public List<String> getCommandAliases()
    {
        return this.aliases;
    }

    @Override
    public String getCommandName()
    {
        return aliases.get(0);
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + aliases.get(0) + "<option name> <optional:newvalue>";
    }

    @Override
    /** Return the required permission level for this command. */
    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
            BlockPos pos)
    {
        List<String> ret = new ArrayList<String>();
        if (args.length == 1)
        {
            String text = args[0];
            for (String name : fields)
            {
                if (name.contains(text))
                {
                    ret.add(name);
                }
            }
            Collections.sort(ret, new Comparator<String>()
            {
                @Override
                public int compare(String o1, String o2)
                {
                    return o1.compareToIgnoreCase(o2);
                }
            });
        }
        return ret;
    }

    private void populateFields()
    {
        Class<ConfigHandler> me = ConfigHandler.class;
        Configure c;
        for (Field f : me.getDeclaredFields())
        {
            c = f.getAnnotation(Configure.class);
            if (c != null)
            {
                f.setAccessible(true);
                fields.add(f.getName());
                fieldMap.put(f.getName(), f);
            }
        }
    }

    public static boolean isOp(ICommandSender sender)
    {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() != null
                && !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) { return true; }

        if (sender instanceof EntityPlayer)
        {
            EntityPlayer player = sender.getEntityWorld().getPlayerEntityByName(sender.getName());
            UserListOpsEntry userentry = ((EntityPlayerMP) player).mcServer.getPlayerList().getOppedPlayers()
                    .getEntry(player.getGameProfile());
            return userentry != null && userentry.getPermissionLevel() >= 4;
        }
        else if (sender instanceof TileEntityCommandBlock) { return true; }
        return sender.getName().equalsIgnoreCase("@") || sender.getName().equals("Server");
    }
}
