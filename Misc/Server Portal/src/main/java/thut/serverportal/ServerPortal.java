package thut.serverportal;

import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = ServerPortal.MODID, name = "Server Portal", version = ServerPortal.VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = ServerPortal.MCVERSIONS)
public class ServerPortal
{
    public static final String MODID       = "serverportal";
    public static final String VERSION     = "0.0.1";
    public final static String MCVERSIONS  = "[1.12.2]";
    public static int          PORTALSIDE  = 9;
    public static int          PORTALUP    = 3;
    public static int          CLIENTTICKS = 40;
    static WorldSwitcher       switcher    = null;

    public ServerPortal()
    {
    }

    @SideOnly(Side.CLIENT)
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        PORTALSIDE = config.getInt("width", Configuration.CATEGORY_GENERAL, PORTALSIDE, 2, 9, "width of portal");
        PORTALUP = config.getInt("up", Configuration.CATEGORY_GENERAL, PORTALUP, 0, 9,
                "number of blocks above sign for portal");
        CLIENTTICKS = config.getInt("ticks", Configuration.CATEGORY_GENERAL, CLIENTTICKS, 0, 200,
                "ticks after entering portal to completetp (gives time to move out of portal)");
        config.save();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void playerTickEvent(TickEvent.PlayerTickEvent event)
    {
        if (event.player != FMLClientHandler.instance().getClientPlayerEntity()) return;
        BlockPos posOld = event.player.getPosition();
        MutableBlockPos pos = new MutableBlockPos(posOld);
        BlockPos command = null;
        if (pos.getY() - PORTALUP < 0 || pos.getY() > 255 || switcher != null || event.player.ticksExisted % 10 == 0)
            return;
        int dir = -1;
        for (int dx = -PORTALSIDE; dx <= PORTALSIDE; dx++)
        {
            pos.setPos(posOld.getX() + dx, posOld.getY() - PORTALUP, posOld.getZ());
            IBlockState block = event.player.getEntityWorld().getBlockState(pos);
            IBlockState blockUp = event.player.getEntityWorld().getBlockState(pos.up());
            if (block != null && block.getBlock() instanceof BlockCommandBlock && blockUp != null
                    && blockUp.getBlock() instanceof BlockSign)
            {
                command = pos;
                if (dx < 0) dir = 1;
                break;
            }
        }
        if (command == null) for (int dz = -PORTALSIDE; dz <= PORTALSIDE; dz++)
        {
            dir = -2;
            pos.setPos(posOld.getX(), posOld.getY() - PORTALUP, posOld.getZ() + dz);
            IBlockState block = event.player.getEntityWorld().getBlockState(pos);
            IBlockState blockUp = event.player.getEntityWorld().getBlockState(pos.up());
            if (block != null && block.getBlock() instanceof BlockCommandBlock && blockUp != null
                    && blockUp.getBlock() instanceof BlockSign)
            {
                command = pos;
                if (dz < 0) dir = 2;
                break;
            }
        }
        if (command != null)
        {
            TileEntitySign sign = (TileEntitySign) event.player.getEntityWorld().getTileEntity(command.up());
            boolean sign2 = false;
            if (Math.abs(dir) == 1)
            {
                BlockPos tempPos = new BlockPos(pos.getX() + PORTALSIDE * Math.signum(dir), pos.getY() + 1, pos.getZ());
                IBlockState block = event.player.getEntityWorld().getBlockState(tempPos);
                if (block != null && block.getBlock() instanceof BlockSign)
                {
                    sign2 = true;
                }
                if (!sign2) { return; }
            }
            else
            {
                BlockPos tempPos = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ() + PORTALSIDE * Math.signum(dir));
                IBlockState block = event.player.getEntityWorld().getBlockState(tempPos);
                if (block != null && block.getBlock() instanceof BlockSign)
                {
                    sign2 = true;
                }
                if (!sign2) { return; }
            }
            if (sign2)
            {
                try
                {
                    String line1 = sign.signText[0].getUnformattedText();
                    String line2 = sign.signText[1].getUnformattedText();
                    System.out.println(line1 + " " + line2);
                    int port = Integer.parseInt(line2);
                    new WorldSwitcher(line1, port);
                }
                catch (Exception e)
                {
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static class WorldSwitcher
    {
        final String host;
        final int    port;
        boolean      disconnected = false;
        int          ticks        = CLIENTTICKS;

        public WorldSwitcher(String host, int port)
        {
            this.host = host;
            this.port = port;
            MinecraftForge.EVENT_BUS.register(this);
            switcher = this;
        }

        @SubscribeEvent
        public void clientTick(ClientTickEvent event)
        {
            if (event.phase == Phase.END)
            {
                if (ticks-- > 0) { return; }
                if (!disconnected)
                {
                    System.out.println("Disconnecting");
                    FMLClientHandler.instance().getClient().world.sendQuittingDisconnectingPacket();
                    FMLClientHandler.instance().getClient().loadWorld(null);
                    FMLClientHandler.instance().getClient().displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
                    disconnected = true;
                }
                else
                {
                    System.out.println("Connecting");
                    FMLClientHandler.instance().connectToServerAtStartup(host, port);
                    MinecraftForge.EVENT_BUS.unregister(this);
                    switcher = null;
                }
            }
        }
    }
}