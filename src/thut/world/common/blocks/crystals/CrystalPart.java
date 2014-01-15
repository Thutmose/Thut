package thut.world.common.blocks.crystals;

import java.awt.List;
import java.util.ArrayList;

import thut.api.Blocks;
import thut.world.common.WorldCore;
import thut.world.common.blocks.fluids.BlockFluid;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.microblock.FaceMicroblock;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.minecraft.McBlockPart;
import codechicken.multipart.minecraft.McSidedMetaPart;

public class CrystalPart extends McSidedMetaPart {

	public static BlockCrystal block = (BlockCrystal) Blocks.sulfur;
    public static int[] sideMetaMap = new int[]{0, 1, 2, 3, 4, 5};
    public static int[] metaSideMap = new int[]{0, 1, 2, 3, 4, 5, 0, 0};
    
	public CrystalPart(int meta)
	{
		super(meta);
		metaSideMap = new int[]{0, 1, 2, 3, 4, 5, 0, 0};
		sideMetaMap = new int[]{0, 1, 2, 3, 4, 5};
	}
	
	
    public CrystalPart() {}


	@Override
    public Cuboid6 getBounds()
    {
        int m = meta & 7;
        if (m == 0)
            return new Cuboid6(0.125, 0, 0.125, 0.875, 0.125f, 0.875);
        if (m == 1)
            return new Cuboid6(0, 1-0.125f, 0, 0.875, 1, 0.875);
        if (m == 2)
            return new Cuboid6(0.125, 0.125, 0, 0.875, 0.875, 0.125f);
        if (m == 3)
            return new Cuboid6(0.125, 0.125, 1-0.125f, 0.875, 1, 0.875);
        if (m == 4)
            return new Cuboid6(0, 0.125, 0.125, 0.125f, 0.875, 0.875);
        if (m == 5)
            return new Cuboid6(1-0.125f, 0.125, 0.125, 1, 0.875, 0.875);
       // System.out.println(meta);
        return null;//falloff
    }

	@Override
	public Block getBlock() {
		return block;
	}
	
	@Override 
	public Iterable<ItemStack> getDrops()
	{
		return new ArrayList<ItemStack>();
	}
	
	@Override 
	public void harvest(MovingObjectPosition hit, EntityPlayer player)
	{
		ItemStack drop = new ItemStack(block);
		EntityItem toDrop = new EntityItem(player.worldObj, hit.blockX+0.5, hit.blockY+0.5, hit.blockZ+0.5);
		toDrop.setEntityItemStack(drop);
		player.worldObj.spawnEntityInWorld(toDrop);
		this.drop();
	}
	
	@Override
	public void onEntityCollision(Entity entity)
	{
    	if(entity instanceof EntityLivingBase)
    	{
    		EntityLivingBase e = (EntityLivingBase)entity;
    		e.addPotionEffect(new PotionEffect(Potion.weakness.id, 100));
    		e.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 100));
    		if(!((EntityLivingBase)entity).isPotionActive(Potion.confusion.id))
    			e.addPotionEffect(new PotionEffect(Potion.confusion.id, 100));
    	}
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition hit, ItemStack item)
	{
		System.out.println(canStay());
		return false;
	}

	@Override
	public String getType() {
		return "crystal";
	}

	@Override
	public int sideForMeta(int meta) {
		return metaSideMap[meta&7];
	}
    
    public static McBlockPart placement(World world, BlockCoord pos, int side)
    {
    	if(side>5)
    		return null;
        pos = pos.copy().offset(side^1);
        if(!world.isBlockSolidOnSide(pos.x, pos.y, pos.z, ForgeDirection.getOrientation(side)))
            return null;
        int id = world.getBlockId(pos.x, pos.y, pos.z);
        int m = world.getBlockMetadata(pos.x, pos.y, pos.z);
        if(Block.blocksList[id]instanceof BlockFluid&&m!=15)
        	return null;
        
        
        int meta = sideMetaMap[side^1];
        return new CrystalPart(meta);
    }
    
    public int getMeta()
    {
    	return meta;
    }
    
    @Override
    public boolean canStay()
    {
    	super.canStay();
        BlockCoord pos = new BlockCoord(tile()).offset(sideForMeta(meta));
        return world().isBlockSolidOnSide(pos.x, pos.y, pos.z, ForgeDirection.getOrientation(sideForMeta(meta)));
    }
    
    @Override
    public boolean doesTick()
    {
        return true;
    }
    
    @Override 
    public void update()
    {
    	super.update();
    	if(!WorldCore.proxy.isOnClientSide())
    		dropIfCantStay();
    }
    
    public void drop()
    {
      //  TileMultipart.dropItem(new ItemStack(getBlock()), world(), Vector3.fromTileEntityCenter(tile()));
    //	System.out.println("@drop");
        tile().remPart(this);
    }
    
    public void onAdded()
    {
    	//super.onAdded();
    	//dropIfCantStay();
    }
    
    public boolean solid(int side)
    {
    	return false;
    }
    @Override
    public boolean dropIfCantStay()
    {
        if(!canStay())
        {
        	drop();
            return true;
        }
        return false;
    }
}
