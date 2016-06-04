package thut.tech.common.items;

import java.util.List;
import java.util.UUID;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.ThutBlocks;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.BlockLift;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;

public class ItemLinker extends Item
{
    public static Item instance;

    public ItemLinker()
    {
        super();
        this.setHasSubtypes(true);
        this.setUnlocalizedName("devicelinker");
        this.setCreativeTab(TechCore.tabThut);
        instance = this;
    }

    @Override
    /** If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client. */
    public boolean getShareTag()
    {
        return true;
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (stack.getTagCompound() == null)
        {
            return EnumActionResult.PASS;
        }
        else
        {
            IBlockState state = worldIn.getBlockState(pos);

            if (state.getBlock() == ThutBlocks.lift
                    && state.getValue(BlockLift.VARIANT) == BlockLift.EnumType.CONTROLLER && !playerIn.isSneaking())
            {
                TileEntityLiftAccess te = (TileEntityLiftAccess) worldIn.getTileEntity(pos);
                te.setSide(facing, true);
                return EnumActionResult.SUCCESS;
            }

            UUID liftID;
            try
            {
                liftID = UUID.fromString(stack.getTagCompound().getString("lift"));
            }
            catch (Exception e)
            {
                return EnumActionResult.FAIL;
            }

            EntityLift lift = EntityLift.getLiftFromUUID(liftID, worldIn.isRemote);

            if (playerIn.isSneaking() && lift != null && state.getBlock() == ThutBlocks.lift
                    && state.getValue(BlockLift.VARIANT) == BlockLift.EnumType.CONTROLLER)
            {
                TileEntityLiftAccess te = (TileEntityLiftAccess) worldIn.getTileEntity(pos);
                te.setLift(lift);
                int floor = te.getButtonFromClick(facing, hitX, hitY, hitZ);
                te.setFloor(floor);

                String message = "msg.floorSet.name";

                if (worldIn.isRemote) playerIn.addChatMessage(new TextComponentTranslation(message, floor));
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    public void setLift(EntityLift lift, ItemStack stack)
    {
        if (stack.getTagCompound() == null)
        {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setString("lift", lift.id.toString());
    }

    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        subItems.add(new ItemStack(itemIn, 1, 0));
        subItems.add(TechCore.getInfoBook());
    }
}
