package thut.tech.common.blocks.lift;

import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.ThutBlocks;
import thut.tech.common.TechCore;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.items.ItemLinker;

public class BlockLift extends Block implements ITileEntityProvider
{
    public static enum EnumType implements IStringSerializable
    {
        LIFT(0, "lift"), CONTROLLER(1, "liftcontroller");

        private static final EnumType[] META_LOOKUP = new EnumType[values().length];
        static
        {
            EnumType[] var0 = values();
            int var1 = var0.length;

            for (int var2 = 0; var2 < var1; ++var2)
            {
                EnumType var3 = var0[var2];
                META_LOOKUP[var3.getMetadata()] = var3;
            }
        }
        public static EnumType byMetadata(int meta)
        {
            if (meta < 0 || meta >= META_LOOKUP.length)
            {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }
        private final int meta;

        private final String name;

        private final String unlocalizedName;

        private EnumType(int meta, String name)
        {
            this(meta, name, name);
        }

        private EnumType(int meta, String name, String unlocalizedName)
        {
            this.meta = meta;
            this.name = name;
            this.unlocalizedName = unlocalizedName;
        }

        public int getMetadata()
        {
            return this.meta;
        }

        @Override
        public String getName()
        {
            return this.name;
        }

        public String getUnlocalizedName()
        {
            return this.unlocalizedName;
        }

        @Override
        public String toString()
        {
            return this.name;
        }
    }
    public static final PropertyEnum<EnumType> VARIANT = PropertyEnum.create("variant", EnumType.class,
            new Predicate<EnumType>()
            {
                @Override
                public boolean apply(EnumType type)
                {
                    return type.getMetadata() < 4;
                }
            });
    public static final PropertyBool           CALLED  = PropertyBool.create("called");

    public static final PropertyBool           CURRENT = PropertyBool.create("current");

    public int size = 5;

