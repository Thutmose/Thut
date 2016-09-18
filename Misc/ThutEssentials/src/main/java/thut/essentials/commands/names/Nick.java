package thut.essentials.commands.names;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thut.essentials.events.NameEvent;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.ConfigManager;
import thut.essentials.util.PlayerDataHandler;
import thut.essentials.util.RuleManager;

public class Nick extends BaseCommand
{
    public Nick()
    {
        super("nick", 2);
        MinecraftForge.EVENT_BUS.register(this);
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
        EntityPlayerMP player;
        int start = 1;
        try
        {
            player = getPlayer(server, sender, args[0]);
        }
        catch (Exception e)
        {
            player = getCommandSenderAsPlayer(sender);
            start = 0;
        }
        String arg = args.length == start ? "" : args[start];
        NBTTagCompound tag = PlayerDataHandler.getCustomDataTag(player);
        NBTTagCompound nametag = tag.getCompoundTag("name");
        if (!nametag.hasKey("original")) nametag.setString("original", player.getDisplayNameString());
        for (int i = start + 1; i < args.length; i++)
        {
            arg = arg + " " + args[i];
        }
        arg = RuleManager.format(arg);
        nametag.setString("name", arg);
        tag.setTag("name", nametag);
        PlayerDataHandler.saveCustomData(player);
        player.refreshDisplayName();
    }

    @SubscribeEvent
    public void getDisplayNameEvent(PlayerEvent.NameFormat event)
    {
        String displayName = event.getDisplayname();
        NBTTagCompound tag = PlayerDataHandler.getCustomDataTag(event.getEntityPlayer());
        NBTTagCompound nametag = tag.getCompoundTag("name");
        if (nametag.hasKey("name") && ConfigManager.INSTANCE.name)
        {
            displayName = nametag.getString("name");
        }
        if (nametag.hasKey("prefix") && ConfigManager.INSTANCE.prefix)
        {
            if (!nametag.getString("prefix").trim().isEmpty())
                displayName = nametag.getString("prefix") + " " + displayName;
        }
        if (nametag.hasKey("suffix") && ConfigManager.INSTANCE.suffix)
        {
            if (!nametag.getString("suffix").trim().isEmpty())
                displayName = displayName + " " + nametag.getString("suffix");
        }
        NameEvent event1 = new NameEvent(event.getEntityPlayer(), displayName);
        MinecraftForge.EVENT_BUS.post(event1);
        event.setDisplayname(event1.getName());
    }

}
