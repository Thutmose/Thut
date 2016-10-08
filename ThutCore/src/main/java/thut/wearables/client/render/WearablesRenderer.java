package thut.wearables.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderLivingBase;
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
        ItemStack leftBrace = null;
        ItemStack rightBrace = null;
        ItemStack leftEar = null;
        ItemStack rightEar = null;
        ItemStack bag = null;
        ItemStack hat = null;
        ItemStack leftLeg = null;
        ItemStack rightLeg = null;
        ItemStack neck = null;
        ItemStack eyes = null;
        PlayerWearables worn = PlayerDataHandler.getInstance().getPlayerData(player).getData(PlayerWearables.class);
        leftRing = worn.getWearable(EnumWearable.FINGER, 1);
        rightRing = worn.getWearable(EnumWearable.FINGER, 0);
        leftBrace = worn.getWearable(EnumWearable.WRIST, 1);
        rightBrace = worn.getWearable(EnumWearable.WRIST, 0);
        beltStack = worn.getWearable(EnumWearable.WAIST);
        bag = worn.getWearable(EnumWearable.BACK);
        hat = worn.getWearable(EnumWearable.HAT);
        leftEar = worn.getWearable(EnumWearable.EAR, 1);
        rightEar = worn.getWearable(EnumWearable.EAR, 0);
        leftLeg = worn.getWearable(EnumWearable.ANKLE, 1);
        rightLeg = worn.getWearable(EnumWearable.ANKLE, 0);
        neck = worn.getWearable(EnumWearable.NECK);
        eyes = worn.getWearable(EnumWearable.EYE);

        if (!(this.livingEntityRenderer.getMainModel() instanceof ModelBiped)) return;
        boolean thin = ((AbstractClientPlayer) player).getSkinType().equals("slim");

        GlStateManager.pushMatrix();
        RenderHelper.disableStandardItemLighting();
        if (leftRing != null)
        {
            GlStateManager.pushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, 0.01F);
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedLeftArm.postRender(0.0625f);
            GlStateManager.translate(0.0625F, 0.59F, 0.0625F);
            if (thin)
            {
                GlStateManager.translate(-0.025, 0, 0);
                GlStateManager.scale(0.75, 1, 1);
            }
            if (leftRing.getItem() instanceof IWearable)
            {
                ((IWearable) leftRing.getItem()).renderWearable(EnumWearable.FINGER, player, leftRing, partialTicks);
            }
            GlStateManager.popMatrix();
        }
        if (rightRing != null)
        {
            GlStateManager.pushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, 0.01F);
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedRightArm.postRender(0.0625f);
            GlStateManager.translate(-0.0625F, 0.59F, 0.0625F);
            if (thin)
            {
                GlStateManager.translate(0.025, 0, 0);
                GlStateManager.scale(0.75, 1, 1);
            }
            if (rightRing.getItem() instanceof IWearable)
            {
                ((IWearable) rightRing.getItem()).renderWearable(EnumWearable.FINGER, player, rightRing, partialTicks);
            }
            GlStateManager.popMatrix();
        }
        if (leftBrace != null)
        {
            GlStateManager.pushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, 0.01F);
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedLeftArm.postRender(0.0625f);
            GlStateManager.translate(0.0625F, 0.4375F, 0.0625F);
            if (thin)
            {
                GlStateManager.translate(-0.025, 0, 0);
                GlStateManager.scale(0.75, 1, 1);
            }
            if (leftBrace.getItem() instanceof IWearable)
            {
                ((IWearable) leftBrace.getItem()).renderWearable(EnumWearable.WRIST, player, leftBrace, partialTicks);
            }
            GlStateManager.popMatrix();
        }
        if (rightBrace != null)
        {
            GlStateManager.pushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, 0.01F);
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedRightArm.postRender(0.0625f);
            GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);
            if (thin)
            {
                GlStateManager.translate(0.025, 0, 0);
                GlStateManager.scale(0.75, 1, 1);
            }
            if (rightBrace.getItem() instanceof IWearable)
            {
                ((IWearable) rightBrace.getItem()).renderWearable(EnumWearable.WRIST, player, rightBrace, partialTicks);
            }
            GlStateManager.popMatrix();
        }
        if (leftLeg != null)
        {
            GlStateManager.pushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, 0.01F);
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedLeftLeg.postRender(0.0625f);
            GlStateManager.translate(0.0F, 0.4375F, 0.0625F);
            if (leftLeg.getItem() instanceof IWearable)
            {
                ((IWearable) leftLeg.getItem()).renderWearable(EnumWearable.WRIST, player, leftLeg, partialTicks);
            }
            GlStateManager.popMatrix();
        }
        if (rightLeg != null)
        {
            GlStateManager.pushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, 0.01F);
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedRightLeg.postRender(0.0625f);
            GlStateManager.translate(0.0F, 0.4375F, 0.0625F);
            if (rightLeg.getItem() instanceof IWearable)
            {
                ((IWearable) rightLeg.getItem()).renderWearable(EnumWearable.WRIST, player, rightLeg, partialTicks);
            }
            GlStateManager.popMatrix();
        }
        if (beltStack != null)
        {
            // First pass of render
            GL11.glPushMatrix();
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedBody.postRender(0.0625F);
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.13125F, -0.105F);
            }
            if (beltStack.getItem() instanceof IWearable)
            {
                ((IWearable) beltStack.getItem()).renderWearable(EnumWearable.WAIST, player, beltStack, partialTicks);
            }
            GL11.glPopMatrix();
        }
        if (bag != null)
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
        if (neck != null)
        {
            GL11.glPushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, -0.0F);
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedBody.postRender(0.0625F);
            if (neck.getItem() instanceof IWearable)
            {
                ((IWearable) neck.getItem()).renderWearable(EnumWearable.NECK, player, neck, partialTicks);
            }
            GL11.glPopMatrix();
        }
        if (hat != null || leftEar != null || rightEar != null || eyes != null)
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
            GlStateManager.translate(0, -0.25, 0);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (hat != null)
            {
                GlStateManager.pushMatrix();
                if (hat.getItem() instanceof IWearable)
                {
                    ((IWearable) hat.getItem()).renderWearable(EnumWearable.HAT, player, hat, partialTicks);
                }
                GlStateManager.popMatrix();
            }
            if (leftEar != null)
            {
                GlStateManager.pushMatrix();
                GL11.glTranslated(0.25, -0.1, 0.0);
                GL11.glRotated(90, 0, 1, 0);
                GL11.glRotated(90, 1, 0, 0);
                if (leftEar.getItem() instanceof IWearable)
                {
                    ((IWearable) leftEar.getItem()).renderWearable(EnumWearable.EAR, player, leftEar, partialTicks);
                }
                GlStateManager.popMatrix();
            }
            if (rightEar != null)
            {
                GlStateManager.pushMatrix();
                GL11.glTranslated(-0.25, -0.1, 0.0);
                GL11.glRotated(90, 0, 1, 0);
                GL11.glRotated(90, 1, 0, 0);
                if (rightEar.getItem() instanceof IWearable)
                {
                    ((IWearable) rightEar.getItem()).renderWearable(EnumWearable.EAR, player, rightEar, partialTicks);
                }
                GlStateManager.popMatrix();
            }
            if (eyes != null)
            {
                GlStateManager.pushMatrix();
                if (eyes.getItem() instanceof IWearable)
                {
                    ((IWearable) eyes.getItem()).renderWearable(EnumWearable.EYE, player, eyes, partialTicks);
                }
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }

}
