package thut.tech.client.render;

import javax.vecmath.Vector3f;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.client.FMLClientHandler;
import thut.tech.common.blocks.door.TileEntityDoor;

public class RenderDoor extends TileEntitySpecialRenderer<TileEntityDoor>
{

    @Override
    public void renderTileEntityAt(TileEntityDoor te, double x, double y, double z, float partialTicks,
            int destroyStage)
    {
        IBlockState state = te.state;

        Vector3f shift = te.getShiftForPart(te, partialTicks);
        
        if (state != null)
        {
            state = state.getBlock().getExtendedState(state, te.getWorld(), te.getPos());
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            GlStateManager.enableRescaleNormal();
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + shift.x, y + shift.y, z+shift.z);
            GlStateManager.rotate(-90, 0, 1, 0);
            GlStateManager.translate(0, 0, 0);
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            int i1 = state.getBlock().getMixedBrightnessForBlock(te.getWorld(), te.getPos());
            int j1 = i1 % 65536;
            int k1 = i1 / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k1 / 1.0F);
            //TODO colour from block
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            blockrendererdispatcher.renderBlockBrightness(state, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
        }

    }

}
