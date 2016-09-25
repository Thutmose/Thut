package thut.essentials.commands.homes;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import thut.essentials.commands.CommandManager;
import thut.essentials.commands.misc.Spawn.PlayerMover;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.ConfigManager;
import thut.essentials.util.HomeManager;
import thut.essentials.util.PlayerDataHandler;

public class Home extends BaseCommand
{
    public Home()
    {
        super("home", 0);
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + getCommandName() + " <optional|homeName>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        String homeName = args.length > 0 ? args[0] : null;
        int[] home = HomeManager.getHome(player, homeName);
        
        NBTTagCompound tag = PlayerDataHandler.getCustomDataTag(player);
        NBTTagCompound tptag = tag.getCompoundTag("tp");
        long last = tptag.getLong("homeDelay");
        long time = player.getServer().worldServerForDimension(0).getTotalWorldTime();
        if (last > time)
        {
            player.addChatMessage(
                    CommandManager.makeFormattedComponent("Too Soon between Warp attempt", TextFormatting.RED, false));
            return;
        }
        
        if (home != null)
        {
            if (homeName == null) homeName = "Home";
            ITextComponent teleMess = CommandManager.makeFormattedComponent("Warped to " + homeName,
                    TextFormatting.GREEN);
            tptag.setLong("homeDelay", time + ConfigManager.INSTANCE.homeDelay);
            tag.setTag("tp", tptag);
            PlayerDataHandler.saveCustomData(player);
            PlayerMover.setMove(player, home[3], new BlockPos(home[0], home[1], home[2]), teleMess);
        }
        else
        {
            throw new CommandException("You have no Home");
        }
    }

}
