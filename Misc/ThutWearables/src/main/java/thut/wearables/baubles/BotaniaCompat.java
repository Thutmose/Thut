package thut.wearables.baubles;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import baubles.api.IBauble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.wearables.EnumWearable;
import thut.wearables.baubles.BaublesCompat.WearableBauble;
import vazkii.botania.api.item.IBaubleRender;
import vazkii.botania.api.item.IBaubleRender.Helper;
import vazkii.botania.api.item.IBaubleRender.RenderType;
import vazkii.botania.api.item.ICosmeticAttachable;
import vazkii.botania.api.item.IPhantomInkable;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.item.ModItems;

/** This class is used to provide rendering support for botania. The render
 * dispatching oode here was found at
 * https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/
 * client/core/handler/BaubleRenderHandler.java and then modified
 * accordingly. */
public class BotaniaCompat
{

    public BotaniaCompat()
    {
        BaublesCompat.botania = true;
    }

    @SubscribeEvent
    public void onItemCapabilityAttach(AttachCapabilitiesEvent.Item event)
    {
        if (event.getItem() instanceof IBauble
                && !event.getCapabilities().containsKey(new ResourceLocation("wearable_compat:bauble")))
        {
            event.addCapability(new ResourceLocation("wearable_compat:bauble"), new WearableBotania());
        }
    }

    public static class WearableBotania extends WearableBauble
    {
        @SideOnly(Side.CLIENT)
        @Override
        public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
        {
            if (slot == EnumWearable.HAT)
            {
                GlStateManager.translate(0, 0.25, 0);
                if (wearer.isSneaking()) GlStateManager.translate(0.0F, -0.2F, 0.0F);
            }
            if (slot == EnumWearable.NECK && wearer.isSneaking())
            {
                GlStateManager.translate(0.0F, -0.15F, -0.2F);
            }
            if (wearer instanceof EntityPlayer) doRenderLayer((EntityPlayer) wearer, partialTicks, stack);
        }

        @SideOnly(Side.CLIENT)
        public void doRenderLayer(@Nonnull EntityPlayer player, float partialTicks, ItemStack stack)
        {
            if (!ConfigHandler.renderBaubles || player.getActivePotionEffect(MobEffects.INVISIBILITY) != null) return;
            dispatchRenders(stack, player, RenderType.BODY, partialTicks);
            // if (inv.getStackInSlot(3) != null)
            // TODO render mana tablet if no belt?
            // renderManaTablet(player);
            float yaw = player.prevRotationYawHead
                    + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks;
            float yawOffset = player.prevRenderYawOffset
                    + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
            float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
            GlStateManager.pushMatrix();
            GlStateManager.rotate(yawOffset, 0, -1, 0);
            GlStateManager.rotate(yaw - 270, 0, 1, 0);
            GlStateManager.rotate(pitch, 0, 0, 1);
            dispatchRenders(stack, player, RenderType.HEAD, partialTicks);
            GlStateManager.popMatrix();
        }

        @SideOnly(Side.CLIENT)
        private void dispatchRenders(ItemStack stack, EntityPlayer player, RenderType type, float partialTicks)
        {
            if (stack != null)
            {
                Item item = stack.getItem();
                if (item instanceof IPhantomInkable)
                {
                    IPhantomInkable inkable = (IPhantomInkable) item;
                    if (inkable.hasPhantomInk(stack)) return;
                }
                if (item instanceof ICosmeticAttachable)
                {
                    ICosmeticAttachable attachable = (ICosmeticAttachable) item;
                    ItemStack cosmetic = attachable.getCosmeticItem(stack);
                    if (cosmetic != null)
                    {
                        GlStateManager.pushMatrix();
                        GL11.glColor3ub((byte) 255, (byte) 255, (byte) 255);
                        GlStateManager.color(1F, 1F, 1F, 1F);
                        ((IBaubleRender) cosmetic.getItem()).onPlayerBaubleRender(cosmetic, player, type, partialTicks);
                        GlStateManager.popMatrix();
                        return;
                    }
                }
                if (item instanceof IBaubleRender)
                {
                    GlStateManager.pushMatrix();
                    GL11.glColor3ub((byte) 255, (byte) 255, (byte) 255);
                    GlStateManager.color(1F, 1F, 1F, 1F);
                    ((IBaubleRender) stack.getItem()).onPlayerBaubleRender(stack, player, type, partialTicks);
                    GlStateManager.popMatrix();
                }
            }

        }

        @SideOnly(Side.CLIENT)
        private void renderManaTablet(EntityPlayer player)
        {
            boolean renderedOne = false;
            for (int i = 0; i < player.inventory.getSizeInventory(); i++)
            {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (stack != null && stack.getItem() == ModItems.manaTablet)
                {
                    GlStateManager.pushMatrix();
                    Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                    Helper.rotateIfSneaking(player);
                    boolean armor = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS) != null;
                    GlStateManager.rotate(90, 0, 1, 0);
                    GlStateManager.rotate(180, 0, 0, 1);
                    GlStateManager.translate(0, -0.6, 0);
                    GlStateManager.scale(0.55, 0.55, 0.55);

                    if (renderedOne) GlStateManager.translate(0F, 0F, armor ? 0.55F : 0.5F);
                    else GlStateManager.translate(0F, 0F, armor ? -0.55F : -0.5F);

                    GlStateManager.scale(0.75F, 0.75F, 0.75F);

                    GlStateManager.color(1F, 1F, 1F);
                    int light = 15728880;
                    int lightmapX = light % 65536;
                    int lightmapY = light / 65536;
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapX, lightmapY);
                    Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
                    GlStateManager.popMatrix();

                    if (renderedOne) return;
                    renderedOne = true;
                }
            }
        }

    }
}
