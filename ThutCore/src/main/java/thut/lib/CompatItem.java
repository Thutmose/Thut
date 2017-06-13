package thut.lib;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class CompatItem extends Item
{
    @SideOnly(Side.CLIENT)
    protected List<ItemStack> getTabItems(Item itemIn, CreativeTabs tab)
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        List<ItemStack> stacks = getTabItems(this, tab);
        if (stacks != null) for (ItemStack stack : stacks)
            list.add(stack);
    }

    // 1.10
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn,
            EnumHand hand)
    {
        return new ActionResult<>(EnumActionResult.PASS, itemStackIn);
    }

    // 1.10
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        return EnumActionResult.PASS;
    }

    // 1.11
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        return onItemRightClick(player.getHeldItem(hand), world, player, hand);
    }

    // 1.11
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return onItemUse(playerIn.getHeldItem(hand), playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ);
    }
}
