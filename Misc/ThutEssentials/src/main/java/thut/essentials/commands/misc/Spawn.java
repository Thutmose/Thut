package thut.essentials.commands.misc;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import thut.essentials.ThutEssentials;
import thut.essentials.commands.CommandManager;
import thut.essentials.events.MoveEvent;
import thut.essentials.util.ConfigManager;
import thut.essentials.util.PlayerDataHandler;
import thut.essentials.util.Transporter;
import thut.essentials.util.Transporter.Vector3;

public class Spawn extends CommandBase
{
    public static class PlayerMover
    {
        final EntityPlayer   player;
        final BlockPos       moveTo;
        final int            dimension;
        final ITextComponent message;

        public PlayerMover(EntityPlayer toMove, int dim, BlockPos pos, ITextComponent mess)
        {
            moveTo = pos;
            player = toMove;
            dimension = dim;
            message = mess;
            toMove.getServer().addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    MinecraftForge.EVENT_BUS.post(new MoveEvent(player));
                    Entity player1 = Transporter.teleportEntity(player, new Vector3(moveTo), dimension);
                    if (message != null) player1.addChatMessage(message);
                }
            });
        }
    }

    public Spawn()
    {
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public String getCommandName()
    {
        return CommandManager.commands.get("spawn").get(0);
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + getCommandName();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        NBTTagCompound tag = PlayerDataHandler.getCustomDataTag(player);
        NBTTagCompound tptag = tag.getCompoundTag("tp");
        long last = tptag.getLong("spawnDelay");
        long time = player.getServer().worldServerForDimension(0).getTotalWorldTime();
        if (last > time)
        {
            player.addChatMessage(
                    CommandManager.makeFormattedComponent("Too Soon between Warp attempt", TextFormatting.RED, false));
            return;
        }
        if (args.length == 0)
        {
            BlockPos spawn = server.worldServerForDimension(ThutEssentials.instance.config.spawnDimension)
                    .getSpawnPoint();
            ITextComponent teleMess = CommandManager.makeFormattedComponent("Warped to Spawn", TextFormatting.GREEN);
            new PlayerMover(player, ThutEssentials.instance.config.spawnDimension, spawn, teleMess);
            tptag.setLong("spawnDelay", time + ConfigManager.INSTANCE.spawnDelay);
            tag.setTag("tp", tptag);
            PlayerDataHandler.saveCustomData(player);
        }
        else if (args[0].equalsIgnoreCase("me"))
        {
            BlockPos spawn = player.getBedLocation();
            if (spawn != null)
            {
                ITextComponent teleMess = CommandManager.makeFormattedComponent("Warped to Bed location",
                        TextFormatting.GREEN);
                new PlayerMover(player, player.dimension, spawn, teleMess);
                tptag.setLong("spawnDelay", time + ConfigManager.INSTANCE.spawnDelay);
                tag.setTag("tp", tptag);
            }
            else
            {
                throw new CommandException("no bed found");
            }
        }
    }

}
