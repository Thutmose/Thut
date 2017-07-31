package thut.tech.common.entity;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import thut.api.boom.ExplosionCustom;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;

public class EntityProjectile extends EntityFallingBlock
{

    IBlockState block;
    boolean     accelerated = false;

    public EntityProjectile(World worldIn)
    {
        super(worldIn);
        // TODO Auto-generated constructor stub
    }

    public EntityProjectile(World worldIn, double x, double y, double z, IBlockState fallingBlockState)
    {
        super(worldIn, x, y, z, fallingBlockState);
        this.block = fallingBlockState;
    }

    public Vector3 getAccelerationFromRails(Vector3 here)
    {
        Vector3 ret = Vector3.getNewVector();
        EnumFacing dir = null;

        for (EnumFacing side : EnumFacing.values())
        {
            if (side.getFrontOffsetY() == 0)
            {
                Block b = here.getBlock(getEntityWorld(), side);
                if (b == Blocks.GOLDEN_RAIL)
                {
                    dir = side;
                    break;
                }
            }
        }
        if (dir != null) ret.set(dir);
        else return ret;
        int n = 1;
        boolean end = false;
        Vector3 temp = Vector3.getNewVector();
        Vector3 temp1 = Vector3.getNewVector();
        while (!end)
        {
            temp1.set(ret).scalarMultBy(n++);
            temp.set(temp1.addTo(here));
            end = temp.getBlock(getEntityWorld()) != Blocks.GOLDEN_RAIL;
        }
        ret.scalarMultBy(n);

        return ret;
    }

    boolean isOnRails(Vector3 here)
    {
        return here.getBlock(getEntityWorld()) == Blocks.GOLDEN_RAIL;
    }

    /** Called to update the entity's position/logic. */
    @Override
    public void onUpdate()
    {
        if (block == null || block.getMaterial() == Material.AIR)
        {
            this.setDead();
        }
        else
        {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            ++this.fallTime;

            Vector3 here = Vector3.getNewVector().set(this);
            Vector3 velocity = Vector3.getNewVector().setToVelocity(this);
            double d = velocity.mag() + 1;

            IBlockState downState = here.offset(EnumFacing.DOWN).getBlockState(getEntityWorld());

            Vector3 hit = here.findNextSolidBlock(getEntityWorld(), velocity, d);
            d -= 1;

            if (isOnRails(here) && !accelerated)
            {
                hit = null;
                Vector3 dir = getAccelerationFromRails(here);
                accelerated = !dir.isEmpty();
                dir.addVelocities(this);
            }
            else if ((d < 0.04 && fallTime > 2 && downState.getMaterial().isSolid()) && hit == null) hit = here.copy();

            if (hit != null)
            {
                double dist = here.distanceTo(hit);
                velocity.scalarMultBy(dist);
                velocity.setVelocities(this);
                this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                ExplosionCustom boom = new ExplosionCustom(getEntityWorld(), this, hit, 100);
                float h = block.getBlockHardness(getEntityWorld(), hit.getPos());
                double oldD = d;
                d /= 100;
                d = Math.max(d, oldD / 2);
                d *= d;
                d *= h;
                d = Math.min(100, d);
                boom.doExplosion();

                this.setDead();
                motionX = motionY = motionZ = 0;
                return;
            }
            this.motionY -= 0.03999999910593033D;

            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9800000190734863D;
            this.motionY *= 0.9800000190734863D;
            this.motionZ *= 0.9800000190734863D;
        }
    }

    @Override
    public void move(MoverType type, double x, double y, double z)
    {
        List<AxisAlignedBB> aabbs = Lists.newArrayList();
        Matrix3 mainBox = new Matrix3();
        Vector3 offset = Vector3.getNewVector();
        mainBox.boxMin().clear();
        mainBox.boxMax().x = width;
        mainBox.boxMax().z = height;
        mainBox.boxMax().y = width;
        offset.set(-mainBox.boxMax().x / 2, 0, -mainBox.boxMax().z / 2);
        Vector3 vec = Vector3.getNewVector().set(this);
        mainBox.addOffsetTo(offset).addOffsetTo(vec);
        AxisAlignedBB box = mainBox.getBoundingBox();
        AxisAlignedBB box1 = box.expand(2 + width, 2 + height, 2 + width);
        box1 = box1.grow(motionX, motionY, motionZ);
        aabbs = mainBox.getCollidingBoxes(box1, getEntityWorld(), getEntityWorld());
        Matrix3.expandAABBs(aabbs, box);
        Matrix3.mergeAABBs(aabbs, 0.01, 0.01, 0.01);
        Vector3 diffs = Vector3.getNewVector().set(x, y, z);
        mainBox.set(getEntityBoundingBox());
        diffs.set(mainBox.doTileCollision(world, aabbs, this, Vector3.empty, diffs, false));
        boolean lock = false;
        if (diffs.x != x || diffs.y != y || diffs.z != z)
        {
            lock = true;
        }
        x = diffs.x;
        y = diffs.y;
        z = diffs.z;
        super.move(type, x, y, z);
        if (lock)
        {
            this.world.setBlockState(getPosition(), block);
            this.setDead();
        }
    }
}
