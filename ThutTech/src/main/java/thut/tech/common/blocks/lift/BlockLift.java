package thut.tech.common.blocks.lift;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.ThutBlocks;
import thut.api.network.PacketHandler;
import thut.lib.CompatWrapper;
import thut.tech.Reference;
import thut.tech.common.TechCore;
import thut.tech.common.items.ItemLinker;

public class BlockLift extends Block implements ITileEntityProvider
{
    public static enum EnumType implements IStringSerializable
    {
        LIFT("lift"), CONTROLLER("liftcontroller");

        private final String name;

        private EnumType(String name)
        {
            this.name = name;
        }

        @Override
        public String getName()
        {
            return this.name;
        }
    }

    public static final PropertyEnum<EnumType> VARIANT = PropertyEnum.create("variant", EnumType.class);
    public static final PropertyBool           CALLED  = PropertyBool.create("called");

    public static final PropertyBool           CURRENT = PropertyBool.create("current");

    public static void init()
    {
        ThutBlocks.lift = new BlockLift().setRegistryName(Reference.MOD_ID, "lift");
    }

    protected BlockLift()
    {
        super(Material.IRON);
        setHardness(3.5f);
        this.setUnlocalizedName("lift");
        this.setCreativeTab(TechCore.tabThut);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, EnumType.CONTROLLER)
                .withProperty(CALLED, Boolean.valueOf(false)).withProperty(CURRENT, Boolean.valueOf(false)));
    }

    /** Can this block provide power. Only wire currently seems to have this
     * change based on its state. */
    @Override
    public boolean canProvidePower(IBlockState state)
    {
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] { VARIANT, CALLED, CURRENT });
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityLiftAccess();
    }

    /** Called throughout the code as a replacement for
     * ITileEntityProvider.createNewTileEntity Return the same thing you would
     * from that function. This will fall back to
     * ITileEntityProvider.createNewTileEntity(World) if this block is a
     * ITileEntityProvider
     *
     * @param metadata
     *            The Metadata of the current block
     * @return A instance of a class extending TileEntity */
    public TileEntity createTileEntity(World world, int metadata)
    {
        if (getStateFromMeta(metadata).getValue(VARIANT) == EnumType.LIFT) { return null; }
        return new TileEntityLiftAccess();
    }

    @Override
    /** Gets the metadata of the item this Block can drop. This method is called
     * when the block gets destroyed. It returns the metadata of the dropped
     * item based on the old metadata of the block. */
    public int damageDropped(IBlockState state)
    {
        return state.getValue(VARIANT) == EnumType.LIFT ? 0 : 1;
    }

    public Direction getFacingfromEntity(MobEntity e)
    {
        Direction side = null;
        double angle = e.rotationYaw % 360;

        if (angle > 315 || angle <= 45) { return Direction.SOUTH; }
        if (angle > 45 && angle <= 135) { return Direction.WEST; }
        if (angle > 135 && angle <= 225) { return Direction.NORTH; }
        if (angle > 225 && angle <= 315) { return Direction.EAST; }

        return side;
    }

    @Override
    /** Convert the BlockState into the correct metadata value */
    public int getMetaFromState(IBlockState state)
    {
        if (state.getBlock() != this) return state.getBlock().getMetaFromState(state);
        int ret = state.getValue(VARIANT).ordinal();
        if ((state.getValue(CALLED))) ret += 4;
        if ((state.getValue(CURRENT))) ret += 8;
        return ret;
    }

    @Override
    /** Convert the given metadata into a BlockState for this Block */
    public IBlockState getStateFromMeta(int meta)
    {
        int typeMeta = meta & 3;
        boolean called = ((meta / 4) & 1) > 0;
        boolean current = ((meta / 8) & 1) > 0;
        return getDefaultState().withProperty(VARIANT, EnumType.values()[typeMeta]).withProperty(CALLED, called)
                .withProperty(CURRENT, current);
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side)
    {
        return 0;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    @OnlyIn(Dist.CLIENT)

    /** returns a list of blocks with the same ID, but different meta (eg: wood
     * returns 4 blocks) */
    public void getSubBlocks(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List)
    {
        for (int j = 0; j < 2; j++)
        {
            par3List.add(new ItemStack(Item.getItemFromBlock(this), 1, j));
        }
    }

    ////////////////////////////////////////////////////// RedStone
    ////////////////////////////////////////////////////// stuff/////////////////////////////////////////////////
    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side)
    {
        return blockState.getValue(CURRENT) ? 15 : 0;
    }

    /** Called throughout the code as a replacement for block instanceof
     * BlockContainer Moving this to the Block base class allows for mods that
     * wish to extend vinella blocks, and also want to have a tile entity on
     * that block, may. Return true from this function to specify this block has
     * a tile entity.
     *
     * @param metadata
     *            Metadata of the current block
     * @return True if block has a tile entity, false otherwise */
    public boolean hasTileEntity(int metadata)
    {
        return metadata == 1;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, PlayerEntity playerIn,
            Hand hand, Direction side, float hitX, float hitY, float hitZ)
    {
        ItemStack heldItem = playerIn.getHeldItem(hand);
        boolean linkerOrStick = CompatWrapper.isValid(heldItem)
                && (heldItem.getItem().getUnlocalizedName().toLowerCase().contains("wrench")
                        || heldItem.getItem().getUnlocalizedName().toLowerCase().contains("screwdriver")
                        || heldItem.getItem() instanceof ItemLinker || heldItem.getItem() == Items.STICK);
        if (linkerOrStick && playerIn.isSneaking()) return false;

        if (CompatWrapper.isValid(heldItem) && !linkerOrStick && side == Direction.DOWN)
        {
            Block b = Block.getBlockFromItem(heldItem.getItem());
            if (b != null && state.getValue(VARIANT) == EnumType.CONTROLLER)
            {
                IBlockState newState = CompatWrapper.getBlockStateFromMeta(b, heldItem.getItemDamage());
                TileEntityLiftAccess te = (TileEntityLiftAccess) worldIn.getTileEntity(pos);
                te.copiedState = newState;
                if (!te.getWorld().isRemote) PacketHandler.sendTileUpdate(te);
                return true;
            }

            return false;
        }
        if (state.getValue(VARIANT) == EnumType.LIFT)
        {
            return false;
        }
        else if (state.getValue(VARIANT) == EnumType.CONTROLLER)
        {
            TileEntityLiftAccess te = (TileEntityLiftAccess) worldIn.getTileEntity(pos);
            if (te != null
                    && (!te.isSideOn(side) || (CompatWrapper.isValid(heldItem) && heldItem.getItem() == Items.STICK)))
            {
                if (linkerOrStick)
                {
                    if (!worldIn.isRemote)
                    {
                        te.setSide(side, !te.isSideOn(side));
                        if (worldIn instanceof WorldServer) te.sendUpdate((ServerPlayerEntity) playerIn);
                    }
                    return true;
                }
            }
            else if (te != null && te.isSideOn(side))
            {
                if (CompatWrapper.isValid(heldItem)
                        && (heldItem.getItem().getUnlocalizedName().toLowerCase().contains("wrench")
                                || heldItem.getItem().getUnlocalizedName().toLowerCase().contains("screwdriver")
                                || heldItem.getItem() instanceof ItemLinker))
                {
                    if (!worldIn.isRemote && !te.editFace[side.ordinal()] && !te.floorDisplay[side.ordinal()])
                    {
                        te.setSidePage(side, (te.getSidePage(side) + 1) % 8);
                        if (playerIn instanceof ServerPlayerEntity) te.sendUpdate((ServerPlayerEntity) playerIn);
                        PacketHandler.sendTileUpdate(te);
                    }
                    return true;
                }
                else
                {
                    return te.doButtonClick(playerIn, side, hitX, hitY, hitZ);
                }
            }
            else if (te == null) new Exception().printStackTrace();
        }
        return false;
    }

    @Override
    /** Called when a block is placed using its ItemBlock. Args: World, X, Y, Z,
     * side, hitX, hitY, hitZ, block metadata */
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, Direction facing, float hitX, float hitY,
            float hitZ, int meta, LivingEntity placer)
    {
        return getStateFromMeta(meta);
    }

    @Deprecated
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        TileEntityLiftAccess tile = (TileEntityLiftAccess) worldIn.getTileEntity(pos);
        if (tile != null && tile.copiedState != null && tile.getWorld().isRemote) return tile.copiedState;
        return state;
    }
}
