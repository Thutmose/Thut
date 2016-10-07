package thut.wearables.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import thut.core.common.handlers.PlayerDataHandler;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;
import thut.wearables.inventory.PlayerWearables;

public class WearablesRenderer implements LayerRenderer<EntityPlayer>
{

    private final RenderLivingBase<?> livingEntityRenderer;

    public WearablesRenderer(RenderLivingBase<?> livingEntityRendererIn)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
    }

    @Override
    public void doRenderLayer(EntityPlayer player, float f, float f1, float partialTicks, float f3, float f4, float f5,
            float scale)
    {
        ItemStack beltStack = null;
        ItemStack leftRing = null;
        ItemStack rightRing = null;
        ItemStack bag = null;
        ItemStack hat = null;
        PlayerWearables worn = PlayerDataHandler.getInstance().getPlayerData(player).getData(PlayerWearables.class);
        boolean left = (leftRing = worn.getWearable(EnumWearable.FINGER, 1)) != null;
        boolean right = (rightRing = worn.getWearable(EnumWearable.FINGER, 0)) != null;
        boolean belt = (beltStack = worn.getWearable(EnumWearable.WAIST)) != null;
        boolean back = (bag = worn.getWearable(EnumWearable.BACK)) != null;
        boolean top = (hat = worn.getWearable(EnumWearable.HAT)) != null;
        boolean thin = ((AbstractClientPlayer) player).getSkinType().equals("slim");
        if (!(this.livingEntityRenderer.getMainModel() instanceof ModelBiped)) return;

        if (left)
        {
            GlStateManager.pushMatrix();
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedLeftArm.postRender(0.0625f);
            GlStateManager.translate(0.1, -0.01, 0);
            GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);

            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.203125F, -0.07F);
            }
            if (thin) GlStateManager.scale(0.75, 1, 0.75);
            else
            {
                GlStateManager.scale(0.85, 1, 0.85);
                GL11.glTranslated(0.02, 0, 0.01);
            }
            if (leftRing.getItem() instanceof IWearable)
            {
                ((IWearable) leftRing.getItem()).renderWearable(EnumWearable.FINGER, player, leftRing, partialTicks);
            }
            GlStateManager.popMatrix();
        }
        if (right)
        {
            GlStateManager.pushMatrix();
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedRightArm.postRender(0.0625F);
            GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);

            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.203125F, -0.07F);
            }
            if (thin)
            {
                GlStateManager.scale(0.75, 1, 0.75);
                GL11.glTranslated(0.02, 0, 0.01);
            }
            else GlStateManager.scale(0.85, 1, 0.85);
            if (rightRing.getItem() instanceof IWearable)
            {
                ((IWearable) rightRing.getItem()).renderWearable(EnumWearable.FINGER, player, rightRing, partialTicks);
            }
            GlStateManager.popMatrix();
        }
        if (belt)
        {
            // First pass of render
            GL11.glPushMatrix();
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedBody.postRender(0.0625F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            GlStateManager.pushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.13125F, -0.105F);
            }
            if (beltStack.getItem() instanceof IWearable)
            {
                ((IWearable) beltStack.getItem()).renderWearable(EnumWearable.WAIST, player, beltStack, partialTicks);
            }
            GlStateManager.cullFace(GlStateManager.CullFace.BACK);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            GL11.glPopMatrix();
        }
        if (back)
        {
            GL11.glPushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, -0.0F);
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedBody.postRender(0.0625F);
            if (bag.getItem() instanceof IWearable)
            {
                ((IWearable) bag.getItem()).renderWearable(EnumWearable.BACK, player, bag, partialTicks);
            }
            GL11.glPopMatrix();
        }
        if (top && livingEntityRenderer instanceof RenderPlayer)
        {
            GlStateManager.pushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
            if (player.isChild())
            {
                float af = 2.0F;
                float af1 = 1.4F;
                GlStateManager.translate(0.0F, 0.5F * scale, 0.0F);
                GlStateManager.scale(af1 / af, af1 / af, af1 / af);
                GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedHead.postRender(0.0625F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            GL11.glTranslated(0, -0.25, 0);
            if (hat.getItem() instanceof IWearable)
            {
                ((IWearable) hat.getItem()).renderWearable(EnumWearable.HAT, player, hat, partialTicks);
            }
            GlStateManager.cullFace(GlStateManager.CullFace.BACK);
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }

}
