package pokecube.alternative.client.gui;

import java.awt.Color;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent;
import pokecube.core.client.Resources;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.render.RenderHealth;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Tools;

public class GuiBattleHandler
{

    public GuiBattleHandler()
    {
        // TODO Auto-generated constructor stub
    }

    @SubscribeEvent(receiveCanceled = false)
    public void drawSelected(GuiEvent.RenderSelectedInfo evt)
    {
        IPokemob pokemob = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
        if (pokemob == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.pushMatrix();
        GL11.glTranslated(27, 6, 0);
        GuiDisplayPokecubeInfo.guiDims[0] = 60;
        GuiDisplayPokecubeInfo.applyTransform(PokecubeCore.core.getConfig().guiRef, PokecubeMod.core.getConfig().guiPos,
                GuiDisplayPokecubeInfo.guiDims, PokecubeMod.core.getConfig().guiSize);
        EntityLivingBase entity = (EntityLivingBase) pokemob;
        float scale = 1.5f;
        GlStateManager.scale(scale, scale, scale);
        GL11.glTranslated(9, -2, 0);
        drawHealth(entity);
        if (pokemob.getStatus() != IPokemob.STATUS_NON)
        {
            mc.renderEngine.bindTexture(Resources.GUI_BATTLE);
            byte status = pokemob.getStatus();
            float x = -34;
            float y = -6.5f;
            int dv = 0;
            if ((status & IMoveConstants.STATUS_BRN) != 0)
            {
                dv = 2 * 14;
            }
            if ((status & IMoveConstants.STATUS_FRZ) != 0)
            {
                dv = 1 * 14;
            }
            if ((status & IMoveConstants.STATUS_PAR) != 0)
            {
                dv = 3 * 14;
            }
            if ((status & IMoveConstants.STATUS_PSN) != 0)
            {
                dv = 4 * 14;
            }
            int height = 15;
            int width = 15;
            float sx = 0.5f;
            float sy = sx;
            int textureY = 138 + dv;
            int textureX = 0;
            float f = 0.00390625F;
            float f1 = 0.00390625F;
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexbuffer.pos(x + 0, y + height * sy, 0).tex((textureX + 0) * f, (textureY + height) * f1).endVertex();
            vertexbuffer.pos(x + width * sx, y + height * sy, 0).tex((textureX + width) * f, (textureY + height) * f1)
                    .endVertex();
            vertexbuffer.pos(x + width * sx, y + 0, 0).tex((textureX + width) * f, (textureY + 0) * f1).endVertex();
            vertexbuffer.pos(x + 0, y + 0, 0).tex((textureX + 0) * f, (textureY + 0) * f1).endVertex();
            tessellator.draw();
        }
        GL11.glTranslated(-9, 2, 0);
        GlStateManager.scale(1 / scale, 1 / scale, 1 / scale);

        GlStateManager.pushMatrix();
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        boolean lighting = GL11.glGetBoolean(GL11.GL_LIGHTING);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        int moveCount = 0;
        for (moveCount = 0; moveCount < 4; moveCount++)
        {
            if (pokemob.getMove(moveCount) == null) break;
        }
        float padding = PokecubeMod.core.getConfig().backgroundPadding;
        int bgHeight = 0;
        scale = 0.75f;
        int barHeight1 = (int) (mc.fontRendererObj.FONT_HEIGHT * moveCount * scale);
        float size = PokecubeMod.core.getConfig().plateSize;
        int selected = GuiDisplayPokecubeInfo.instance.currentMoveIndex;
        GlStateManager.translate(0F, 6, 0F);

        // Background
        if (PokecubeMod.core.getConfig().drawBackground)
        {
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(-size - padding, -bgHeight, 0.0D).color(0, 0, 0, 64).endVertex();
            buffer.pos(-size - padding, barHeight1 + padding, 0.0D).color(0, 0, 0, 64).endVertex();
            buffer.pos(size + padding, barHeight1 + padding, 0.0D).color(0, 0, 0, 64).endVertex();
            buffer.pos(size + padding, -bgHeight, 0.0D).color(0, 0, 0, 64).endVertex();
            tessellator.draw();
            if (selected != 5)
            {
                GL11.glScaled(scale, scale, scale);
                GlStateManager.translate(0F, selected * mc.fontRendererObj.FONT_HEIGHT, 0F);
                barHeight1 = (int) (mc.fontRendererObj.FONT_HEIGHT * scale);
                buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                int alpha = 128;
                int r = 0;
                int g = 0;
                int b = 128;
                buffer.pos(-size / scale - padding, -bgHeight, 0.0D).color(r, g, b, alpha).endVertex();
                buffer.pos(-size / scale - padding, barHeight1 + padding, 0.0D).color(r, g, b, alpha).endVertex();
                buffer.pos(size / scale + padding, barHeight1 + padding, 0.0D).color(r, g, b, alpha).endVertex();
                buffer.pos(size / scale + padding, -bgHeight, 0.0D).color(r, g, b, alpha).endVertex();
                tessellator.draw();
                GlStateManager.translate(0F, -selected * mc.fontRendererObj.FONT_HEIGHT, 0F);
                GL11.glScaled(1 / scale, 1 / scale, 1 / scale);
            }
        }
        GlStateManager.enableTexture2D();

        for (int moveIndex = 0; moveIndex < 4; moveIndex++)
        {
            int index = moveIndex;
            Move_Base move = MovesUtils.getMoveFromName(pokemob.getMove(index));

            if (move != null)
            {
                GL11.glScaled(scale, scale, scale);
                mc.fontRendererObj.drawString(MovesUtils.getMoveName(move.getName()).getFormattedText(), -33,
                        moveIndex * mc.fontRendererObj.FONT_HEIGHT, move.move.type.colour);
                GL11.glScaled(1 / scale, 1 / scale, 1 / scale);
            }
        }
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        if (lighting) GlStateManager.enableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        evt.setCanceled(true);
    }

    @SubscribeEvent(receiveCanceled = false)
    public void drawTarget(GuiEvent.RenderTargetInfo evt)
    {
        IPokemob pokemob = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
        if (pokemob == null) return;
        EntityLivingBase entity = ((EntityLiving) pokemob).getAttackTarget();
        if (entity == null) return;
        GlStateManager.pushMatrix();
        GL11.glTranslated(27, 6, 0);
        GuiDisplayPokecubeInfo.targetDims[0] = 60;
        GuiDisplayPokecubeInfo.applyTransform(PokecubeCore.core.getConfig().targetRef,
                PokecubeMod.core.getConfig().targetPos, GuiDisplayPokecubeInfo.targetDims,
                PokecubeMod.core.getConfig().targetSize);
        float scale = 1.5f;
        GlStateManager.scale(scale, scale, scale);
        drawHealth(entity);
        if (pokemob.getStatus() != IPokemob.STATUS_NON)
        {
            Minecraft.getMinecraft().renderEngine.bindTexture(Resources.GUI_BATTLE);
            byte status = pokemob.getStatus();
            float x = 26.5f;
            float y = -6.5f;
            int dv = 0;
            if ((status & IMoveConstants.STATUS_BRN) != 0)
            {
                dv = 2 * 14;
            }
            if ((status & IMoveConstants.STATUS_FRZ) != 0)
            {
                dv = 1 * 14;
            }
            if ((status & IMoveConstants.STATUS_PAR) != 0)
            {
                dv = 3 * 14;
            }
            if ((status & IMoveConstants.STATUS_PSN) != 0)
            {
                dv = 4 * 14;
            }
            int height = 15;
            int width = 15;
            float sx = 0.5f;
            float sy = sx;
            int textureY = 138 + dv;
            int textureX = 0;
            float f = 0.00390625F;
            float f1 = 0.00390625F;
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexbuffer.pos(x + 0, y + height * sy, 0).tex((textureX + 0) * f, (textureY + height) * f1).endVertex();
            vertexbuffer.pos(x + width * sx, y + height * sy, 0).tex((textureX + width) * f, (textureY + height) * f1)
                    .endVertex();
            vertexbuffer.pos(x + width * sx, y + 0, 0).tex((textureX + width) * f, (textureY + 0) * f1).endVertex();
            vertexbuffer.pos(x + 0, y + 0, 0).tex((textureX + 0) * f, (textureY + 0) * f1).endVertex();
            tessellator.draw();
        }
        GlStateManager.popMatrix();
        evt.setCanceled(true);
    }

    private void drawHealth(EntityLivingBase entity)
    {
        IPokemob pokemob = null;
        if (entity instanceof IPokemob) pokemob = (IPokemob) entity;
        if (entity == null) { return; }
        Minecraft mc = Minecraft.getMinecraft();
        processing:
        {
            float maxHealth = entity.getMaxHealth();
            float health = Math.min(maxHealth, entity.getHealth());

            if (maxHealth <= 0) break processing;
            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

            GlStateManager.pushMatrix();
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            boolean lighting = GL11.glGetBoolean(GL11.GL_LIGHTING);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer buffer = tessellator.getBuffer();

            float padding = PokecubeMod.core.getConfig().backgroundPadding;
            int bgHeight = PokecubeMod.core.getConfig().backgroundHeight;
            int barHeight1 = PokecubeMod.core.getConfig().barHeight;
            float size = PokecubeMod.core.getConfig().plateSize;

            int r = 0;
            int g = 255;
            int b = 0;
            ItemStack stack = null;
            if (pokemob != null && pokemob.getPokemonOwner() == renderManager.renderViewEntity)
            {
                stack = entity.getHeldItemMainhand();
            }
            int armor = entity.getTotalArmorValue();
            float hue = Math.max(0F, (health / maxHealth) / 3F - 0.07F);
            Color color = Color.getHSBColor(hue, 1F, 1F);
            r = color.getRed();
            g = color.getGreen();
            b = color.getBlue();
            GlStateManager.translate(0F, 0f, 0F);
            ITextComponent nameComp = entity.getDisplayName();
            if (entity.hasCustomName())
                nameComp = new TextComponentString(TextFormatting.ITALIC + entity.getCustomNameTag());
            float s = 0.5F;
            String name = I18n.format(nameComp.getFormattedText());
            float namel = mc.fontRendererObj.getStringWidth(name) * s;
            if (namel + 20 > size * 2) size = namel / 2F + 10F;
            float healthSize = size * (health / maxHealth);

            // Background
            if (PokecubeMod.core.getConfig().drawBackground)
            {
                buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                buffer.pos(-size - padding, -bgHeight, 0.0D).color(0, 0, 0, 64).endVertex();
                buffer.pos(-size - padding, barHeight1 + padding, 0.0D).color(0, 0, 0, 64).endVertex();
                buffer.pos(size + padding, barHeight1 + padding, 0.0D).color(0, 0, 0, 64).endVertex();
                buffer.pos(size + padding, -bgHeight, 0.0D).color(0, 0, 0, 64).endVertex();
                tessellator.draw();
            }

            // Health bar
            // Gray Space
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(-size, 0, 0.0D).color(127, 127, 127, 127).endVertex();
            buffer.pos(-size, barHeight1, 0.0D).color(127, 127, 127, 127).endVertex();
            buffer.pos(size, barHeight1, 0.0D).color(127, 127, 127, 127).endVertex();
            buffer.pos(size, 0, 0.0D).color(127, 127, 127, 127).endVertex();
            tessellator.draw();

            // Health Bar
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(-size, 0, 0.0D).color(r, g, b, 127).endVertex();
            buffer.pos(-size, barHeight1, 0.0D).color(r, g, b, 127).endVertex();
            buffer.pos(healthSize * 2 - size, barHeight1, 0.0D).color(r, g, b, 127).endVertex();
            buffer.pos(healthSize * 2 - size, 0, 0.0D).color(r, g, b, 127).endVertex();
            tessellator.draw();

            // Exp Bar
            if (pokemob != null)
            {
                r = 64;
                g = 64;
                b = 255;
                int exp = pokemob.getExp() - Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel());
                float maxExp = Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel() + 1)
                        - Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel());
                if (pokemob.getLevel() == 100) maxExp = exp = 1;
                if (exp < 0 || !pokemob.getPokemonAIState(IMoveConstants.TAMED))
                {
                    exp = 0;
                }
                float expSize = size * (exp / maxExp);
                // Gray Space
                buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                buffer.pos(-size, barHeight1, 0.0D).color(127, 127, 127, 127).endVertex();
                buffer.pos(-size, barHeight1 + 1, 0.0D).color(127, 127, 127, 127).endVertex();
                buffer.pos(size, barHeight1 + 1, 0.0D).color(127, 127, 127, 127).endVertex();
                buffer.pos(size, barHeight1, 0.0D).color(127, 127, 127, 127).endVertex();
                tessellator.draw();
                // Bar
                buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                buffer.pos(-size, barHeight1, 0.0D).color(r, g, b, 127).endVertex();
                buffer.pos(-size, barHeight1 + 1, 0.0D).color(r, g, b, 127).endVertex();
                buffer.pos(expSize * 2 - size, barHeight1 + 1, 0.0D).color(r, g, b, 127).endVertex();
                buffer.pos(expSize * 2 - size, barHeight1, 0.0D).color(r, g, b, 127).endVertex();
                tessellator.draw();
            }

