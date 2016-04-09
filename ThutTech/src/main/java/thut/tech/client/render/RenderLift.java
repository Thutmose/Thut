package thut.tech.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import thut.tech.common.entity.EntityLift;

@SuppressWarnings("rawtypes")
public class RenderLift<T extends EntityLivingBase> extends RendererLivingEntity
{

    float            pitch = 0.0f;
    float            yaw   = 0.0f;
    long             time  = 0;
    boolean          up    = true;

    ResourceLocation texture;

    public RenderLift(RenderManager manager)
    {
        super(manager, null, 0);
    }

    @Override
    public void doRender(EntityLivingBase entity, double d0, double d1, double d2, float f, float f1)
    {
        render(entity, d0, d1, d2);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity var1)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void render(Entity te, double x, double y, double z)
    {
        if (te instanceof EntityLift)
        {
            renderBase(te, 1, x, y, z);
        }

    }

    private void renderBase(Entity te, float scale, double x, double y, double z)
    {

        try
        {
            EntityLift lift = (EntityLift) te;

            GL11.glPushMatrix();
            GL11.glTranslated(x, y, z);
            GL11.glScaled(0.999, 0.999, 0.999);

            int xMin = lift.boundMin.intX();
            int zMin = lift.boundMin.intZ();
            int xMax = lift.boundMax.intX();
            int zMax = lift.boundMax.intZ();
            int yMin = lift.boundMin.intY();
            int yMax = lift.boundMax.intY();
            for (int i = xMin; i <= xMax; i++)
                for (int k = yMin; k <= yMax; k++)
                    for (int j = zMin; j <= zMax; j++)
                    {
                        Block b = Blocks.air;
                        int meta = 0;
                        if (i - xMin >= lift.blocks.length || k - yMin >= lift.blocks[0].length
                                || j - zMin >= lift.blocks[0][0].length)
                        {
                            
                        }
                        else
                        {
                            ItemStack stack = lift.blocks[i - xMin][k - yMin][j - zMin];
                            if (stack == null) continue;
                            b = Block.getBlockFromItem(stack.getItem());
                            meta = stack.getItemDamage();
                        }

                        if (i == 0 && j == 0 && k == 0 && lift.getHeldItem() != null)
                        {
                            b = Block.getBlockFromItem(lift.getHeldItem().getItem());
                            meta = lift.getHeldItem().getItemDamage();
                        }
                        // Render bottom platform
                        GL11.glPushMatrix();
                        GL11.glTranslated(0 + i, k + 0.5, 0 + j);

                        IBlockState iblockstate = b.getStateFromMeta(meta);
                        if (iblockstate.getBlock().getMaterial() != Material.air)
                        {
                            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft()
                                    .getBlockRendererDispatcher();
                            GlStateManager.enableRescaleNormal();
                            GlStateManager.pushMatrix();
                            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                            GlStateManager.rotate(-180.0F, 1.0F, 0.0F, 0.0F);
                            GlStateManager.translate(0.5F, 0.5F, 0.5F);
                            float f7 = 1.0F;
                            GlStateManager.scale(-f7, -f7, f7);
                            int i1 = lift.getBrightnessForRender(0);
                            int j1 = i1 % 65536;
                            int k1 = i1 / 65536;
                            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k1 / 1.0F);
                            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                            FMLClientHandler.instance().getClient().renderEngine
                                    .bindTexture(TextureMap.locationBlocksTexture);
                            blockrendererdispatcher.renderBlockBrightness(iblockstate, 1.0F);
                            GlStateManager.popMatrix();
                            GlStateManager.disableRescaleNormal();
                        }
                        GL11.glPopMatrix();
                    }
            GL11.glPopMatrix();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
