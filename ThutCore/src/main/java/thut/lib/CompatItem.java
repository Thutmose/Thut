package thut.lib;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class CompatItem extends Item
{
    @OnlyIn(Dist.CLIENT)
    protected List<ItemStack> getTabItems(Item itemIn, CreativeTabs tab)
    {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
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
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, PlayerEntity playerIn,
            Hand hand)
    {
        return new ActionResult<>(ActionResultType.PASS, itemStackIn);
    }

    // 1.10
    public ActionResultType onItemUse(ItemStack stack, PlayerEntity playerIn, World worldIn, BlockPos pos,
            Hand hand, Direction facing, float hitX, float hitY, float hitZ)
    {
        return ActionResultType.PASS;
    }

    // 1.11
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        return onItemRightClick(player.getHeldItem(hand), world, player, hand);
    }

    // 1.11
    @Override
    public ActionResultType onItemUse(PlayerEntity playerIn, World worldIn, BlockPos pos, Hand hand,
            Direction side, float hitX, float hitY, float hitZ)
    {
        return onItemUse(playerIn.getHeldItem(hand), playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ);
    }
}
