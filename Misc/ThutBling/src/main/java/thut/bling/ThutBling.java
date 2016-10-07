package thut.bling;

import java.awt.Color;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import thut.bling.client.item.TextureHandler;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.x3d.X3dModel;
import thut.core.common.ThutCore;
import thut.wearables.EnumWearable;

@Mod(modid = ThutBling.MODID, name = "Thut's Bling", version = ThutBling.VERSION)
public class ThutBling
{
    public static final String MODID   = "thut_bling";
    public static final String VERSION = "1.0.0";

    @SidedProxy
    public static CommonProxy  proxy;
    @Instance(value = MODID)
    public static ThutBling    instance;
    public static Item         bling;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        bling = new ItemBling().setRegistryName(MODID, "bling");
        bling.setCreativeTab(ThutCore.tabThut);
        GameRegistry.register(bling);
        proxy.preInit(e);
    }

    public static class CommonProxy
    {
        public void preInit(FMLPreInitializationEvent event)
        {
        }

        public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
        {
        }
    }

    public static class ServerProxy extends CommonProxy
    {
    }

    public static class ClientProxy extends CommonProxy
    {
        Map<EnumWearable, X3dModel[]>         models   = Maps.newHashMap();
        Map<EnumWearable, ResourceLocation[]> textures = Maps.newHashMap();

        @Override
        public void preInit(FMLPreInitializationEvent event)
        {
            TextureHandler.registerItemModels();
        }

        @Override
        public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
        {
            if (slot == EnumWearable.EYE)
            {
                GlStateManager.pushMatrix();
                Minecraft.getMinecraft().renderEngine
                        .bindTexture(new ResourceLocation(MODID, "textures/items/eye.png"));
                GL11.glTranslated(-0.25, -0.175, -0.251);
                Tessellator tessellator = Tessellator.getInstance();
                VertexBuffer vertexbuffer = tessellator.getBuffer();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                double height = 0.5;
                double width = 0.5;
                vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                vertexbuffer.pos(0.0D, height, 0.0D).tex(0.0D, 1).color(255, 255, 255, 255).endVertex();
                vertexbuffer.pos(width, height, 0.0D).tex(1, 1).color(255, 255, 255, 255).endVertex();
                vertexbuffer.pos(width, 0.0D, 0.0D).tex(1, 0).color(255, 255, 255, 255).endVertex();
                vertexbuffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0).color(255, 255, 255, 255).endVertex();
                tessellator.draw();
                GL11.glPopMatrix();
                return;
            }

            X3dModel[] model = models.get(slot);
            ResourceLocation[] tex = textures.get(slot);
            if (model == null)
            {
                if (slot == EnumWearable.WAIST)
                {
                    model = new X3dModel[1];
                    tex = new ResourceLocation[2];
                    model[0] = new X3dModel(new ResourceLocation(ThutBling.MODID, "models/worn/belt.x3d"));
                    tex[0] = new ResourceLocation("minecraft", "textures/items/diamond.png");
                    tex[1] = new ResourceLocation(ThutBling.MODID, "textures/worn/belt.png");
                    models.put(slot, model);
                    textures.put(slot, tex);
                }
                if (slot == EnumWearable.WRIST)
                {
                    model = new X3dModel[1];
                    tex = new ResourceLocation[2];
                    model[0] = new X3dModel(new ResourceLocation(ThutBling.MODID, "models/worn/belt.x3d"));
                    tex[0] = new ResourceLocation("minecraft", "textures/items/diamond.png");
                    tex[1] = new ResourceLocation(ThutBling.MODID, "textures/worn/belt.png");
                    models.put(slot, model);
                    textures.put(slot, tex);
                }
                if (slot == EnumWearable.ANKLE)
                {
                    model = new X3dModel[1];
                    tex = new ResourceLocation[2];
                    model[0] = new X3dModel(new ResourceLocation(ThutBling.MODID, "models/worn/belt.x3d"));
                    tex[0] = new ResourceLocation("minecraft", "textures/items/diamond.png");
                    tex[1] = new ResourceLocation(ThutBling.MODID, "textures/worn/belt.png");
                    models.put(slot, model);
                    textures.put(slot, tex);
                }
                if (slot == EnumWearable.FINGER)
                {
                    model = new X3dModel[1];
                    tex = new ResourceLocation[2];
                    model[0] = new X3dModel(new ResourceLocation(ThutBling.MODID, "models/worn/belt.x3d"));
                    tex[0] = new ResourceLocation("minecraft", "textures/items/diamond.png");
                    tex[1] = new ResourceLocation(ThutBling.MODID, "textures/worn/belt.png");
                    models.put(slot, model);
                    textures.put(slot, tex);
                }
                if (slot == EnumWearable.EAR)
                {
                    model = new X3dModel[1];
                    tex = new ResourceLocation[2];
                    model[0] = new X3dModel(new ResourceLocation(ThutBling.MODID, "models/worn/belt.x3d"));
                    tex[0] = new ResourceLocation("minecraft", "textures/items/diamond.png");
                    tex[1] = new ResourceLocation(ThutBling.MODID, "textures/worn/belt.png");
                    models.put(slot, model);
                    textures.put(slot, tex);
                }
                if (slot == EnumWearable.HAT)
                {
                    model = new X3dModel[2];
                    tex = new ResourceLocation[2];
                    model[0] = new X3dModel(new ResourceLocation(ThutBling.MODID, "models/worn/hat.x3d"));
                    model[1] = new X3dModel(new ResourceLocation(ThutBling.MODID, "models/worn/hat.x3d"));
                    tex[0] = new ResourceLocation(ThutBling.MODID, "textures/worn/hat.png");
                    tex[1] = new ResourceLocation(ThutBling.MODID, "textures/worn/hat2.png");
                    models.put(slot, model);
                    textures.put(slot, tex);
                }
                if (slot == EnumWearable.NECK)
                {
                    model = new X3dModel[1];
                    tex = new ResourceLocation[2];
                    model[0] = new X3dModel(new ResourceLocation(ThutBling.MODID, "models/worn/belt.x3d"));
                    tex[0] = new ResourceLocation("minecraft", "textures/items/diamond.png");
                    tex[1] = new ResourceLocation(ThutBling.MODID, "textures/worn/belt.png");
                    models.put(slot, model);
                    textures.put(slot, tex);
                }
            }
            if (model == null) return;
            Color colour;
            float s, sy, sx, sz, dx, dy, dz;
            int brightness = wearer.getBrightnessForRender(partialTicks);
            EnumDyeColor ret;
            switch (slot)
            {
            case ANKLE:
                dx = 0.f;
                dy = .06f;
                dz = 0.f;
                s = 0.475f;
                sx = 1.05f * s / 2;
                sy = s * 1.8f / 2;
                sz = s / 2;
                if (stack.hasTagCompound() && stack.getTagCompound().hasKey("gem"))
                {
                    tex[0] = new ResourceLocation(stack.getTagCompound().getString("gem"));
                }
                else
                {
                    tex[0] = null;
                }
                // Second pass with colour.
                GL11.glPushMatrix();
                GL11.glRotated(90, 1, 0, 0);
                GL11.glRotated(180, 0, 0, 1);
                GL11.glTranslatef(dx, dy, dz);
                GL11.glScalef(sx, sy, sz);
                renderModel(stack, "main", "gem", model[0], tex, brightness);
                GL11.glPopMatrix();
                break;
            case BACK:
                break;
            case EAR:
                dx = 0.0f;
                dy = .175f;
                dz = 0.0f;
                s = 0.475f;
                sx = s / 4;
                sy = s / 4;
                sz = s / 4;
                if (stack.hasTagCompound() && stack.getTagCompound().hasKey("gem"))
                {
                    tex[0] = new ResourceLocation(stack.getTagCompound().getString("gem"));
                }
                else
                {
                    tex[0] = null;
                }
                // Second pass with colour.
                GL11.glPushMatrix();
                GL11.glRotated(90, 1, 0, 0);
                GL11.glRotated(180, 0, 0, 1);
                GL11.glTranslatef(dx, dy, dz);
                GL11.glScalef(sx, sy, sz);
                renderModel(stack, "main", "gem", model[0], tex, brightness);
                GL11.glPopMatrix();
                break;
            case EYE:
                break;
            case FINGER:
                dx = 0.0f;
                dy = .175f;
                dz = 0.0f;
                s = 0.475f;
                sx = s / 4;
                sy = s / 4;
                sz = s / 4;
                if (stack.hasTagCompound() && stack.getTagCompound().hasKey("gem"))
                {
                    tex[0] = new ResourceLocation(stack.getTagCompound().getString("gem"));
                }
                else
                {
                    tex[0] = null;
                }
                // Second pass with colour.
                GL11.glPushMatrix();
                GL11.glRotated(90, 1, 0, 0);
                GL11.glRotated(180, 0, 0, 1);
                GL11.glTranslatef(dx, dy, dz);
                GL11.glScalef(sx, sy, sz);
                Minecraft.getMinecraft().renderEngine.bindTexture(tex[1]);
                renderModel(stack, "main", "gem", model[0], tex, brightness);
                GL11.glPopMatrix();
                break;
            case HAT:
                Minecraft minecraft = Minecraft.getMinecraft();
                GlStateManager.pushMatrix();
                s = 0.285f;
                GL11.glScaled(s, -s, -s);
                minecraft.renderEngine.bindTexture(tex[0]);
                model[0].renderAll();
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GL11.glScaled(s * 0.995f, -s * 0.995f, -s * 0.995f);
                minecraft.renderEngine.bindTexture(tex[1]);
                ret = EnumDyeColor.RED;
                if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
                {
                    int damage = stack.getTagCompound().getInteger("dyeColour");
                    ret = EnumDyeColor.byDyeDamage(damage);
                }
                colour = new Color(ret.getMapColor().colorValue + 0xFF000000);
                int[] col2 = { colour.getRed(), colour.getBlue(), colour.getGreen(), 255, brightness };
                for (IExtendedModelPart part1 : model[1].getParts().values())
                {
                    part1.setRGBAB(col2);
                }
                model[1].renderAll();
                GL11.glColor3f(1, 1, 1);
                GlStateManager.popMatrix();
                break;
            case NECK:
                dx = 0;
                dy = -.0f;
                dz = -0.03f;
                s = 0.525f;
                if (wearer.getItemStackFromSlot(EntityEquipmentSlot.LEGS) == null)
                {
                    s = 0.465f;
                }
                if (stack.hasTagCompound() && stack.getTagCompound().hasKey("gem"))
                {
                    tex[0] = new ResourceLocation(stack.getTagCompound().getString("gem"));
                }
                else
                {
                    tex[0] = null;
                }
                GL11.glPushMatrix();
                GL11.glRotated(90, 1, 0, 0);
                GL11.glRotated(180, 0, 0, 1);
                GL11.glTranslatef(dx, dy, dz);
                GL11.glScalef(s, s, s);
                String colorpart = "main";
                String itempart = "gem";
                ret = EnumDyeColor.YELLOW;
                if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
                {
                    int damage = stack.getTagCompound().getInteger("dyeColour");
                    ret = EnumDyeColor.byDyeDamage(damage);
                }
                colour = new Color(ret.getMapColor().colorValue + 0xFF000000);
                int[] col = { colour.getRed(), colour.getBlue(), colour.getGreen(), 255, brightness };
                IExtendedModelPart part = model[0].getParts().get(colorpart);
                if (part != null)
                {
                    part.setRGBAB(col);
                    Minecraft.getMinecraft().renderEngine.bindTexture(tex[1]);
                    GlStateManager.scale(1, 1, .1);
                    model[0].renderPart(colorpart);
                }
                GL11.glColor3f(1, 1, 1);
                part = model[0].getParts().get(itempart);
                if (part != null && tex[0] != null)
                {
                    Minecraft.getMinecraft().renderEngine.bindTexture(tex[0]);
                    GlStateManager.scale(1, 1, 10);
                    model[0].renderPart(itempart);
                }
                GL11.glPopMatrix();
                break;
            case WAIST:
                dx = 0;
                dy = -.0f;
                dz = -0.6f;
                s = 0.525f;
                if (wearer.getItemStackFromSlot(EntityEquipmentSlot.LEGS) == null)
                {
                    s = 0.465f;
                }
                if (stack.hasTagCompound() && stack.getTagCompound().hasKey("gem"))
                {
                    tex[0] = new ResourceLocation(stack.getTagCompound().getString("gem"));
                }
                else
                {
                    tex[0] = null;
                }
                GL11.glPushMatrix();
                GL11.glRotated(90, 1, 0, 0);
                GL11.glRotated(180, 0, 0, 1);
                GL11.glTranslatef(dx, dy, dz);
                GL11.glScalef(s, s, s);
                renderModel(stack, "main", "gem", model[0], tex, brightness);
                GL11.glPopMatrix();
                break;
            case WRIST:
                dx = 0.f;
                dy = .06f;
                dz = 0.f;
                s = 0.475f;
                sx = 1.05f * s / 2;
                sy = s * 1.8f / 2;
                sz = s / 2;
                if (stack.hasTagCompound() && stack.getTagCompound().hasKey("gem"))
                {
                    tex[0] = new ResourceLocation(stack.getTagCompound().getString("gem"));
                }
                else
                {
                    tex[0] = null;
                }
                // Second pass with colour.
                GL11.glPushMatrix();
                GL11.glRotated(90, 1, 0, 0);
                GL11.glRotated(180, 0, 0, 1);
                GL11.glTranslatef(dx, dy, dz);
                GL11.glScalef(sx, sy, sz);
                renderModel(stack, "main", "gem", model[0], tex, brightness);
                GL11.glPopMatrix();
                break;
            default:
                break;
            }
            // TODO
        }

        private void renderModel(ItemStack stack, String colorpart, String itempart, X3dModel model,
                ResourceLocation[] tex, int brightness)
        {
            EnumDyeColor ret = EnumDyeColor.YELLOW;
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
            {
                int damage = stack.getTagCompound().getInteger("dyeColour");
                ret = EnumDyeColor.byDyeDamage(damage);
            }
            Color colour = new Color(ret.getMapColor().colorValue + 0xFF000000);
            int[] col = { colour.getRed(), colour.getBlue(), colour.getGreen(), 255, brightness };
            IExtendedModelPart part = model.getParts().get(colorpart);
            if (part != null)
            {
                part.setRGBAB(col);
                Minecraft.getMinecraft().renderEngine.bindTexture(tex[1]);
                model.renderPart(colorpart);
            }
            GL11.glColor3f(1, 1, 1);
            part = model.getParts().get(itempart);
            if (part != null && tex[0] != null)
            {
                Minecraft.getMinecraft().renderEngine.bindTexture(tex[0]);
                model.renderPart(itempart);
            }
        }
    }
}
