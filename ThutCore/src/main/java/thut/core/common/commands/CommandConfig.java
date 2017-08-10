package thut.core.common.commands;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import thut.core.common.config.ConfigBase;
import thut.core.common.config.Configure;

public class CommandConfig extends CommandBase
{
    private List<String>   aliases;
    final ConfigBase       config;

    ArrayList<String>      fields   = Lists.newArrayList();

    HashMap<String, Field> fieldMap = Maps.newHashMap();

    public CommandConfig(String name, ConfigBase config)
    {
        this.aliases = new ArrayList<String>();
        this.aliases.add(name);
        this.config = config;
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
        boolean op = CommandTools.isOp(sender);
        if (args.length == 0)
        {
            CommandTools.sendBadArgumentsTryTab(sender);
            return;
        }
        boolean check = args.length <= 1;
        Field field = fieldMap.get(args[0]);

        if (field == null)
        {
            CommandTools.sendBadArgumentsTryTab(sender);
            return;
        }
        try
        {
            String text = "";
            Object o = field.get(config);
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
            ITextComponent mess = CommandTools.makeTranslatedMessage("pokecube.command.settings.check", "gold", args[0],
                    text);
            if (check)
            {
                sender.sendMessage(mess);
                return;
            }
            if (!op)
            {
                CommandTools.sendNoPermissions(sender);
                return;
            }
            try
            {
                String val = args[1];

                if (val.equals("!set"))
                {
                    handleSet(sender, args, o, field);
                    return;
                }

                if (val.equals("!add"))
                {
                    handleAdd(sender, args, o, field);
                    return;
                }

                if (val.equals("!remove"))
                {
                    handleRemove(sender, args, o, field);
                    return;
                }

                if (args.length > 2)
                {
                    for (int i = 2; i < args.length; i++)
                    {
                        val = val + " " + args[i];
                    }
                }
                config.updateField(field, val);
            }
            catch (Exception e)
            {
                mess = CommandTools.makeTranslatedMessage("pokecube.command.settings.invalid", "gold", args[0]);
                sender.sendMessage(mess);
                CommandTools.sendError(sender, text);
                return;
            }
            text = "";
            o = field.get(config);
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
            mess = CommandTools.makeTranslatedMessage("pokecube.command.settings.set", "gold", args[0], text);
            sender.sendMessage(mess);
            return;
        }
        catch (Exception e)
        {
            CommandTools.sendError(sender, "pokecube.command.settings.error");
            return;
        }
    }

    private void handleSet(ICommandSender sender, String[] args, Object o, Field field) throws Exception
    {
        int num = parseInt(args[2]);
        String value = args[3];
        for (int i = 4; i < args.length; i++)
        {
            value = value + " " + args[i];
        }
        String oldValue = "";
        Object toSet = null;
        if (o instanceof String[])
        {
            oldValue = ((String[]) o)[num];
            ((String[]) o)[num] = value;
            toSet = ((String[]) o).clone();
        }
        else if (o instanceof int[])
        {
            oldValue = ((int[]) o)[num] + "";
            ((int[]) o)[num] = parseInt(value);
            toSet = ((int[]) o).clone();
        }
        else
        {
            throw new CommandException("This can only by done for arrays.");
        }
        sender.sendMessage(new TextComponentString("Changed index " + num + " from " + oldValue + " to " + value));
        config.updateField(field, toSet);
    }

    private void handleAdd(ICommandSender sender, String[] args, Object o, Field field) throws Exception
    {
        String value = args[2];
        for (int i = 3; i < args.length; i++)
        {
            value = value + " " + args[i];
        }
        Object toSet = null;
        if (o instanceof String[])
        {
            int len = ((String[]) o).length;
            toSet = Arrays.copyOf(((String[]) o), len + 1);
            ((String[]) toSet)[len] = value;
        }
        else if (o instanceof int[])
        {
            int len = ((int[]) o).length;
            toSet = Arrays.copyOf(((int[]) o), len + 1);
            ((int[]) toSet)[len] = parseInt(value);
        }
        else
        {
            throw new CommandException("This can only by done for arrays.");
        }
        sender.sendMessage(new TextComponentString("Added " + value + " to " + field.getName()));
        config.updateField(field, toSet);
    }

    private void handleRemove(ICommandSender sender, String[] args, Object o, Field field) throws Exception
    {
        String value = args[2];
        for (int i = 3; i < args.length; i++)
        {
            value = value + " " + args[i];
        }
        Object toSet = null;
        if (o instanceof String[])
        {
            String[] arr = ((String[]) o);
            List<String> values = Lists.newArrayList(arr);
            int index = values.indexOf(value);
            if (index != -1) values.remove(index);
            toSet = values.toArray(new String[values.size()]);
        }
        else if (o instanceof int[])
        {
            int[] arr = ((int[]) o);
            int arg = parseInt(value);
            List<Integer> values = Lists.newArrayList();
            for (int i = 0; i < arr.length; i++)
                values.add(arr[i]);
            int index = values.indexOf(arg);
            if (index != -1) values.remove(index);
            toSet = arr = new int[values.size()];
            for (int i = 0; i < values.size(); i++)
                arr[i] = values.get(i);
        }
        else
        {
            throw new CommandException("This can only by done for arrays.");
        }
        sender.sendMessage(new TextComponentString("Removed " + value + " from " + field.getName()));
        config.updateField(field, toSet);
    }

    @Override
    public List<String> getAliases()
    {
        return this.aliases;
    }

    @Override
    public String getName()
    {
        return aliases.get(0);
    }

    @Override
    public String getUsage(ICommandSender sender)
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
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
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
            ret = getListOfStringsMatchingLastWord(args, ret);
        }
        return ret;
    }

    private void populateFields()
    {
        Class<?> me = config.getClass();
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
}