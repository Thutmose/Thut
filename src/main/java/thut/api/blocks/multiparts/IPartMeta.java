package thut.api.blocks.multiparts;

import codechicken.lib.vec.BlockCoord;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface IPartMeta
{
    public int getMetadata();
    
    public World getWorld();

    public Block getBlock();
    
    public BlockCoord getPos();
    
    public TileEntity getTileEntity();
}