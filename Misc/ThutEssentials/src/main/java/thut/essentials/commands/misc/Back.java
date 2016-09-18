package thut.essentials.commands.misc;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thut.essentials.commands.CommandManager;
import thut.essentials.events.MoveEvent;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.ConfigManager;
import thut.essentials.util.PlayerDataHandler;
import thut.essentials.util.Transporter;
import thut.essentials.util.Transporter.Vector3;

public class Back extends BaseCommand
{
    public Back()
    {
        super("back", 0);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void move(MoveEvent event)
    {
        event.getEntityPlayer().getEntityData().setIntArray("prevPos", event.getPos());
    }

    @SubscribeEvent
    public void death(LivingDeathEvent event)
    {
        if (event.getEntityLiving() instanceof EntityPlayer)
        {
            BlockPos pos = event.getEntityLiving().getPosition();
            int[] loc = new int[] { pos.getX(), pos.getY(), pos.getZ(), event.getEntityLiving().dimension };
            PlayerDataHandler.getCustomDataTag(event.getEntityLiving().getCachedUniqueIdString()).setIntArray("prevPos",
                    loc);
            PlayerDataHandler.saveCustomData(event.getEntityLiving().getCachedUniqueIdString());
        }
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        NBTTagCompound tag = PlayerDataHandler.getCustomDataTag(player);
        NBTTagCompound tptag = tag.getCompoundTag("tp");
        long last = tptag.getLong("backDelay");
        long time = player.getServer().worldServerForDimension(0).getTotalWorldTime();
        if (last > time)
        {
            player.addChatMessage(
                    CommandManager.makeFormattedComponent("Too Soon between Warp attempt", TextFormatting.RED, false));
            return;
        }
        if (PlayerDataHandler.getCustomDataTag(player).hasKey("prevPos"))
        {
            int[] pos = PlayerDataHandler.getCustomDataTag(player).getIntArray("prevPos");
            Transporter.teleportEntity(player, new Vector3(pos[0], pos[1], pos[2]), pos[3]);
            PlayerDataHandler.getCustomDataTag(player).removeTag("prevPos");
            tptag.setLong("backDelay", time + ConfigManager.INSTANCE.backDelay);
            tag.setTag("tp", tptag);
            PlayerDataHandler.saveCustomData(player);
        }
        else
        {
            throw new CommandException("No valid /back destination");
        }
    }

}
