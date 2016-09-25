package thut.essentials.commands.warps;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import thut.essentials.commands.CommandManager;
import thut.essentials.commands.misc.Spawn.PlayerMover;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.ConfigManager;
import thut.essentials.util.PlayerDataHandler;
import thut.essentials.util.WarpManager;

public class Warp extends BaseCommand
{

    public Warp()
    {
        super("warp", 0);
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + getCommandName() + " <warpName>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        String warpName = args[0];
        for (int i = 1; i < args.length; i++)
        {
            warpName = warpName + " " + args[i];
        }
        int[] warp = WarpManager.getWarp(warpName);
        NBTTagCompound tag = PlayerDataHandler.getCustomDataTag(player);
        NBTTagCompound tptag = tag.getCompoundTag("tp");
        long last = tptag.getLong("warpDelay");
        long time = player.getServer().worldServerForDimension(0).getTotalWorldTime();
        if (last > time)
        {
            player.addChatMessage(
                    CommandManager.makeFormattedComponent("Too Soon between Warp attempt", TextFormatting.RED, false));
            return;
        }
        if (warp != null)
        {
            ITextComponent teleMess = CommandManager.makeFormattedComponent("Warped to " + warpName,
                    TextFormatting.GREEN);
            PlayerMover.setMove(player, warp[3], new BlockPos(warp[0], warp[1], warp[2]), teleMess);
            tptag.setLong("warpDelay", time + ConfigManager.INSTANCE.warpDelay);
            tag.setTag("tp", tptag);
            PlayerDataHandler.saveCustomData(player);
        }
        else throw new CommandException("Warp " + warpName + " not found.");
    }

}
