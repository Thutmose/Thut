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
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.ThutBlocks;
import thut.api.maths.Vector3;
import thut.tech.common.TechCore;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.items.ItemLinker;

public class BlockLift extends Block implements ITileEntityProvider
{
    public static final PropertyEnum<EnumType> VARIANT = PropertyEnum.create("variant", EnumType.class, new Predicate<EnumType>()
    {
        public boolean apply(EnumType type)
        {
            return type.getMetadata() < 4;
        }
    });
    public static final PropertyBool CALLED  = PropertyBool.create("called");
    public static final PropertyBool CURRENT = PropertyBool.create("current");

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

    @Override
    /** Called when a block is placed using its ItemBlock. Args: World, X, Y, Z,
     * side, hitX, hitY, hitZ, block metadata */
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
            int meta, EntityLivingBase placer)
    {
        return getStateFromMeta(meta);
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
    public boolean onBlockActivated(World worldObj, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (state.getValue(VARIANT) == EnumType.LIFT)
        {
            ItemStack[][] stacks;
            TileEntityLiftAccess te = (TileEntityLiftAccess) worldObj.getTileEntity(pos);
            stacks = checkBlocks(worldObj, te, pos);
            if (stacks != null && !worldObj.isRemote && player.getHeldItem() != null
                    && player.getHeldItem().getItem() instanceof ItemLinker)
            {
                removeBlocks(worldObj, te, pos);
                EntityLift lift = new EntityLift(worldObj, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                lift.blocks = stacks;
                lift.corners = te.corners.clone();
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
            else if(te==null)
                new Exception().printStackTrace();
        }
        return true;
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

    public ItemStack[][] checkBlocks(World worldObj, TileEntityLiftAccess te, BlockPos pos)
    {
        
        int xMin = te.corners[0][0];
        int zMin = te.corners[0][1];
        int xMax = te.corners[1][0];
        int zMax = te.corners[1][1];
        
        ItemStack[][] ret = new ItemStack[(xMax-xMin) + 1][(zMax-zMin)+ 1];
        System.out.println(ret.length+" "+(zMax-zMin));
        Vector3 loc = Vector3.getNewVectorFromPool().set(pos);
        for (int i = xMin; i <= xMax; i++)
            for (int j = zMin; j <= zMax; j++)
            {
                if (!(i == 0 && j == 0))
                {
                    IBlockState state = loc.set(pos).addTo(i, 0, j).getBlockState(worldObj);
                    Block b;
                    if ((b = state.getBlock()).isNormalCube())
                    {
                        ret[i - xMin][j - zMin] = new ItemStack(b, 1, b.getMetaFromState(state));
                    }
                    else
                    {
                        loc.freeVectorFromPool();
                        return null;
                    }
                }
                else
                {
                    ret[i - xMin][j - zMin] = new ItemStack(this);
                }
            }
        loc.freeVectorFromPool();
        return ret;
    }

    public void removeBlocks(World worldObj, TileEntityLiftAccess te, BlockPos pos)
    {
        int xMin = te.corners[0][0];
        int zMin = te.corners[0][1];
        int xMax = te.corners[1][0];
        int zMax = te.corners[1][1];
        Vector3 loc = Vector3.getNewVectorFromPool();
        for (int i = xMin; i <= xMax; i++)
            for (int j = zMin; j <= zMax; j++)
                for (int k = 0; k < 1; k++)
                {
                    worldObj.setBlockToAir(loc.set(pos).add(i, k, j).getPos());
                }
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

    ////////////////////////////////////////////////////// RedStone
    ////////////////////////////////////////////////////// stuff/////////////////////////////////////////////////

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
    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IBlockState state)
    {
        return this.getMetaFromState(state);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityLiftAccess();
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
    /** Convert the BlockState into the correct metadata value */
    public int getMetaFromState(IBlockState state)
    {
        int ret = ((EnumType) state.getValue(VARIANT)).meta;
        if (((Boolean) state.getValue(CALLED))) ret += 4;
        if (((Boolean) state.getValue(CURRENT))) ret += 8;
        return ret;
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, new IProperty[] { VARIANT, CALLED, CURRENT });
    }

    public static enum EnumType implements IStringSerializable
    {
        LIFT(0, "lift"), CONTROLLER(1, "liftcontroller");

        private static final EnumType[] META_LOOKUP = new EnumType[values().length];
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

        public String toString()
        {
            return this.name;
        }

        public static EnumType byMetadata(int meta)
        {
            if (meta < 0 || meta >= META_LOOKUP.length)
            {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        public String getName()
        {
            return this.name;
        }

        public String getUnlocalizedName()
        {
            return this.unlocalizedName;
        }

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
    }
}
