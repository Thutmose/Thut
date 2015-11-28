package thut.tech.client.render;

import org.lwjgl.opengl.GL11;

import thut.tech.common.entity.EntityLift;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;

public class RenderLift extends RendererLivingEntity
{

	float pitch = 0.0f;
	float yaw = 0.0f;
	long time = 0;
	boolean up = true;
	
	@Override
	public void doRender(EntityLivingBase entity, double d0, double d1, double d2,
			float f, float f1) {
		render(entity, d0, d1, d2);
	}
	
//	private IModelCustom modelTurret;
	ResourceLocation texture;
	
	public RenderLift()
	{
		super(Minecraft.getMinecraft().getRenderManager(), null, 0);
//		modelTurret = AdvancedModelLoader.loadModel(new ResourceLocation("thuttech","models/lift.obj"));
	}


	public void render(Entity te, double x,double y,double z)
	{
		if(te instanceof EntityLift)
		{
			EntityLift laser = (EntityLift)te;

			float scale = (float) laser.size*0.4f;

			renderBase(te, scale,x,y,z);

		}

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
        			if(i==0&&j==0&&lift.getHeldItem()!=null)
        			{
                        b = Block.getBlockFromItem(lift.getHeldItem().getItem());
                        meta = lift.getHeldItem().getItemDamage();
        			}
        			//Render top platform
        			GL11.glPushMatrix();
        	        GL11.glTranslated(0 + i, 0+5, 0 + j);
//        			RenderBlocks.getInstance().renderBlockAsItem(b, meta, 1.0f);
        			GL11.glPopMatrix();
        			//Render bottom platform
        			GL11.glPushMatrix();
        	        GL11.glTranslated(0 + i, 0+0.5, 0 + j);

        	        
        	        IBlockState iblockstate = b.getStateFromMeta(meta);

        	        if (iblockstate.getBlock().getMaterial() != Material.air)
        	        {
        	            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        	            GlStateManager.enableRescaleNormal();
        	            GlStateManager.pushMatrix();
                        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                        GlStateManager.rotate(-180.0F, 1.0F, 0.0F, 0.0F);
        	            GlStateManager.translate(0.5F, 0.5F, 0.5F);
//        	            GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
//        	            GlStateManager.translate(0.25F, 0.1875F, 0.25F);
        	            float f7 = 1.0F;
        	            GlStateManager.scale(-f7, -f7, f7);
        	            int i1 = lift.getBrightnessForRender(0);
        	            int j1 = i1 % 65536;
        	            int k1 = i1 / 65536;
        	            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j1 / 1.0F, (float)k1 / 1.0F);
        	            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        	            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        	            blockrendererdispatcher.renderBlockBrightness(iblockstate, 1.0F);
        	            GlStateManager.popMatrix();
        	            GlStateManager.disableRescaleNormal();
        	        }
        	        
        	        
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
