package thut.core.common.items;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidContainerItem;
import thut.api.ThutItems;
import thut.api.maths.ExplosionCustom;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class ItemSpout extends Item
{

    public ItemSpout()
    {
        super();
        this.setHasSubtypes(true);
        this.setUnlocalizedName("spout");
        this.setCreativeTab(ThutCore.tabThut);
        ThutItems.spout = this;
    }

    public ArrayList<ItemStack> getTanks(EntityPlayer player)
    {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

        for (ItemStack stack : player.inventory.mainInventory)
        {
            if (stack != null && stack.getItem() instanceof IFluidContainerItem)
            {
                IFluidContainerItem tank = (IFluidContainerItem) stack.getItem();
                if (tank.getFluid(stack) != null) ret.add(stack);
            }
        }

        return ret;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World world, EntityPlayer player,
            EnumHand hand)
    {
        if (itemstack.getItemDamage() != 15) return super.onItemRightClick(itemstack, world, player, hand);

        Vector3 v = Vector3.getNewVector();
        int range = 5;
        int num = 1;
        float power = 0.11f;
        for (int i = 0; i < num; i++)
        {
            v.set(player).addTo(range * (Math.random() - 0.5), range * (Math.random() - 0.5),
                    range * (Math.random() - 0.5));
            ExplosionCustom boom = new ExplosionCustom(world, player, v, power);
            boom.doExplosion();
        }
        return super.onItemRightClick(itemstack, world, player, hand);
    }

    // TODO move this to real method
    @Override
    public EnumActionResult onItemUse(ItemStack stackIn, EntityPlayer playerIn, World worldIn, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        int toDrain = 0;
        Vector3 hit = Vector3.getNewVector().set(pos);
        Vector3 next = hit.offset(side);

        boolean full = !playerIn.isSneaking();
        ArrayList<ItemStack> tanks = getTanks(playerIn);
        if (tanks.size() == 0) return EnumActionResult.FAIL;

        if (next.getBlockMaterial(worldIn).isReplaceable())
        {
            for (ItemStack stack : tanks)
            {
                IFluidContainerItem tank = (IFluidContainerItem) stack.getItem();
                Fluid f1 = tank.getFluid(stack).getFluid();
                if (!f1.canBePlacedInWorld())
                {
                    continue;
                }
                Block b = f1.getBlock();
                if (b instanceof BlockFluidBase)
                {
                    BlockFluidBase block = (BlockFluidBase) b;
                    int maxMeta = block.getMaxRenderHeightMeta();

                    int metaDiff = full ? 16 : 1;

                    toDrain = maxMeta == 0 ? 1000 : (int) (metaDiff * 1000f / (maxMeta + 1));
                    next.setBlock(worldIn, b, metaDiff - 1, 3);
                    tank.drain(stack, toDrain, !playerIn.capabilities.isCreativeMode);
                    break;
                }
                else
                {
                    tank.drain(stack, 1000, !playerIn.capabilities.isCreativeMode);
                    next.setBlock(worldIn, b, 0, 3);
                }
            }
        }

        return EnumActionResult.FAIL;
    }
}
