package thut.essentials.commands.tpa;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextFormatting;
import thut.essentials.commands.CommandManager;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.PlayerDataHandler;

public class TpToggle extends BaseCommand
{
    public TpToggle()
    {
        super("tptoggle", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        NBTTagCompound tag = PlayerDataHandler.getCustomDataTag(player);
        NBTTagCompound tpaTag = tag.getCompoundTag("tpa");
        boolean ignore = !tpaTag.getBoolean("ignore");
        tpaTag.setBoolean("ignore", ignore);
        tag.setTag("tpa", tpaTag);
        PlayerDataHandler.saveCustomData(player);
        player.addChatMessage(CommandManager.makeFormattedComponent("Set ignoring TPA to " + ignore,
                TextFormatting.DARK_GREEN, true));
    }

}
