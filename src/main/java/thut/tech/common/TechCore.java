package thut.tech.common;

import static thut.tech.common.network.PacketPipeline.*;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import thut.api.CreativeTabThut;
import thut.api.entity.IMultibox;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;
import thut.tech.common.handlers.BlockHandler;
import thut.tech.common.handlers.ConfigHandler;
import thut.tech.common.handlers.ItemHandler;
import thut.tech.common.network.PacketPipeline.ClientPacket.MessageHandlerClient;
import thut.tech.common.network.PacketPipeline.ServerPacket.MessageHandlerServer;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod( modid = "ThutTech", name="Thut's Tech", version="2.0.0")
public class TechCore 
{
	@SidedProxy(clientSide = "thut.tech.client.ClientProxy", serverSide = "thut.tech.common.CommonProxy")
	public static CommonProxy proxy;
	
	@Instance("ThutTech")
	public static TechCore instance;
	
	public static CreativeTabThut tabThut = CreativeTabThut.tabThut;
    
	@EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
		BlockHandler.registerBlocks();
		ItemHandler.registerItems();
		
		Configuration config =  new Configuration(e.getSuggestedConfigurationFile());
		ConfigHandler.load(config);
	   	 packetPipeline = NetworkRegistry.INSTANCE.newSimpleChannel("ThutTech");
	   	 
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
		
	}
	
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void livingRender(RenderLivingEvent.Post evt)
    {
    	if(!RenderManager.debugBoundingBox)
    		return;
    	
		EntityPlayer p = proxy.getPlayer(null);
		Vector3 v = Vector3.getNewVectorFromPool().set(p);
		Vector3 v1 = Vector3.getNewVectorFromPool();
		Object o = evt.entity;
		if(o instanceof IMultibox)
		{
			EntityLivingBase e = (EntityLivingBase) o;
			IMultibox b = (IMultibox) o;
			for(String s: b.getBoxes().keySet())
			{
				drawOutlinedBoundingBox(b.getBoxes().get(s), v1.set(o).subtractFrom(v), (b.getOffsets().get(s)), 123456);
			}
		}
		v.freeVectorFromPool();
		v1.freeVectorFromPool();
    }

    @SideOnly(Side.CLIENT)
    /**
     * Draws lines for the edges of the bounding box.
     */
    public static void drawOutlinedBoundingBox(Matrix3 box, Vector3 globalOffset, Vector3 localOffset, int colour)
    {
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
    	
        Tessellator tessellator = Tessellator.instance;
        
        if(localOffset == null)
        	localOffset = Vector3.getNewVectorFromPool();
        
        GL11.glPushMatrix();
        
        GL11.glTranslated(globalOffset.x, globalOffset.y, globalOffset.z);
        GL11.glRotated(box.boxRotation().z * 180 / Math.PI, 0, 1, 0);
        GL11.glTranslated(localOffset.x, localOffset.y, localOffset.z);
        
        
        tessellator.startDrawing(3);

        if (colour != -1)
        {
            tessellator.setColorOpaque_I(colour);
        }

        tessellator.addVertex(box.boxMin().x, box.boxMin().y, box.boxMin().z);
        tessellator.addVertex(box.boxMax().x, box.boxMin().y, box.boxMin().z);
        tessellator.addVertex(box.boxMax().x, box.boxMin().y, box.boxMax().z);
        tessellator.addVertex(box.boxMin().x, box.boxMin().y, box.boxMax().z);
        tessellator.addVertex(box.boxMin().x, box.boxMin().y, box.boxMin().z);
        tessellator.draw();
        tessellator.startDrawing(3);

        if (colour != -1)
        {
            tessellator.setColorOpaque_I(colour);
        }

        tessellator.addVertex(box.boxMin().x, box.boxMax().y, box.boxMin().z);
        tessellator.addVertex(box.boxMax().x, box.boxMax().y, box.boxMin().z);
        tessellator.addVertex(box.boxMax().x, box.boxMax().y, box.boxMax().z);
        tessellator.addVertex(box.boxMin().x, box.boxMax().y, box.boxMax().z);
        tessellator.addVertex(box.boxMin().x, box.boxMax().y, box.boxMin().z);
        tessellator.draw();
        tessellator.startDrawing(1);

        if (colour != -1)
        {
            tessellator.setColorOpaque_I(colour);
        }

        tessellator.addVertex(box.boxMin().x, box.boxMin().y, box.boxMin().z);
        tessellator.addVertex(box.boxMin().x, box.boxMax().y, box.boxMin().z);
        tessellator.addVertex(box.boxMax().x, box.boxMin().y, box.boxMin().z);
        tessellator.addVertex(box.boxMax().x, box.boxMax().y, box.boxMin().z);
        tessellator.addVertex(box.boxMax().x, box.boxMin().y, box.boxMax().z);
        tessellator.addVertex(box.boxMax().x, box.boxMax().y, box.boxMax().z);
        tessellator.addVertex(box.boxMin().x, box.boxMin().y, box.boxMax().z);
        tessellator.addVertex(box.boxMin().x, box.boxMax().y, box.boxMax().z);
        tessellator.draw();
        
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
    }
}
