package thut.core.common.items;

import thut.api.ThutItems;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.core.common.blocks.BlockFluid;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemSpreader extends Item
{

    public ItemSpreader()
    {
        super();
        this.setHasSubtypes(true);
        this.setUnlocalizedName("spreader");
        this.setCreativeTab(ThutCore.tabThut);
        ThutItems.spreader = this;
    }

    //TODO make this work again
    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World worldObj, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ)
    {
        // Block b = worldObj.getBlock(x, y, z);
        // if(b instanceof BlockFluid && !worldObj.isRemote)
        // {
        // BlockFluid fluid = (BlockFluid) b;
        // if(!fluid.solid)
        // {
        // fluid.trySpread(worldObj, new Vector3(x, y, z), true);
        // return true;
        // }
        // }

        return false;
    }
}
