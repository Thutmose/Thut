package thut.tech.common;

import static thut.tech.common.network.PacketPipeline.packetPipeline;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.entity.IMultibox;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;
import thut.core.common.CreativeTabThut;
import thut.tech.ThutTechReference;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.handlers.BlockHandler;
import thut.tech.common.handlers.ConfigHandler;
import thut.tech.common.handlers.ItemHandler;
import thut.tech.common.network.PacketPipeline.ClientPacket;
import thut.tech.common.network.PacketPipeline.ClientPacket.MessageHandlerClient;
import thut.tech.common.network.PacketPipeline.ServerPacket;
import thut.tech.common.network.PacketPipeline.ServerPacket.MessageHandlerServer;

@Mod(modid = ThutTechReference.MOD_ID, name = ThutTechReference.MOD_NAME, version = ThutTechReference.VERSION)
public class TechCore
{
    @SidedProxy(clientSide = ThutTechReference.CLIENT_PROXY_CLASS, serverSide = ThutTechReference.COMMON_PROXY_CLASS)
    public static CommonProxy proxy;

    @Instance(ThutTechReference.MOD_ID)
    public static TechCore instance;

    public static CreativeTabThut tabThut = CreativeTabThut.tabThut;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        BlockHandler.registerBlocks(e);
        ItemHandler.registerItems();
        proxy.preinit(e);

        Configuration config = new Configuration(e.getSuggestedConfigurationFile());
        ConfigHandler.load(config);
        packetPipeline = NetworkRegistry.INSTANCE.newSimpleChannel(ThutTechReference.MOD_ID);

        MinecraftForge.EVENT_BUS.register(this);

        packetPipeline.registerMessage(MessageHandlerClient.class, ClientPacket.class, 0, Side.CLIENT);
        packetPipeline.registerMessage(MessageHandlerServer.class, ServerPacket.class, 1, Side.SERVER);
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        proxy.initClient();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
    	ItemHandler.registerRecipes();
    }

    @EventHandler
    public void serverStop(FMLServerStoppedEvent e)
    {
        EntityLift.clear();
    }

    @SuppressWarnings("rawtypes")
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void livingRender(RenderLivingEvent.Post evt)
    {
        if (!Minecraft.getMinecraft().getRenderManager().isDebugBoundingBox()) return;

        EntityPlayer p = proxy.getPlayer(null);
        Vector3 v = Vector3.getNewVectorFromPool().set(p);
        Vector3 v1 = Vector3.getNewVectorFromPool();
        Object o = evt.entity;
        if (o instanceof IMultibox)
        {
            IMultibox b = (IMultibox) o;
            for (String s : b.getBoxes().keySet())
            {
                drawOutlinedBoundingBox(b.getBoxes().get(s), v1.set(o).subtractFrom(v), (b.getOffsets().get(s)),
                        123456);
            }
        }
        v.freeVectorFromPool();
        v1.freeVectorFromPool();
    }

    @SideOnly(Side.CLIENT)
    /** Draws lines for the edges of the bounding box. */
    public static void drawOutlinedBoundingBox(Matrix3 box, Vector3 globalOffset, Vector3 localOffset, int colour)
    {
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
    }
}
