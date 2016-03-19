package thut.tech.common.blocks.door;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thut.tech.common.TechCore;

public class BlockDoor extends Block implements ITileEntityProvider
{
    public static BlockDoor instance;

    public BlockDoor()
    {
        super(Material.iron);
        instance = this;
        setHardness(3.5f);
        setLightOpacity(0);
        this.setCreativeTab(TechCore.tabThut);
        this.setUnlocalizedName("door");
    }

    @Override
    public boolean isFullCube()
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    /** The type of render function called. 3 for standard block models, 2 for
     * TESR's, 1 for liquids, -1 is no render */
    public int getRenderType()
    {
        return 2;
    }

    @Override
    public boolean onBlockActivated(World worldObj, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntityDoor door = (TileEntityDoor) worldObj.getTileEntity(pos);
        Block block;
        if (player.getHeldItem() != null && (block = Block.getBlockFromItem(player.getHeldItem().getItem())) != null)
        {
            door.state = block.getStateFromMeta(player.getHeldItem().getItemDamage());
        }
        else
        {
            door.createDoor();
            System.out.println(door.getBounds(0));
        }

        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityDoor();
    }

    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return true;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntityDoor door = (TileEntityDoor) worldIn.getTileEntity(pos);
        if (door == null) return new AxisAlignedBB(pos, pos.add(1, 1, 1));
        return door.getBounds(0);
    }

}
