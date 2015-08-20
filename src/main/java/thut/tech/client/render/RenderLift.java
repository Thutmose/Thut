package thut.tech.client.render;

import org.lwjgl.opengl.GL11;

import thut.api.render.RenderRebar;
import thut.tech.common.entity.EntityLift;
import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.MinecraftForge;

public class RenderLift extends RendererLivingEntity
{

	float pitch = 0.0f;
	float yaw = 0.0f;
	long time = 0;
	boolean up = true;
	
	@Override
	public void doRender(Entity entity, double d0, double d1, double d2,
			float f, float f1) {
		render(entity, d0, d1, d2);
	}
	
	private IModelCustom modelTurret;
	ResourceLocation texture;
	
	public RenderLift()
	{
		super(null, 0);
		modelTurret = AdvancedModelLoader.loadModel(new ResourceLocation("thuttech","models/lift.obj"));
	}


	public void render(Entity te, double x,double y,double z)
	{
		if(te instanceof EntityLift)
		{
			EntityLift laser = (EntityLift)te;

			float scale = (float) laser.size*0.4f;

			renderBase(te, scale,x,y,z);
			renderAttachments(laser, scale,x,y,z);
			renderAttachments(laser, scale,x,y+4.5,z);
			renderAttachments(laser, 1.23f ,x,y+0.1,z);

		}

	}


	private void renderAttachments(EntityLift lift, float scale, double x,double y,double z)
	{
        GL11.glPushMatrix();

        GL11.glTranslated(x, y, z);
		GL11.glRotatef(lift.axis?-90:180, 0F, 1F, 0F);

		if(scale!=1.23f)
		{
			//Render base of attachement
	        GL11.glPushMatrix();
	        //GL11.glRotatef(180, 0F, 1F, 0F);
	        
			GL11.glTranslated((-5+lift.size)/2.8 - (1-lift.size)*0.15, 0, 0);
	        GL11.glScalef(1.5f, 0.49F, 0.75f);
	
			texture = new ResourceLocation("thuttech", "textures/models/railAttatchment.png");
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
			modelTurret.renderPart("railAttatchment2");
	
	        GL11.glPopMatrix();
	        
	        GL11.glPushMatrix();
	
		//	GL11.glTranslated((5-lift.size)*0.25 - 0.02 * lift.size, 0, 0);
			GL11.glTranslated((5-lift.size)/2.8 + (1-lift.size)*0.15, 0, 0);
	        GL11.glScalef(1.5f, 0.49F, 0.75f);
	        
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
	    	modelTurret.renderPart("railAttatchment1");
	         
	        GL11.glPopMatrix();
		}
		else
		{
	        GL11.glPushMatrix();
	        //GL11.glRotatef(180, 0F, 1F, 0F);
	        
	        double sc = 1.9;
	        double sc2 = 0.47;
	        double sc3 = 0.25;
	        
			GL11.glTranslated((-5+lift.size)/sc - (1-lift.size)*sc2 - (1-lift.size)* (5-lift.size) * sc3, 0, 0);
	        GL11.glScalef(0.749f, 2.249F, 0.749f);
	
			texture = new ResourceLocation("thuttech", "textures/models/railAttatchment.png");
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
			modelTurret.renderPart("railAttatchment2");
	
	        GL11.glPopMatrix();
	        
	        GL11.glPushMatrix();
	
		//	GL11.glTranslated((5-lift.size)*0.25 - 0.02 * lift.size, 0, 0);
			GL11.glTranslated((5-lift.size)/sc + (1-lift.size)*sc2 + (1-lift.size)* (5-lift.size) * sc3, 0, 0);
	        GL11.glScalef(0.749f, 2.249F, 0.749f);
	        
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
	    	modelTurret.renderPart("railAttatchment1");
	         
	        GL11.glPopMatrix();
		}
        
		
        GL11.glPopMatrix();

      //  MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post(lift, this, x, y, z));
        
	}

	private void renderBase(Entity te, float scale, double x,double y,double z)
	{
        
        try {
        	EntityLift lift = (EntityLift) te;
        	
        	GL11.glPushMatrix();
        	GL11.glTranslated(x, y, z);
        	GL11.glScaled(0.999, 0.999, 0.999);
        	
        	for(int i = (int) (-lift.size/2); i<lift.size/2; i++)
        		for(int j = (int) (-lift.size/2); j<lift.size/2;j ++)
        		{

        			Block b = Blocks.iron_block;
        			int meta = 0;
        			if(lift.blocks!=null)
        			{
        				b = Block.getBlockFromItem(lift.blocks[i + (int) (lift.size/2)][j + (int) (lift.size/2)].getItem());
        				meta = lift.blocks[i + (int) (lift.size/2)][j + (int) (lift.size/2)].getItemDamage();
        			}
        			//Render top platform
        			GL11.glPushMatrix();
        	        GL11.glTranslated(0 + i, 0+5, 0 + j);
        	        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        			RenderBlocks.getInstance().renderBlockAsItem(b, meta, 1.0f);
        			GL11.glPopMatrix();
        			//Render bottom platform
        			GL11.glPushMatrix();
        	        GL11.glTranslated(0 + i, 0+0.5, 0 + j);
        	        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        			RenderBlocks.getInstance().renderBlockAsItem(b, meta, 1.0f);
        			GL11.glPopMatrix();
        		}
        	
        	GL11.glPopMatrix();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
        
	}


	@Override
	protected ResourceLocation getEntityTexture(Entity var1) {
		// TODO Auto-generated method stub
		return null;
	}
}
