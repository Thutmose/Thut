package thut.bling;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;

public class ItemBling extends Item implements IWearable
{
    public static Map<String, EnumWearable> wearables = Maps.newHashMap();

    static
    {
        wearables.put("ring", EnumWearable.FINGER);
        wearables.put("neck", EnumWearable.NECK);
        wearables.put("wrist", EnumWearable.WRIST);
        wearables.put("eye", EnumWearable.EYE);
        wearables.put("ankle", EnumWearable.ANKLE);
        wearables.put("ear", EnumWearable.EAR);
        wearables.put("waist", EnumWearable.WAIST);
        wearables.put("hat", EnumWearable.HAT);
        wearables.put("bag", EnumWearable.BACK);
    }

    public ItemBling()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean bool)
    {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
        {
            int damage = stack.getTagCompound().getInteger("dyeColour");
            EnumDyeColor colour = EnumDyeColor.byDyeDamage(damage);
            String s = I18n.format(colour.getUnlocalizedName());
            list.add(s);
        }
    }

    @Override
    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        ItemStack stack;
        for (String s : wearables.keySet())
        {
            stack = new ItemStack(itemIn);
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setString("type", s);
            subItems.add(stack);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        String name = super.getUnlocalizedName(stack);
        if (stack.hasTagCompound())
        {
            NBTTagCompound tag = stack.getTagCompound();
            String variant = "ring";
            if (tag != null)
            {
                String stackname = tag.getString("type");
                variant = stackname.toLowerCase(java.util.Locale.ENGLISH);
            }
            name = "item.bling." + variant;
        }
        return name;
    }

    @Override
    public EnumWearable getSlot(ItemStack stack)
    {
        String name = getUnlocalizedName(stack).replace("item.bling.", "");
        return wearables.get(name);
    }

    @Override
    public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
    {
        ThutBling.proxy.renderWearable(slot, wearer, stack, partialTicks);
    }

}
