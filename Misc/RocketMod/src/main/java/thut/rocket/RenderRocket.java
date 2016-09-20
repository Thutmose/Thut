package thut.rocket;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.FMLClientHandler;

public class RenderRocket<T extends EntityLivingBase> extends RenderLivingBase<T>
{
    float                    pitch = 0.0f;
    float                    yaw   = 0.0f;
    long                     time  = 0;
    boolean                  up    = true;

    static final Tessellator t     = new Tessellator(2097152);
    VertexBuffer             b     = t.getBuffer();

    ResourceLocation         texture;

    public RenderRocket(RenderManager manager)
    {
        super(manager, null, 0);
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        try
        {
            EntityRocket rocket = (EntityRocket) entity;
            GL11.glPushMatrix();
            GL11.glTranslated(x, y, z);
            GL11.glScaled(0.999, 0.999, 0.999);
            MutableBlockPos pos = new MutableBlockPos();
            int xMin = MathHelper.floor_double(rocket.boundMin.getX() + rocket.posX);
            int zMin = MathHelper.floor_double(rocket.boundMin.getZ() + rocket.posZ);
            int xMax = MathHelper.floor_double(rocket.boundMax.getX() + rocket.posX);
            int zMax = MathHelper.floor_double(rocket.boundMax.getZ() + rocket.posZ);
            int yMin = MathHelper.floor_double(rocket.boundMin.getY() + rocket.posY);
            int yMax = MathHelper.floor_double(rocket.boundMax.getY() + rocket.posY);

            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        pos.setPos(i, j, k);
                        drawBlockAt(pos, rocket);
                        drawTileAt(pos, rocket, partialTicks);
                    }
            GL11.glPopMatrix();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void drawBlockAt(BlockPos pos, EntityRocket lift)
    {
        IBlockState iblockstate = lift.getWorld().getBlockState(pos);
        GL11.glPushMatrix();
        BlockPos liftPos = lift.getPosition();
        GL11.glTranslated(pos.getX() - liftPos.getX(), pos.getY() + 0.5 - liftPos.getY(), pos.getZ() - liftPos.getZ());
        if (iblockstate.getMaterial() != Material.AIR)
        {
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            iblockstate = iblockstate.getActualState(lift.getWorld(), pos);
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
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            if (iblockstate.getRenderType() != EnumBlockRenderType.ENTITYBLOCK_ANIMATED)
                blockrendererdispatcher.renderBlockBrightness(iblockstate, 1.0F);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
        }
        GL11.glPopMatrix();
    }

    private void drawTileAt(BlockPos pos, EntityRocket rocket, float partialTicks)
    {
        TileEntity tile = rocket.getWorld().getTileEntity(pos);
        GL11.glPushMatrix();
        BlockPos liftPos = rocket.getPosition();
        GL11.glTranslated(pos.getX() - liftPos.getX(), pos.getY() + 0.5 - liftPos.getY(), pos.getZ() - liftPos.getZ());
        if (tile != null)
        {
            GlStateManager.enableRescaleNormal();
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-180.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.5F, 0.5F, 0.5F);
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            float f7 = 1.0F;
            GlStateManager.scale(-f7, -f7, f7);
            int i1 = rocket.getBrightnessForRender(0);
            int j1 = i1 % 65536;
            int k1 = i1 / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k1 / 1.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            TileEntityRendererDispatcher.instance.renderTileEntityAt(tile, 0, 0, 0, partialTicks);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
        }
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity)
    {
        return null;
    }
}
