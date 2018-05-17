package thut.dimspam;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = DimSpam.MODID, name = "Dimension Spam", version = DimSpam.VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = DimSpam.MCVERSIONS)
public class DimSpam
{
    public static final String MODID      = "dimspam";
    public static final String VERSION    = "1.0.0";
    public final static String MCVERSIONS = "[1.12.2]";

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent ev)
    {
        if ((ev.getServer().getCommandManager() instanceof ServerCommandManager))
        {
            ServerCommandManager scm = (ServerCommandManager) ev.getServer().getCommandManager();
            scm.registerCommand(new CommandBase()
            {

                @Override
                public String getName()
                {
                    return "dimspam";
                }

                @Override
                public String getUsage(ICommandSender sender)
                {
                    return "/dimspam <newdim|check>";
                }

                @Override
                public void execute(MinecraftServer server, ICommandSender sender, String[] args)
                        throws CommandException
                {
                    if (args.length != 1) throw new CommandException(getUsage(sender));
                    EntityPlayer player = getCommandSenderAsPlayer(sender);
                    if (args[0].equals("check"))
                    {
                        player.sendMessage(new TextComponentString(
                                "Current Dim: " + player.getEntityWorld().provider.getDimension()));
                        return;
                    }
                    int dim = parseInt(args[0]);
                    createNewSecretBaseDimension(dim, dim < 20 ? DimensionType.NETHER
                            : dim < 40 ? DimensionType.OVERWORLD : DimensionType.THE_END);
                    WorldServer world1 = DimensionManager.getWorld(dim);
                    if (player.getLastPortalVec() == null) player.setPortal(player.getPosition().up(20));
                    server.getPlayerList().transferPlayerToDimension((EntityPlayerMP) player, dim,
                            new Teleporter(world1));
                }

                public boolean createNewSecretBaseDimension(int dim, DimensionType type)
                {
                    if (!DimensionManager.isDimensionRegistered(dim)) DimensionManager.registerDimension(dim, type);
                    WorldServer overworld = DimensionManager.getWorld(0);
                    WorldServer world1 = DimensionManager.getWorld(dim);
                    boolean registered = true;
                    if (world1 == null)
                    {
                        MinecraftServer mcServer = overworld.getMinecraftServer();
                        ISaveHandler savehandler = overworld.getSaveHandler();
                        world1 = (WorldServer) (new WorldServerMulti(mcServer, savehandler, dim, overworld,
                                mcServer.profiler).init());
                        world1.addEventListener(new ServerWorldEventHandler(mcServer, world1));
                        MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world1));
                        mcServer.setDifficultyForAllWorlds(mcServer.getDifficulty());
                    }
                    return !registered;
                }

            });
        }
    }
}
