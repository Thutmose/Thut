package dorfgen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

import dorfgen.conversion.DorfMap;
import dorfgen.conversion.DorfMap.Region;
import dorfgen.conversion.DorfMap.Site;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class Commands extends CommandBase
{
    private List<String> aliases;

    public Commands()
    {
        this.aliases = new ArrayList<String>();
        this.aliases.add("dorfgen");
        this.aliases.add("dg");
    }

    @Override
    public String getCommandName()
    {
        return "dorfgen";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        // TODO Auto-generated method stub
        return "dorfgen <text>";
    }

    @Override
    public List<String> getCommandAliases()
    {
        return aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        // TODO command to say which building player is in in the site.
        if (args.length > 1 && args[0].equalsIgnoreCase("tp") && sender instanceof EntityPlayer)
        {
            String name = args[1];

            if (args[1].contains("\""))
            {
                for (int i = 2; i < args.length; i++)
                {
                    name += args[i];
                }
            }

            EntityPlayer entity = (EntityPlayer) sender;
            Site telesite = null;
            try
            {
                int id = Integer.parseInt(name);
                telesite = DorfMap.sitesById.get(id);
            }
            catch (NumberFormatException e)
            {
                ArrayList<Site> sites = new ArrayList<Site>(DorfMap.sitesById.values());
                for (Site s : sites)
                {
                    if (s.name.replace(" ", "").equalsIgnoreCase(name.replace("\"", "").replace(" ", "")))
                    {
                        telesite = s;
                        break;
                    }
                }
            }
            if (telesite != null)
            {
                int x = telesite.x * WorldGenerator.scale + WorldGenerator.scale;
                int z = telesite.z * WorldGenerator.scale + WorldGenerator.scale;

                int y = WorldGenerator.instance.dorfs.elevationMap[(x - WorldGenerator.shift.getX())
                        / WorldGenerator.scale][(z - WorldGenerator.shift.getZ()) / WorldGenerator.scale];
                entity.addChatMessage(new TextComponentString("Teleported to " + telesite));
                entity.setPositionAndUpdate(x, y, z);
            }
        }
        else if (args.length > 0 && args[0].equalsIgnoreCase("info") && sender instanceof EntityPlayer)
        {
            EntityPlayer entity = (EntityPlayer) sender;
            BlockPos pos = entity.getPosition();
            Region region = WorldGenerator.instance.dorfs.getRegionForCoords(pos.getX(), pos.getZ());
            HashSet<Site> sites = WorldGenerator.instance.dorfs.getSiteForCoords(pos.getX(), pos.getZ());
            String message = "Region: " + region.toString();
            if (sites != null) for (Site site : sites)
            {
                message += ", Site: " + site;
            }
            entity.addChatMessage(new TextComponentString(message));
        }
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
            @Nullable BlockPos pos)
    {
        // TODO Auto-generated method stub

        if (args[0].equalsIgnoreCase("tp"))
        {
            Collection<Site> sites = DorfMap.sitesById.values();
            ArrayList<String> names = new ArrayList<String>();
            Collections.sort(names);
            for (Site site : sites)
            {
                if (site.name.split(" ").length > 1)
                {
                    names.add("\"" + site.name + "\"");
                }
                else
                {
                    names.add(site.name);
                }
            }
            List<String> ret = new ArrayList<String>();
            if (args.length == 2)
            {
                String text = args[1];
                for (String name : names)
                {
                    if (name.contains(text))
                    {
                        ret.add(name);
                    }
                }
            }
            return ret;
        }
        return null;
    }

}