            GlStateManager.enableTexture2D();

            GlStateManager.pushMatrix();
            GlStateManager.translate(-size, -4.5F, 0F);
            GlStateManager.scale(s, s, s);

            UUID owner = null;
            if (pokemob != null)
            {
                owner = pokemob.getPokemonOwnerID();
            }
            boolean isOwner = renderManager.renderViewEntity.getUniqueID().equals(owner);
            int colour = isOwner ? 0xFFFFFF : 0xAA4444;
            mc.fontRendererObj.drawString(name, 0, 0, colour);

            GlStateManager.pushMatrix();
            float s1 = 0.75F;
            GlStateManager.scale(s1, s1, s1);

            int h = PokecubeMod.core.getConfig().hpTextHeight;
            String maxHpStr = "" + (int) (Math.round(maxHealth * 100.0) / 100.0);
            String hpStr = "" + (int) (Math.round(health * 100.0) / 100.0);
            String healthStr = hpStr + "/" + maxHpStr;
            String gender = "";
            String lvlStr = "";
            if (pokemob != null)
            {
                gender = pokemob.getSexe() == IPokemob.MALE ? "\u2642"
                        : pokemob.getSexe() == IPokemob.FEMALE ? "\u2640" : "";
                lvlStr = "L." + pokemob.getLevel();
            }

