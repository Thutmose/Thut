package thut.tech.client.render;

import org.lwjgl.opengl.GL11;

import scala.actors.threadpool.Arrays;
import thut.api.ThutBlocks;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
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

		if(monitor.getBlockType()==ThutBlocks.liftRail)
			return;
		
		
		for(int i = 0; i<6; i++)
		{
		
			if(!monitor.isSideOn(i))
				continue;
			
			ForgeDirection dir = ForgeDirection.getOrientation(i);
			
			GL11.glPushMatrix();
	
			GL11.glTranslatef((float)x, (float)y, (float)z);
			
			if(dir == ForgeDirection.EAST)
			{
				GL11.glTranslatef(1, 0, 0);
				GL11.glRotatef(270, 0, 1, 0);
			}
			else if(dir == ForgeDirection.SOUTH)
			{
				GL11.glTranslatef(1, 0, 1);
				GL11.glRotatef(180, 0, 1, 0);
			}
			else if(dir == ForgeDirection.WEST)
			{
				GL11.glTranslatef(0, 0, 1);
				GL11.glRotatef(90, 0, 1, 0);
			}	
			
			
			TextureManager renderengine = Minecraft.getMinecraft().renderEngine;
			
			GL11.glPushMatrix();
			if(renderengine != null)
			{
				texture = new ResourceLocation("thuttech:textures/blocks/controlPanel_1.png");
				renderengine.bindTexture(texture);
			}
			GL11.glPushAttrib(GL11.GL_BLEND);
	        GL11.glEnable(GL11.GL_BLEND);
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);

			RenderHelper.disableStandardItemLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
			
			Tessellator t = Tessellator.instance;
			t.startDrawing(GL11.GL_QUADS);
//			t.setBrightness(monitor.getBlockType().getMixedBrightnessForBlock(monitor.getWorldObj(),
//					monitor.xCoord, monitor.yCoord, monitor.zCoord));

			GL11.glTranslated(0, 0, -0.001*(0+0.5));
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);
	        {
		        t.addVertexWithUV(1, 1, 0, 0, 0);
		        t.addVertexWithUV(1, 0, 0, 0, 1);
		        
		        t.addVertexWithUV(0, 0, 0, 1, 1);
		        t.addVertexWithUV(0, 1, 0, 1, 0);
	        }
	        
			t.draw();
			
			GL11.glEnable(GL11.GL_LIGHTING);
			//GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glPopAttrib();
			GL11.glPopAttrib();
			GL11.glPopMatrix();
			
			
			
			drawFloorNumbers(monitor.getSidePage(i));
			drawOverLay(monitor, monitor.floor, 0,i);
			drawOverLay(monitor, monitor.calledFloor, 1,i);
			drawOverLay(monitor, monitor.currentFloor, 2,i);
			if(monitor.lift!=null)
			for(int j = monitor.getSidePage(i) * 16; j<16 + monitor.getSidePage(i) * 16; j++)
			{
				if((monitor.lift.floors[j]<0))
				{
					drawOverLay(monitor, j+1, 3, i);
				}
			}
			
			GL11.glPopMatrix();
		}
	}
	
	public void drawFloorNumbers(int page)
	{
		for(int floor = 0; floor<(16); floor++)
		{
			TextureManager renderengine = Minecraft.getMinecraft().renderEngine;
	
			GL11.glPushMatrix();
			if(renderengine != null)
			{
				texture = new ResourceLocation("thuttech:textures/blocks/font.png");
				renderengine.bindTexture(texture);
			}
			GL11.glPushAttrib(GL11.GL_BLEND);
	        GL11.glEnable(GL11.GL_BLEND);
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
			
			Tessellator t = Tessellator.instance;
			t.startDrawing(GL11.GL_QUADS);
			double x = ((double)(3-floor&3))/(double)4,y= ((double)3-(floor>>2))/(double)4;
			int actFloor = floor + page*16;
			double[] uvs = locationFromNumber((actFloor+1)%10);
			double[] uvs1 = locationFromNumber((actFloor+1)/10);
			GL11.glColor4f(0.0F, 0.0F, 0.0F, 1F);
	        
	        if(actFloor>8)
	        {

				GL11.glTranslated(x+0.01, y+0.06, -0.001*(5+1));
		        t.addVertexWithUV(0.15, 0.15, 0, uvs[0], uvs[2]);
		        t.addVertexWithUV(0.15, 0.0, 0, uvs[0], uvs[3]);
		        
		        t.addVertexWithUV(0, 0.0, 0, uvs[1], uvs[3]);
		        t.addVertexWithUV(0, 0.15, 0, uvs[1], uvs[2]);
	        	
		        t.addVertexWithUV(0.15+0.1, 0.15, 0, uvs1[0], uvs1[2]);
		        t.addVertexWithUV(0.15+0.1, 0, 0, uvs1[0], uvs1[3]);
		        
		        t.addVertexWithUV(0+0.1, 0, 0, uvs1[1], uvs1[3]);
		        t.addVertexWithUV(0+0.1, 0.15, 0, uvs1[1], uvs1[2]);
	        }
	        else
	        {
				GL11.glTranslated(x+0.05, y+0.06, -0.001*(5+1));
		        t.addVertexWithUV(0.15, 0.15, 0, uvs[0], uvs[2]);
		        t.addVertexWithUV(0.15, 0.0, 0, uvs[0], uvs[3]);
		        
		        t.addVertexWithUV(0, 0.0, 0, uvs[1], uvs[3]);
		        t.addVertexWithUV(0, 0.15, 0, uvs[1], uvs[2]);
	        }
	        
			t.draw();
			
			GL11.glEnable(GL11.GL_LIGHTING);
			//GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glPopAttrib();
			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}
		
	}
	
	public void drawOverLay(TileEntityLiftAccess monitor, int floor, int colour, int side)
	{
		floor = floor - monitor.getSidePage(side)*16;
		if(monitor.getBlockMetadata()==1&&monitor.getBlockType()==ThutBlocks.lift&&floor>0&&floor<17)
		{
			
			TextureManager renderengine = Minecraft.getMinecraft().renderEngine;
			String col = colour==0?"green":colour==1?"orange":colour==2?"blue":"gray";
			

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
			GL11.glTranslated(x, y, -0.001*(colour+1));
			Tessellator t = Tessellator.instance;
			t.startDrawing(GL11.GL_QUADS);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
			
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
	
	
	public double[] locationFromNumber(int number)
	{
		double[] ret = new double[4];
		
		if(number>9||number<0)
			return ret;
		int index = 16+number;
		
		ret[0] = (double)(index%10)/10;
		ret[2] = (double)(index/10)/10;
		
		ret[1] = (double)(1+(index)%10)/10;
		ret[3] = (double)(1+(index)/10)/10;
		
		
		return ret;
	}
}
