package thut.api.blocks.multiparts;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.MultiPartRegistry.IPartConverter;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;

import java.util.ArrayList;
import java.util.Arrays;

import thut.api.ThutBlocks;

public class Content implements IPartFactory, IPartConverter
{
    @Override
    public TMultiPart createPart(String name, boolean client)
    {
        return ThutBlocks.getPart(name);
    }
    public void init()
    {
        MultiPartRegistry.registerConverter(this);
        ArrayList<String> list = new ArrayList();
        list.addAll(ThutBlocks.parts2.keySet());
        MultiPartRegistry.registerParts(this, list.toArray(new String[0]));
    }

    @Override
    public Iterable<Block> blockTypes() {
    	if(ThutBlocks.parts.isEmpty())
    		return new ArrayList();
    	ArrayList ret = new ArrayList();
    	ret.addAll(ThutBlocks.parts.keySet());
        return ret;//Blocks.torch, Blocks.lever, Blocks.stone_button, Blocks.wooden_button, Blocks.redstone_torch, Blocks.unlit_redstone_torch
    }

    @Override
    public TMultiPart convert(World world, BlockCoord pos)
    {
        Block b = world.getBlock(pos.x, pos.y, pos.z);
        int meta = world.getBlockMetadata(pos.x, pos.y, pos.z);
        return ThutBlocks.getPart(b);
    }
}
