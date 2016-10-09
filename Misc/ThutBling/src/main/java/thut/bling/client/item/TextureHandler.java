package thut.bling.client.item;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import thut.bling.ItemBling;
import thut.bling.ThutBling;

public class TextureHandler
{
    public static class Mesh implements ItemMeshDefinition
    {
        @Override
        public ModelResourceLocation getModelLocation(ItemStack stack)
        {
            NBTTagCompound tag = stack.getTagCompound();
            String variant = null;
            if (tag != null && tag.hasKey("type"))
            {
                String stackname = tag.getString("type");
                variant = stackname.toLowerCase(java.util.Locale.ENGLISH);
            }
            if (variant == null) variant = ItemBling.names.get(stack.getItemDamage() % ItemBling.names.size());
            return getLocation(variant);
        }
    }

    public static ModelResourceLocation getLocation(String name)
    {
        return new ModelResourceLocation(new ResourceLocation(ThutBling.MODID, "item/bling"),
                "type=" + name.toLowerCase(java.util.Locale.ENGLISH));
    }

    public static void registerItemModels()
    {
        ModelLoader.setCustomMeshDefinition(ThutBling.bling, new Mesh());
        for (String s : ItemBling.wearables.keySet())
        {
            registerItemVariant("type=" + s);
        }
    }

    private static void registerItemVariant(String variant)
    {
        ModelBakery.registerItemVariants(ThutBling.bling,
                new ModelResourceLocation(new ResourceLocation(ThutBling.MODID, "item/bling"), variant));
    }
}
