package pokecube.alternative.client.render;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pokecube.alternative.Config;
import pokecube.alternative.container.belt.BeltPlayerData;
import pokecube.alternative.container.belt.IPokemobBelt;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.x3d.X3dModel;
import thut.lib.CompatWrapper;

public class BeltRenderer implements LayerRenderer<EntityLivingBase>
{
    X3dModel                          model;
    ResourceLocation                  belt = new ResourceLocation("pokecube_alternative:textures/belt.png");

    private final RenderLivingBase<?> livingEntityRenderer;

    public BeltRenderer(RenderLivingBase<?> livingEntityRendererIn)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
        model = new X3dModel(new ResourceLocation("pokecube:models/worn/belt.x3d"));
    }

    @Override
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        if (!Config.instance.isEnabled) return;
        EntityPlayer player = (EntityPlayer) entitylivingbaseIn;
        IPokemobBelt cap = BeltPlayerData.getBelt(player);
        int brightness = entitylivingbaseIn.getBrightnessForRender();
        // First pass of render
        GL11.glPushMatrix();
        ((ModelBiped) this.livingEntityRenderer.getMainModel()).bipedBody.postRender(0.0625F);

        float dx = Config.instance.offset[0], dy = Config.instance.offset[1], dz = Config.instance.offset[2];
        if (player.isSneaking())
        {
            GlStateManager.translate(Config.instance.sneak[0], Config.instance.sneak[1], Config.instance.sneak[2]);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();
        float s = 0.52f;
        if (!CompatWrapper.isValid(entitylivingbaseIn.getItemStackFromSlot(EntityEquipmentSlot.LEGS)))
        {
            s = 0.46f;
        }
        // Second pass with colour.
        GL11.glPushMatrix();
        GL11.glRotated(90, 1, 0, 0);
        GL11.glRotated(180, 0, 0, 1);
        GL11.glTranslatef(dx, dy, dz);
        GL11.glScalef(s * 1.01f, s, s);
        this.livingEntityRenderer.bindTexture(belt);
        EnumDyeColor ret = EnumDyeColor.GRAY;
        Color colour = new Color(ret.getColorValue() + 0xFF000000);
        int[] col = { colour.getRed(), colour.getBlue(), colour.getGreen(), 255, brightness };
        for (IExtendedModelPart part : model.getParts().values())
        {
            part.setRGBAB(col);
        }
        model.renderAll();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90, 0, 1, 0);
        GlStateManager.rotate(90, 0, 0, 1);
        float rx = 0, ry = -90, rz = 0;
        dx = dy = dz = 0;
        dz = -0.25f;
        dx = 0.25f;
        for (int i = 0; i < 6; i++)
        {
            ItemStack stack = cap.getCube(i);
            if (CompatWrapper.isValid(stack))
            {
                float amountX = 0.25f;
                float amountZ = 0.15f;
                GlStateManager.pushMatrix();
                if (i < 3)
                {
                    dz = -amountZ * (i + 1) - 0.07f;
                    dx = amountX;
                    ry = -90;
                }
                else
                {
                    dz = amountZ * (i - 2) + 0.07f;
                    dx = amountX;
                    ry = -90;
                }
                GlStateManager.translate(dx, dy, dz);
                GlStateManager.rotate(rx, 1, 0, 0);
                GlStateManager.rotate(ry, 0, 1, 0);
                GlStateManager.rotate(rz, 0, 0, 1);
                GlStateManager.scale(0.135, 0.135, 0.135);
                Minecraft.getMinecraft().getItemRenderer().renderItem(player, stack, null);
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.popMatrix();
        GL11.glColor3f(1, 1, 1);
        GL11.glPopMatrix();
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GL11.glPopMatrix();
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }

}
