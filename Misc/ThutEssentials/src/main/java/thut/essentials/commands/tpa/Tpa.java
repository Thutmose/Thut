package thut.essentials.commands.tpa;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import thut.essentials.commands.CommandManager;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.PlayerDataHandler;

public class Tpa extends BaseCommand
{
    public Tpa()
    {
        super("tpa", 0);
    }

    /** Return whether the specified command parameter index is a username
     * parameter. */
    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        EntityPlayer target = getPlayer(server, sender, args[0]);
        NBTTagCompound tag = PlayerDataHandler.getCustomDataTag(target);
        NBTTagCompound tpaTag = tag.getCompoundTag("tpa");

        if (tpaTag.getBoolean("ignore")) { return; }

        ITextComponent header = player.getDisplayName().appendSibling(
                CommandManager.makeFormattedComponent(" has Requested to tp to you", TextFormatting.YELLOW, true));

        target.addChatMessage(header);
        ITextComponent tpMessage;
        ITextComponent accept = CommandManager.makeFormattedCommandLink("Accept",
                "/tpaccept accept " + player.getCachedUniqueIdString(), TextFormatting.GREEN, true);
        ITextComponent deny = CommandManager.makeFormattedCommandLink("Deny",
                "/tpaccept deny " + player.getCachedUniqueIdString(), TextFormatting.RED, true);
        tpMessage = accept.appendSibling(new TextComponentString("      /      ")).appendSibling(deny);
        target.addChatMessage(tpMessage);
        tpaTag.setString("R", player.getCachedUniqueIdString());
        tag.setTag("tpa", tpaTag);
        PlayerDataHandler.saveCustomData(target);
        player.addChatMessage(CommandManager.makeFormattedComponent(
                target.getDisplayName().getFormattedText() + " has been sent a TPA request", TextFormatting.DARK_GREEN,
                true));
    }
}
