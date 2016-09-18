package thut.essentials.commands.misc;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import thut.essentials.util.BaseCommand;

public class Repair extends BaseCommand
{
    public Repair()
    {
        super("repair", 4);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        ItemStack stack = player.getHeldItemMainhand();
        if (stack != null && stack.isItemDamaged())
        {
            stack.setItemDamage(0);
        }
    }
}
