package thut.bling;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;

public class ItemBling extends Item implements IWearable
{
    public static Map<String, EnumWearable>    wearables = Maps.newHashMap();
    public static Map<EnumWearable, ItemStack> defaults  = Maps.newHashMap();
    public static List<String>                 names     = Lists.newArrayList();
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
        names.addAll(wearables.keySet());
        Collections.sort(names);
    }

    public void initDefaults()
    {
        ItemStack stack;
        for (int i = 0; i < names.size(); i++)
        {
            String s = names.get(i);
            stack = new ItemStack(this, 1, i);
            defaults.put(wearables.get(s), stack.copy());
        }
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
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("gemTag"))
        {
            ItemStack gem = ItemStack.loadItemStackFromNBT(stack.getTagCompound().getCompoundTag("gemTag"));
            if (gem != null)
            {
                try
                {
                    list.add(gem.getDisplayName());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn,
            EnumHand hand)
    {
        if (getSlot(itemStackIn) == EnumWearable.BACK)
        {
            playerIn.openGui(ThutBling.instance, 0, worldIn, 0, 0, 0);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
        }
        return super.onItemRightClick(itemStackIn, worldIn, playerIn, hand);
    }

    @Override
    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        ItemStack stack;
        for (int i = 0; i < names.size(); i++)
        {
            stack = new ItemStack(itemIn, 1, i);
            subItems.add(stack);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        String name = super.getUnlocalizedName(stack);
        String variant = null;
        if (stack.hasTagCompound())
        {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey("type"))
            {
                String stackname = tag.getString("type");
                variant = stackname.toLowerCase(java.util.Locale.ENGLISH);
            }
        }
        if (variant == null) variant = names.get(stack.getItemDamage() % names.size());
        name = "item.bling." + variant;
        return name;
    }

    @Override
    public EnumWearable getSlot(ItemStack stack)
    {
        if (stack.hasTagCompound())
        {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey("type")) { return wearables.get(tag.getString("type")); }
        }
        return wearables.get(names.get(stack.getItemDamage() % names.size()));
    }

    @Override
    public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
    {
        ThutBling.proxy.renderWearable(slot, wearer, stack, partialTicks);
    }

}