    public BlockLift()
    {
        super(Material.iron);
        setHardness(3.5f);
        this.setUnlocalizedName("lift");
        this.setTickRandomly(true);
        this.setCreativeTab(TechCore.tabThut);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, EnumType.CONTROLLER)
                .withProperty(CALLED, Boolean.valueOf(false)).withProperty(CURRENT, Boolean.valueOf(false)));
        ThutBlocks.lift = this;
    }

    /** Can this block provide power. Only wire currently seems to have this
     * change based on its state. */
    @Override
    public boolean canProvidePower()
    {
        return true;
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] { VARIANT, CALLED, CURRENT });
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
        if (metadata == 1) { return new TileEntityLiftAccess(); }
        return new TileEntityLiftAccess();
    }

    @Override
    /** Gets the metadata of the item this Block can drop. This method is called
     * when the block gets destroyed. It returns the metadata of the dropped
     * item based on the old metadata of the block. */
    public int damageDropped(IBlockState state)
    {
        return this.getMetaFromState(state);
    }

    public EnumFacing getFacingfromEntity(EntityLiving e)
    {
        EnumFacing side = null;
        double angle = e.rotationYaw % 360;

        if (angle > 315 || angle <= 45) { return EnumFacing.SOUTH; }
        if (angle > 45 && angle <= 135) { return EnumFacing.WEST; }
        if (angle > 135 && angle <= 225) { return EnumFacing.NORTH; }
        if (angle > 225 && angle <= 315) { return EnumFacing.EAST; }

        return side;
    }

    @Override
    /** Convert the BlockState into the correct metadata value */
    public int getMetaFromState(IBlockState state)
    {
        int ret = state.getValue(VARIANT).meta;
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
        return getDefaultState().withProperty(VARIANT, EnumType.byMetadata(typeMeta)).withProperty(CALLED, called)
                .withProperty(CURRENT, current);
    }

    @Override
    public int getStrongPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side)
    {
        return 0;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    @SideOnly(Side.CLIENT)

    /** returns a list of blocks with the same ID, but different meta (eg: wood
     * returns 4 blocks) */
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List)
    {
        for (int j = 0; j < 2; j++)
        {
            par3List.add(new ItemStack(par1, 1, j));
        }
    }

    ////////////////////////////////////////////////////// RedStone
    ////////////////////////////////////////////////////// stuff/////////////////////////////////////////////////
    @Override
    public int getWeakPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side)
    {
        if (state.getValue(VARIANT) == EnumType.CONTROLLER)
        {
            TileEntityLiftAccess te = (TileEntityLiftAccess) worldIn.getTileEntity(pos);
            if (te.lift != null && (int) te.lift.posY == te.getPos().getY() - 2 && te.lift.motionY == 0) return 15;
        }
        return 0;
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
    public boolean onBlockActivated(World worldObj, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (state.getValue(VARIANT) == EnumType.LIFT)
        {
            ItemStack[][] stacks;
            TileEntityLiftAccess te = (TileEntityLiftAccess) worldObj.getTileEntity(pos);
            stacks = EntityLift.checkBlocks(worldObj, te, pos);
            if (stacks != null && !worldObj.isRemote && player.getHeldItem() != null
                    && player.getHeldItem().getItem() instanceof ItemLinker)
            {
                EntityLift.removeBlocks(worldObj, te, pos);
                EntityLift lift = new EntityLift(worldObj, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                lift.blocks = stacks;
                lift.boundMax = te.boundMax.copy();
                lift.boundMin = te.boundMin.copy();
                lift.owner = player.getUniqueID();
                worldObj.spawnEntityInWorld(lift);
                player.addChatMessage(new ChatComponentText("Sucessfully Made Lift"));
                return true;
            }
            else if (te != null && side == EnumFacing.UP)
            {
                te.doButtonClick(side, hitX, hitY, hitZ);
                return true;
            }
            return false;
        }
        else if (state.getValue(VARIANT) == EnumType.CONTROLLER)
        {
            TileEntityLiftAccess te = (TileEntityLiftAccess) worldObj.getTileEntity(pos);
            if (te != null && (!te.isSideOn(side)
                    || (player.getHeldItem() != null && player.getHeldItem().getItem() == Items.stick)))
            {
                if (player.getHeldItem() != null
                        && (player.getHeldItem().getItem().getUnlocalizedName().toLowerCase().contains("wrench")
                                || player.getHeldItem().getItem().getUnlocalizedName().toLowerCase()
                                        .contains("screwdriver")
                                || player.getHeldItem().getItem() instanceof ItemLinker
                                || player.getHeldItem().getItem() == Items.stick))
                {
                    if (!worldObj.isRemote)
                    {
                        te.setSide(side, !te.isSideOn(side));
                        te.markDirty();
                    }
                    return true;
                }
            }
            else if (te != null && te.isSideOn(side))
            {
                if (player.getHeldItem() != null
                        && (player.getHeldItem().getItem().getUnlocalizedName().toLowerCase().contains("wrench")
                                || player.getHeldItem().getItem().getUnlocalizedName().toLowerCase().contains(
                                        "screwdriver")
                                || player.getHeldItem().getItem() instanceof ItemLinker))
                {
                    if (!worldObj.isRemote)
                    {
                        te.setSidePage(side, (te.getSidePage(side) + 1) % 4);
                        te.markDirty();
                    }
                    return true;
                }
                else
                {
                    if (te != null) te.doButtonClick(side, hitX, hitY, hitZ);
                    return true;
                }
            }
            else if (te == null) new Exception().printStackTrace();
        }
        return true;
    }

    @Override
    /** Called when a block is placed using its ItemBlock. Args: World, X, Y, Z,
     * side, hitX, hitY, hitZ, block metadata */
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
            int meta, EntityLivingBase placer)
    {
        return getStateFromMeta(meta);
    }

    /** Rotate the block. For vanilla blocks this rotates around the axis passed
     * in (generally, it should be the "face" that was hit). Note: for mod
     * blocks, this is up to the block and modder to decide. It is not mandated
     * that it be a rotation around the face, but could be a rotation to orient
     * *to* that face, or a visiting of possible rotations. The method should
     * return true if the rotation was successful though.
     *
     * @param worldObj
     *            The world
     * @param x
     *            X position
     * @param y
     *            Y position
     * @param z
     *            Z position
     * @param axis
     *            The axis to rotate around
     * @return True if the rotation was successful, False if the rotation
     *         failed, or is not possible */
    @Override
    public boolean rotateBlock(World worldObj, BlockPos pos, EnumFacing axis)
    {
        return false;// RotationHelper.rotateVanillaBlock(this, worldObj, x, y,
                     // z, axis);
    }

    @Override
    /** Ticks the block if it's been scheduled */
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityLiftAccess)
        {
            // ((TileEntityLiftAccess) te).notifySurroundings();
        }
    }
}
