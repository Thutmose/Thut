package thut.tech.common;

import static thut.tech.common.network.PacketPipeline.packetPipeline;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.StatCollector;
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

@Mod(modid = ThutTechReference.MOD_ID, name = ThutTechReference.MOD_NAME, dependencies = ThutTechReference.DEPSTRING,
version = ThutTechReference.VERSION, acceptedMinecraftVersions = ThutTechReference.MCVERSIONS)
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
        Vector3 v = Vector3.getNewVector().set(p);
        Vector3 v1 = Vector3.getNewVector();
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

    public static ItemStack getInfoBook()
    {

        ItemStack stack = new ItemStack(Items.written_book);
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString("author", "Thutmose");
        stack.getTagCompound().setString("title", "ThutTech Manual");
        stack.getTagCompound().setBoolean("resolved", true);
        NBTTagList list = new NBTTagList();

        String page1 = StatCollector.translateToLocal("ttinfo.page1.1.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page1.2.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page1.3.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page1.4.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page1.5.name");

        String page2 = StatCollector.translateToLocal("ttinfo.page2.1.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page2.2.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page2.3.name");

        String page3 = StatCollector.translateToLocal("ttinfo.page3.1.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page3.2.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page3.3.name");

        String page4 = StatCollector.translateToLocal("ttinfo.page4.1.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page4.2.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page4.3.name");

        String page5 = StatCollector.translateToLocal("ttinfo.page5.1.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page5.2.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page5.3.name");

        String page6 = StatCollector.translateToLocal("ttinfo.page6.1.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page6.2.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page6.3.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page6.4.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page6.5.name");

        String page7 = StatCollector.translateToLocal("ttinfo.page7.1.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page7.2.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page7.3.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page7.4.name");

        String page8 = StatCollector.translateToLocal("ttinfo.page8.1.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page8.2.name") + "\n"
                + StatCollector.translateToLocal("ttinfo.page8.3.name");

        String page9 = StatCollector.translateToLocal("ttinfo.page9.name") + "\n" + "\n" + "setFloor(number)\n" + "\n"
                + "getFloor()\n" + "\n" + "callFloor(number)\n" + "\n" + "callYValue(number)";

        NBTTagString page = new NBTTagString(page1);
        list.appendTag(page);
        page = new NBTTagString(page2);
        list.appendTag(page);
        page = new NBTTagString(page3);
        list.appendTag(page);
        page = new NBTTagString(page4);
        list.appendTag(page);
        page = new NBTTagString(page5);
        list.appendTag(page);
        page = new NBTTagString(page6);
        list.appendTag(page);
        page = new NBTTagString(page7);
        list.appendTag(page);
        page = new NBTTagString(page8);
        list.appendTag(page);
        page = new NBTTagString(page9);
        list.appendTag(page);
        stack.getTagCompound().setTag("pages", list);
        return stack;
    }
}
