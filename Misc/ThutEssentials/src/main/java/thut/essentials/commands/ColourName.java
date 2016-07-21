package thut.essentials.commands;

import java.util.Random;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thut.essentials.ThutEssentials;

public class ColourName extends CommandBase
{
    public ColourName()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public String getCommandName()
    {
        return ThutEssentials.commands.get("colour").get(0);
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + getCommandName() + " <player> <colour>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        int mode = args.length;
        String arg;
        EntityPlayer player;
        if (mode == 2)
        {
            player = getPlayer(server, sender, args[0]);
            arg = args[1];
        }
        else
        {
            player = getCommandSenderAsPlayer(sender);
            arg = args[0];
        }
        TextFormatting format;
        if (arg.equals("random"))
        {
            int num = new Random().nextInt(16);
            format = TextFormatting.values()[num];
        }
        else
        {
            format = TextFormatting.getValueByName(arg);
        }
        player.getEntityData().setString("TColour", format.toString());
        player.refreshDisplayName();
    }

    @SubscribeEvent
    public void chatEvent(ServerChatEvent event)
    {
    }

    @SubscribeEvent
    public void getDisplayNameEvent(PlayerEvent.NameFormat event)
    {
        String displayName = event.getDisplayname();
        if (event.getEntityPlayer().getEntityData().hasKey("TColour"))
        {
            displayName = event.getEntityPlayer().getEntityData().getString("TColour") + displayName;
        }
        event.setDisplayname(displayName);
    }
}
