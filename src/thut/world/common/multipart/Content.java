package thut.world.common.multipart;

import net.minecraft.world.World;
import thut.api.Blocks;
import thut.world.common.blocks.crystals.CrystalPart;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartConverter;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.TMultiPart;

public class Content implements IPartFactory, IPartConverter
{
    @Override
    public TMultiPart createPart(String name, boolean client)
    {
        if(name.equals("crystal")) return new CrystalPart();
        
        return null;
    }
    
    public void init()
    {
        MultiPartRegistry.registerConverter(this);
        MultiPartRegistry.registerParts(this, new String[]{"crystal"});
    }

    @Override
    public boolean canConvert(int blockID)
    {
        return blockID == Blocks.sulfur.blockID;
    }

    @Override
    public TMultiPart convert(World world, BlockCoord pos)
    {
        int id = world.getBlockId(pos.x, pos.y, pos.z);
        int meta = world.getBlockMetadata(pos.x, pos.y, pos.z);
        if(id == Blocks.sulfur.blockID)
            return new CrystalPart(meta);
        
        return null;
    }
}