            if (maxHpStr.endsWith(".0")) maxHpStr = maxHpStr.substring(0, maxHpStr.length() - 2);
            if (hpStr.endsWith(".0")) hpStr = hpStr.substring(0, hpStr.length() - 2);
            colour = 0xBBBBBB;
            if (pokemob != null)
            {
                if (pokemob.getSexe() == IPokemob.MALE)
                {
                    colour = 0x0011CC;
                }
                else if (pokemob.getSexe() == IPokemob.FEMALE)
                {
                    colour = 0xCC5555;
                }
            }
            if (isOwner) mc.fontRendererObj.drawString(healthStr,
                    (int) (size / (s * s1)) - mc.fontRendererObj.getStringWidth(healthStr) / 2, h, 0xFFFFFFFF);
            mc.fontRendererObj.drawString(lvlStr, 2, h, 0xFFFFFF);
            mc.fontRendererObj.drawString(gender,
                    (int) (size / (s * s1) * 2) - 2 - mc.fontRendererObj.getStringWidth(gender), h - 1, colour);
            GlStateManager.popMatrix();

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int off = 0;
            s1 = 0.5F;
            GlStateManager.scale(s1, s1, s1);
            GlStateManager.translate(size / (s * s1) * 2 - 16, 0F, 0F);
            mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            if (stack != null && PokecubeMod.core.getConfig().showAttributes)
            {
                RenderHealth.renderIcon(off, 0, stack, 16, 16);
                off -= 16;
            }

            if (armor > 0 && PokecubeMod.core.getConfig().showArmor)
            {
                int ironArmor = armor % 5;
                int diamondArmor = armor / 5;
                if (!PokecubeMod.core.getConfig().groupArmor)
                {
                    ironArmor = armor;
                    diamondArmor = 0;
                }

                stack = new ItemStack(Items.IRON_CHESTPLATE);
                for (int i = 0; i < ironArmor; i++)
                {
                    RenderHealth.renderIcon(off, 0, stack, 16, 16);
                    off -= 4;
                }

                stack = new ItemStack(Items.DIAMOND_CHESTPLATE);
                for (int i = 0; i < diamondArmor; i++)
                {
                    RenderHealth.renderIcon(off, 0, stack, 16, 16);
                    off -= 4;
                }
            }

            GlStateManager.popMatrix();

            GlStateManager.disableBlend();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            if (lighting) GlStateManager.enableLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }
}
