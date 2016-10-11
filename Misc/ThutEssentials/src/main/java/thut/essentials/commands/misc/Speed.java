package thut.essentials.commands.misc;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.ConfigManager;
import thut.essentials.util.PlayerDataHandler;

public class Speed extends BaseCommand
{
    public Speed()
    {
        super("speed", 2);
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
        EntityPlayerMP player = args.length == 1 ? getCommandSenderAsPlayer(sender)
                : getPlayer(server, sender, args[0]);
        double value = args.length == 1 ? Double.parseDouble(args[0]) : Double.parseDouble(args[1]);
        value = Math.min(ConfigManager.INSTANCE.speedCap, value);
        NBTTagCompound tag = PlayerDataHandler.getCustomDataTag(player);
        NBTTagCompound speed = tag.getCompoundTag("speed");
        if (!speed.hasKey("defaultWalk"))
        {
            speed.setDouble("defaultWalk", player.capabilities.getWalkSpeed());
            speed.setDouble("defaultFly", player.capabilities.getFlySpeed());
        }
        NBTTagCompound cap = new NBTTagCompound();
        player.capabilities.writeCapabilitiesToNBT(cap);
        NBTTagCompound ab = cap.getCompoundTag("abilities");
        ab.setFloat("flySpeed", (float) (speed.getDouble("defaultFly") * value));
        ab.setFloat("walkSpeed", (float) (speed.getDouble("defaultWalk") * value));
        cap.setTag("abilities", ab);
        player.capabilities.readCapabilitiesFromNBT(cap);
        player.sendPlayerAbilities();
        tag.setTag("speed", speed);
        PlayerDataHandler.saveCustomData(player);
    }
}
