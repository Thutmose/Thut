package thut.essentials.commands.tpa;

import java.util.UUID;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import thut.essentials.commands.CommandManager;
import thut.essentials.commands.misc.Spawn;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.PlayerDataHandler;

public class TpAccept extends BaseCommand
{
    public TpAccept()
    {
        super("tpaccept", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        String id = args[1];
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        NBTTagCompound tag = PlayerDataHandler.getCustomDataTag(player);
        NBTTagCompound tpaTag = tag.getCompoundTag("tpa");
        String requestor = tpaTag.getString("R");
        if (!requestor.equals(id)) { return; }
        tpaTag.removeTag("R");
        tag.setTag("tpa", tpaTag);
        PlayerDataHandler.saveCustomData(player);
        EntityPlayer target = server.getPlayerList().getPlayerByUUID(UUID.fromString(id));
        if (args[0].equals("accept"))
        {
            target.addChatMessage(
                    CommandManager.makeFormattedComponent("Your TPA request was accepted", TextFormatting.GREEN, true));
            new Spawn.PlayerMover(target, player.dimension, player.getPosition(), null);
        }
        else if (args[0].equals("deny"))
        {
            target.addChatMessage(
                    CommandManager.makeFormattedComponent("Your TPA request was denied", TextFormatting.RED, true));
        }
    }
}
