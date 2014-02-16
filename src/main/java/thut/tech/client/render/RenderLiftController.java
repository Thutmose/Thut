package thut.tech.client.render;

import org.lwjgl.opengl.GL11;

import thut.api.ThutBlocks;
import thut.tech.common.blocks.tileentity.TileEntityLiftAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class RenderLiftController extends TileEntitySpecialRenderer
{
	
	public static final int ID = RenderingRegistry.getNextAvailableRenderId();
	
	private ModelLiftController _model;
	private RenderBlocks blocks;
	private static final double _renderMin = 1.0/16.0;
	private static final double _renderMax = 15.0/16.0;
	private ResourceLocation texture;

	public static class ModelLiftController extends ModelBase
	{
		private ModelRenderer _main;

		public ModelLiftController()
		{
			textureWidth = 64;
			textureHeight = 32;

			_main = new ModelRenderer(this, 0, 0);
			_main.addBox(0F, 0F, 0F, 1, 1, 1);
			_main.setRotationPoint(0F, 0F, 0F);
			_main.setTextureSize(64, 32);
			_main.mirror = true;
			setRotation(_main, 0F, 0F, 0F);
		}

		public void render(TileEntity te)
		{
			_main.render(1F);
		}

		private void setRotation(ModelRenderer model, float x, float y, float z)
		{
			model.rotateAngleX = x;
			model.rotateAngleY = y;
			model.rotateAngleZ = z;
		}
	}
	
	public RenderLiftController()
	{
		_model = new ModelLiftController();
		blocks = new RenderBlocks();
	}
	

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y,
			double z, float f) 
	{
		TileEntityLiftAccess monitor = (TileEntityLiftAccess)tileentity;

		GL11.glPushMatrix();

		GL11.glTranslatef((float)x, (float)y, (float)z);
		
		if(monitor.getFacing() == ForgeDirection.EAST)
		{
			GL11.glTranslatef(1, 0, 0);
			GL11.glRotatef(270, 0, 1, 0);
		}
		else if(monitor.getFacing() == ForgeDirection.SOUTH)
		{
			GL11.glTranslatef(1, 0, 1);
			GL11.glRotatef(180, 0, 1, 0);
		}
		else if(monitor.getFacing() == ForgeDirection.WEST)
		{
			GL11.glTranslatef(0, 0, 1);
			GL11.glRotatef(90, 0, 1, 0);
		}	
		
		drawOverLay(monitor, monitor.floor, 0);
		drawOverLay(monitor, monitor.calledFloor, 1);
		
		GL11.glPopMatrix();
		
	}
	
	public void drawOverLay(TileEntityLiftAccess monitor, int floor, int colour)
	{
		if(monitor.getBlockMetadata()==1&&monitor.getBlockType()==ThutBlocks.lift&&floor>0&&floor<17)
		{
			
			TextureManager renderengine = Minecraft.getMinecraft().renderEngine;
			String col = colour==0?"green":"orange";
			

			GL11.glPushMatrix();
			if(renderengine != null)
			{
				texture = new ResourceLocation("thuttech:textures/blocks/"+col+"Overlay.png");
				renderengine.bindTexture(texture);
			}
			
			GL11.glPushAttrib(GL11.GL_BLEND);
	        GL11.glEnable(GL11.GL_BLEND);
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
			RenderHelper.disableStandardItemLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
			floor -=1;
			double x = ((double)(3-floor&3))/(double)4,y= ((double)3-(floor>>2))/(double)4;
		//	System.out.println(col);
			//GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glTranslated(x, y, -0.001*(colour+1));
			Tessellator t = Tessellator.instance;
			t.startDrawing(GL11.GL_QUADS);
		//	GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
			
	        t.addVertexWithUV(0.25, 0.25, 0, 0, 0);
	        t.addVertexWithUV(0.25, 0, 0, 0, 1);
	        
	        t.addVertexWithUV(0, 0, 0, 1, 1);
	        t.addVertexWithUV(0, 0.25, 0, 1, 0);
	        
			t.draw();
			
			GL11.glEnable(GL11.GL_LIGHTING);
			//GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glPopAttrib();
			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}
		
	}
	
}
