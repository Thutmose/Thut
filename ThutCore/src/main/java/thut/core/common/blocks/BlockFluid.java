package thut.core.common.blocks;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import thut.api.block.IHardenableFluid;
import thut.api.block.IViscousFluid;

public abstract class BlockFluid extends BlockFluidFinite implements IHardenableFluid, IViscousFluid
{

    public BlockFluid(Fluid fluid, Material material)
    {
        super(fluid, material);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
        int flowDiff = getFlowDifferential(world, pos, state, rand);

        boolean changed = false;
        boolean cont = true;
        int quantaRemaining = (state.getValue(LEVEL)) + 1 - flowDiff;
        quantaRemaining = Math.max(1, quantaRemaining);

        // Flow vertically if possible
        int prevRemaining = quantaRemaining;
        quantaRemaining = tryToFlowVerticallyInto(world, pos, quantaRemaining);

        if (quantaRemaining < 1)
        {
            cont = false;
        }
        else if (quantaRemaining != prevRemaining)
        {
            changed = true;
            if (quantaRemaining == 1)
            {
                world.setBlockState(pos, state.withProperty(LEVEL, quantaRemaining - 1), 2);
                cont = false;
            }
        }
        else if (quantaRemaining == 1)
        {
            cont = false;
        }

        if (cont)
        {

            // Flow out if possible
            int lowerthan = quantaRemaining - 1;
            int total = quantaRemaining;
            int count = 1;

            for (Direction side : Direction.Plane.HORIZONTAL)
            {
                BlockPos off = pos.offset(side);
                if (displaceIfPossible(world, off)) world.setBlockToAir(off);

                int quanta = getQuantaValueBelow(world, off, lowerthan);
                if (quanta >= 0)
                {
                    count++;
                    total += quanta;
                }
            }

            if (count == 1)
            {
                if (changed)
                {
                    world.setBlockState(pos, state.withProperty(LEVEL, quantaRemaining - 1), 2);
                }
                cont = false;
            }

            int each = total / count;
            int rem = total % count;

            if (cont)
            {
                for (Direction side : Direction.Plane.HORIZONTAL)
                {
                    BlockPos off = pos.offset(side);
                    int quanta = getQuantaValueBelow(world, off, lowerthan);
                    if (quanta >= 0)
                    {
                        int newquanta = each;
                        if (rem == count || rem > 1 && rand.nextInt(count - rem) != 0)
                        {
                            ++newquanta;
                            --rem;
                        }

                        if (newquanta != quanta)
                        {
                            if (newquanta == 0)
                            {
                                world.setBlockToAir(off);
                            }
                            else
                            {
                                world.setBlockState(off, getDefaultState().withProperty(LEVEL, newquanta - 1), 2);
                            }
                            world.scheduleUpdate(off, this, tickRate);
                        }
                        --count;
                    }
                }
                if (rem > 0)
                {
                    ++each;
                }
                world.setBlockState(pos, state.withProperty(LEVEL, each - 1), 2);
                changed = true;
            }
        }

        if (!changed && rand.nextDouble() > 0 && getSolidState(world, pos) != null)
        {
            tryHarden(world, pos);
        }
    }
}
