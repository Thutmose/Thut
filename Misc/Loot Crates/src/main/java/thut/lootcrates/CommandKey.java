package thut.lootcrates;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import thut.lootcrates.XMLStuff.Crate;

public class CommandKey extends CommandBase
{

    public CommandKey()
    {
    }

    @Override
    public String getCommandName()
    {
        return "lckey";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/lckey <player> <crate>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayer player = getPlayer(server, sender, args[0]);
        String name = args[1];
        int num = 1;
        if (args.length > 2) num = Integer.parseInt(args[2]);
        Crate crate = XMLStuff.instance.map.get(name);
        if (crate != null)
        {
            ItemStack key = crate.key.copy();
            CompatWrapper.setStackSize(key, num);
            XMLStuff.giveItem(player, key);
        }
    }

}
