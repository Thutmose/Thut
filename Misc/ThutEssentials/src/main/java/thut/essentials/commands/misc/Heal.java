package thut.essentials.commands.misc;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import thut.essentials.util.BaseCommand;

public class Heal extends BaseCommand
{
    public Heal()
    {
        super("heal", 2);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityLivingBase toHeal;
        if(args.length==0)
        {
            toHeal = getCommandSenderAsPlayer(sender);
        }
        else
        {
            toHeal = (EntityLivingBase) getEntity(server, sender, args[0]);
        }
        toHeal.setHealth(toHeal.getMaxHealth());
        if(toHeal instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) toHeal;
            player.getFoodStats().setFoodLevel(20);
        }
    }

}
