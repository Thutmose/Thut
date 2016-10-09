package thut.wearables.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.IWearable;
import thut.wearables.ThutWearables;
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
        PlayerWearables worn = ThutWearables.getWearables(player);
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
            render(leftRing, player, EnumWearable.FINGER, partialTicks);
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
            render(rightRing, player, EnumWearable.FINGER, partialTicks);
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
            render(leftBrace, player, EnumWearable.WRIST, partialTicks);
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
            render(rightBrace, player, EnumWearable.WRIST, partialTicks);
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
            render(leftLeg, player, EnumWearable.ANKLE, partialTicks);
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
            render(rightLeg, player, EnumWearable.ANKLE, partialTicks);
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
            render(beltStack, player, EnumWearable.WAIST, partialTicks);
            GL11.glPopMatrix();
        }
        if (bag != null)
        {
            GL11.glPushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, 0.0F);
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedBody.postRender(0.0625F);
            render(bag, player, EnumWearable.BACK, partialTicks);
            GL11.glPopMatrix();
        }
        if (neck != null)
        {
            GL11.glPushMatrix();
            if (player.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.23125F, 0.0F);
            }
            ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedBody.postRender(0.0625F);
            render(neck, player, EnumWearable.NECK, partialTicks);
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
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (hat != null)
            {
                GlStateManager.pushMatrix();
                render(hat, player, EnumWearable.HAT, partialTicks);
                GlStateManager.popMatrix();
            }
            if (leftEar != null)
            {
                GlStateManager.pushMatrix();
                GL11.glTranslated(0.25, -0.1, 0.0);
                GL11.glRotated(90, 0, 1, 0);
                GL11.glRotated(90, 1, 0, 0);
                render(leftEar, player, EnumWearable.EAR, partialTicks);
                GlStateManager.popMatrix();
            }
            if (rightEar != null)
            {
                GlStateManager.pushMatrix();
                GL11.glTranslated(-0.25, -0.1, 0.0);
                GL11.glRotated(90, 0, 1, 0);
                GL11.glRotated(90, 1, 0, 0);
                render(rightEar, player, EnumWearable.EAR, partialTicks);
                GlStateManager.popMatrix();
            }
            if (eyes != null)
            {
                GlStateManager.pushMatrix();
                render(eyes, player, EnumWearable.EYE, partialTicks);
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

    private void render(ItemStack stack, EntityPlayer player, EnumWearable slot, float partialTicks)
    {
        if (stack.getItem() instanceof IWearable)
        {
            ((IWearable) stack.getItem()).renderWearable(slot, player, stack, partialTicks);
            return;
        }
        IActiveWearable wearable;
        if ((wearable = stack.getCapability(IActiveWearable.WEARABLE_CAP, null)) != null)
        {
            wearable.renderWearable(slot, player, stack, partialTicks);
        }
    }

}
