package thut.generate;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

@Mod(modid = "generate", name = "generate", version = "1.0.0", acceptableRemoteVersions = "*")
public class GenerateTerrain extends CommandBase
{
    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    int          duration = 20;
    int          diff     = 16;
    EntityPlayer source   = null;
    int          xMid     = 0, zMid = 0;
    int          xSize    = 0, zSize = 0;
    int          xCur     = 1, zCur = 1;

    @SubscribeEvent
    public void worldTickEvent(WorldTickEvent evt)
    {
        if (source == null || evt.phase == Phase.END) return;
        if (evt.world.getTotalWorldTime() % duration != 0) return;
        if (evt.world.provider.getDimensionId() != source.dimension) return;

        source.setPositionAndUpdate(xCur + xMid, 255, zCur + zMid);
        source.addChatMessage(new ChatComponentText(xCur+" "+zCur));
        xCur += diff;
        if (xCur >= xSize)
        {
            xCur = -xSize;
            zCur += diff;
        }
        if (zCur >= zSize)
        {
            source.addChatMessage(new ChatComponentText("Done"));
            source.setPositionAndUpdate(xMid, 255, zMid);
            source = null;
        }
    }

    @Override
    public String getCommandName()
    {
        return "generate";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/generate x z xc zc";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 4 || !(sender instanceof EntityPlayer)) return;
        xMid = Integer.parseInt(args[2]);
        zMid = Integer.parseInt(args[3]);

        xSize = Integer.parseInt(args[0]);
        zSize = Integer.parseInt(args[1]);

        xCur = -xSize;
        zCur = -zSize;

        if (args.length > 4)
        {
            duration = Integer.parseInt(args[4]);
        }
        if (args.length > 5)
        {
            diff = Integer.parseInt(args[5]);
        }

        source = (EntityPlayer) sender;
    }

}
