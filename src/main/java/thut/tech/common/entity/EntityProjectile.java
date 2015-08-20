package thut.tech.common.entity;

import java.util.Iterator;

import thut.api.maths.ExplosionCustom;
import thut.api.maths.ExplosionCustom.ExplosionStuff;
import thut.api.maths.Vector3;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityProjectile extends EntityFallingBlock {

	Block block;
	boolean accelerated = false;
	
	public EntityProjectile(World p_i1706_1_) {
		super(p_i1706_1_);
		// TODO Auto-generated constructor stub
	}

	public EntityProjectile(World p_i45318_1_, double p_i45318_2_,
			double p_i45318_4_, double p_i45318_6_, Block b) {
		super(p_i45318_1_, p_i45318_2_, p_i45318_4_, p_i45318_6_, b);
		// TODO Auto-generated constructor stub
		this.block = b;
	}

	public EntityProjectile(World p_i45319_1_, double p_i45319_2_,
			double p_i45319_4_, double p_i45319_6_, Block b,
			int p_i45319_9_) {
		super(p_i45319_1_, p_i45319_2_, p_i45319_4_, p_i45319_6_, b,
				p_i45319_9_);
		// TODO Auto-generated constructor stub
	}


    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        if (block==null||block.getMaterial() == Material.air)
        {
            this.setDead();
        }
        else
        {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            ++this.field_145812_b;
            
            Vector3 here = Vector3.getNewVectorFromPool().set(this);
            Vector3 velocity = Vector3.getNewVectorFromPool().setToVelocity(this);
            double d = velocity.mag()+1;
            
            Block down = here.getBlock(worldObj, EnumFacing.DOWN);
            
            Vector3 hit = here.findNextSolidBlock(worldObj, velocity, d);
            d -= 1;
            
            if(isOnRails(here) && !accelerated)
            {
            	hit = null;
            	Vector3 dir = getAccelerationFromRails(here);
            	accelerated = !dir.isEmpty();
            	dir.addVelocities(this);
            	dir.freeVectorFromPool();
            } 
            else if((d<0.04 && field_145812_b>2 && down.getMaterial().isSolid()) && hit==null)
            	hit = here.copy();
            
            if(hit!=null)
            {
            	double dist = here.distanceTo(hit);
            	velocity.scalarMultBy(dist);
            	velocity.setVelocities(this);
                this.moveEntity(this.motionX, this.motionY, this.motionZ);
                ExplosionCustom boom = new ExplosionCustom(worldObj, this, hit, 100);
                float h =  block.getBlockHardness(worldObj, 0, 0, 0);
                double oldD = d;
                d /= 100;
                d = Math.max(d, oldD/2);
                d *= d;
                d *= h;
                d = Math.min(500,d);
                System.out.println(d+" "+oldD+" "+h);
                ExplosionCustom.explosions.add(new ExplosionStuff(boom, 100, d, worldObj, hit));
                
                this.setDead();
            	motionX = motionY = motionZ = 0;
            	hit.freeVectorFromPool();
            	return;
            }
            this.motionY -= 0.03999999910593033D;
            
            here.freeVectorFromPool();
            velocity.freeVectorFromPool();
            
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9800000190734863D;
            this.motionY *= 0.9800000190734863D;
            this.motionZ *= 0.9800000190734863D;

            if (!this.worldObj.isRemote)
            {
                int i = MathHelper.floor_double(this.posX);
                int j = MathHelper.floor_double(this.posY);
                int k = MathHelper.floor_double(this.posZ);
            }
        }
    }
    
    boolean isOnRails(Vector3 here)
    {
    	return here.getBlock(worldObj)==Blocks.golden_rail;
    }
    
    public Vector3 getAccelerationFromRails(Vector3 here)
    {
    	Vector3 ret = Vector3.getNewVectorFromPool();
    	int meta = here.getBlockMetadata(worldObj);
    	
    	EnumFacing dir = null;
    	
    	for(EnumFacing side: EnumFacing.values())
    	{
    		if(side.getFrontOffsetY()==0)
    		{
    			Block b = here.getBlock(worldObj, side);
    			if(b == Blocks.golden_rail)
    			{
    				dir = side;
    				break;
    			}
    		}
    	}
    	if(dir!=null)
    		ret.set(dir);
    	else
    		return ret;
    	int n = 1;
    	boolean end = false;
    	Vector3 temp = Vector3.getNewVectorFromPool();
    	Vector3 temp1 = Vector3.getNewVectorFromPool();
    	while(!end)
    	{
    		temp1.set(ret).scalarMultBy(n++);
    		temp.set(temp1.addTo(here));
    		end = temp.getBlock(worldObj) != Blocks.golden_rail;
    	}
    	ret.scalarMultBy(n);
    	temp.freeVectorFromPool();
    	temp1.freeVectorFromPool();
    	
    	return ret;
    }
}
