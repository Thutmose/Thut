package thut.api.boom;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import thut.api.boom.ExplosionCustom.BlastResult;
import thut.api.boom.ExplosionCustom.HitEntity;
import thut.api.maths.Cruncher;

public class Checker
{
    final ExplosionCustom boom;

    public Checker(ExplosionCustom boom)
    {
        this.boom = boom;
    }

    private BlastResult getBlocksToRemove2()
    {
        int ind = boom.currentIndex;
        int index;
        int index2;
        double scaleFactor = 1500;
        double rMag;
        float resist;
        double str;
        int num = (int) (Math.sqrt(boom.strength * scaleFactor / 0.5));
        int max = boom.radius * 2 + 1;
        num = Math.min(num, max);
        num = Math.min(num, 1000);
        int numCubed = num * num * num;
        double radSq = num * num / 4;
        int maxIndex = numCubed;
        int increment = 0;
        List<BlockPos> ret = Lists.newArrayList();
        List<HitEntity> entityAffected = Lists.newArrayList();
        boolean done = true;
        long start = System.currentTimeMillis();
        for (boom.currentIndex = ind; boom.currentIndex < maxIndex; boom.currentIndex++)
        {
            increment++;
            long time = System.currentTimeMillis();
            //Break out early if we have taken too long.
            if (time - start > 10)
            {
                done = false;
                break;
            }
            Cruncher.indexToVals(boom.currentIndex, boom.r);
            //TODO make this check world bounds somehow.
            if (boom.r.y + boom.centre.y < 0 || boom.r.y + boom.centre.y > 255) continue;
            double rSq = boom.r.magSq();
            if (rSq > radSq) continue;
            rMag = Math.sqrt(rSq);
            boom.rAbs.set(boom.r).addTo(boom.centre);
            boom.rHat.set(boom.r).norm();
            index = Cruncher.getVectorInt(boom.rHat.scalarMultBy(num / 2d));
            boom.rHat.scalarMultBy(2d / num);
            //Already checked here, so we exit.
            if (boom.blockedSet.contains(index))
            {
                continue;
            }
            str = boom.strength * scaleFactor / rSq;
            //Check for mobs to hit at this point.
            if (boom.rAbs.isAir(boom.world) && !(boom.r.isEmpty()))
            {
                if (ExplosionCustom.AFFECTINAIR)
                {
                    List<Entity> hits = boom.world.getEntitiesWithinAABBExcludingEntity(boom.exploder,
                            boom.rAbs.getAABB().grow(0.5, 0.5, 0.5));
                    if (hits != null) for (Entity e : hits)
                    {
                        entityAffected.add(new HitEntity(e, (float) str));
                    }
                }
                continue;
            }

            //Return due to out of blast power.
            if (str <= boom.minBlastDamage)
            {
                System.out.println("Terminating at distance " + rMag);
                done = true;
                break;
            }
            //Continue to next site, we can't break this block.
            if (!boom.canBreak(boom.rAbs))
            {
                boom.blockedSet.add(index);
                continue;
            }
            float res;
            res = boom.rAbs.getExplosionResistance(boom, boom.world);
            if (res > 1) res = res * res;
            index2 = Cruncher.getVectorInt(boom.r);
            boom.resists.put(index2, res);
            boom.checked.set(index2);
            //This block is too strong, so continue to next block.
            if (res > str)
            {
                boom.blockedSet.add(index);
                continue;
            }
            //Whether we should continue onto the next site.
            boolean stop = false;
            
            rMag = boom.r.mag();
            float dj = 1;
            resist = 0;

            //Check each block to see if we have enough power to break.
            for (float j = 0F; j <= rMag; j += dj)
            {
                //TODO start this just inside the last shell?
                boom.rTest.set(boom.rHat).scalarMultBy(j);

                if (!(boom.rTest.sameBlock(boom.rTestPrev)))
                {
                    boom.rTestAbs.set(boom.rTest).addTo(boom.centre);

                    index2 = Cruncher.getVectorInt(boom.rTest);

                    if (boom.checked.get(index2))
                    {
                        res = boom.resists.get(index2);
                    }
                    else
                    {
                        res = boom.rTestAbs.getExplosionResistance(boom, boom.world);
                        if (res > 1) res = res * res;
                        boom.resists.put(index2, res);
                        boom.checked.set(index2);
                    }
                    resist += res;
                    //Can't break this, so set as blocked and flag for next site.
                    if (!boom.canBreak(boom.rTestAbs))
                    {
                        stop = true;
                        boom.blockedSet.add(index);
                        break;
                    }
                    double d1 = boom.rTest.magSq();
                    double d = d1;
                    str = boom.strength * scaleFactor / d;
                    //too hard, so set as blocked and flag for next site.
                    if (resist > str)
                    {
                        stop = true;
                        boom.blockedSet.add(index);
                        break;
                    }
                }
                boom.rTestPrev.set(boom.rTest);
            }
            //Continue onto next site.
            if (stop) continue;
            boom.rAbs.set(boom.r).addTo(boom.centre);
            Chunk chunk = boom.world.getChunk(boom.rAbs.getPos());
            if (chunk == null)
            {
                System.out.println("No chunk at " + boom.rAbs);
                Thread.dumpStack();
            }
            //Add to affected chunks list.
            if (!boom.affected.contains(chunk)) boom.affected.add(chunk);
            //Add as affected location.
            boom.addChunkPosition(boom.rAbs);
            //Check for additional mobs to hit.
            List<Entity> hits = boom.world.getEntitiesWithinAABBExcludingEntity(boom.exploder,
                    boom.rAbs.getAABB().grow(0.5, 0.5, 0.5));
            if (hits != null) for (Entity e : hits)
            {
                entityAffected.add(new HitEntity(e, (float) str));
            }
            //Add to blocks to remove list.
            ret.add(new BlockPos(boom.rAbs.getPos()));
        }
        //Increment the boom index for next pass.
        boom.nextIndex = boom.currentIndex + increment;
        return new BlastResult(ret, entityAffected, done);
    }

    protected BlastResult getBlocksToRemove()
    {
        boolean oldBoom = true;
        if (oldBoom) return getBlocksToRemove2();

        int ind = boom.currentIndex;
        boom.centre.set(boom.centre.getPos()).addTo(0.5, 0.5, 0.5);
        int coordIndex;
        double scaleFactor = 1500;
        double rMag;
        double str;
        int num = (int) (Math.sqrt(boom.strength * scaleFactor / 0.5));
        int max = boom.radius * 2 + 1;
        num = Math.min(num, max);
        num = Math.min(num, 1000);
        int numCubed = num * num * num;
        double radSq = num * num / 4;
        int currentRadius = (int) Math.floor(Cruncher.cubeRoot(boom.currentIndex));
        currentRadius = (currentRadius - 1) / 2 + 1;
        int maxIndex = numCubed;// Math.min(numCubed, boom.nextIndex);
        int increment = 0;
        List<BlockPos> ret = Lists.newArrayList();
        List<HitEntity> entityAffected = Lists.newArrayList();
        boolean done = maxIndex >= numCubed;
        long start = System.currentTimeMillis();
        int thisIndex;
        int n = 0;
        int nh = 0;
        int noH = 0;
        int pre = 0;

        Set<Integer> test = Sets.newHashSet();
        for (int i = 0; i < 5 * 5 * 5; i++)
        {
            Cruncher.indexToVals(i, boom.r);
            thisIndex = Cruncher.getVectorInt(boom.r);
            test.add(thisIndex);
        }
        // System.out.println(test.size() + " " + (5 * 5 * 5));

        for (boom.currentIndex = ind; boom.currentIndex < maxIndex; boom.currentIndex++)
        {
            n++;
            increment++;
            long time = System.currentTimeMillis();
            if (time - start > 50)
            {
                done = false;
                break;
            }
            Cruncher.indexToVals(boom.currentIndex, boom.r);
            thisIndex = Cruncher.getVectorInt(boom.r);
            int cr = (int) Math.floor(Cruncher.cubeRoot(boom.currentIndex));
            cr = (cr - 1) / 2 + 1;
            if (currentRadius != cr)
            {
                currentRadius = cr;
                System.out.println((boom.thisShell.size() - pre) + " layer:" + n + " found:" + nh + " notFound:" + noH
                        + " total:" + (nh + noH));
                n = 0;
                nh = 0;
                noH = 0;
                pre = boom.thisShell.size();
            }

            if (boom.r.y + boom.centre.y < 0 || boom.r.y + boom.centre.y > 255)
            {
                boom.thisShell.put(thisIndex, new Float(Integer.MAX_VALUE));
                continue;
            }
            double rSq = boom.r.magSq();
            if (rSq > radSq || boom.thisShell.containsKey(thisIndex))
            {
                if (!(rSq > radSq)) System.out.println("invalid " + boom.r);
                boom.thisShell.put(thisIndex, new Float(Integer.MAX_VALUE));
                continue;
            }
            rMag = Math.sqrt(rSq);

            boom.rAbs.set(boom.r).addTo(boom.centre);
            boom.rHat.set(boom.r).norm();
            str = boom.strength * scaleFactor / rSq;
            if (str <= boom.minBlastDamage)
            {
                System.out.println("Terminating at distance " + rMag);
                done = true;
                break;
            }
            float resHere = !boom.canBreak(boom.rAbs) ? Integer.MAX_VALUE
                    : boom.rAbs.getExplosionResistance(boom, boom.world);
            if (resHere > 1) resHere = resHere * resHere;
            if (resHere > str)
            {
                boom.thisShell.put(thisIndex, new Float(Integer.MAX_VALUE));
                continue;
            }
            boolean stop = false;
            rMag = boom.r.mag();
            float dj = 0.5f;
            float res = resHere;
            boom.rTestPrev.set(boom.r);

            for (float j = (float) rMag; j > 0; j -= dj)
            {
                boom.rTest.set(boom.rHat).scalarMultBy(j);
                if (!(boom.rTest.sameBlock(boom.rTestPrev)))
                {
                    boom.rTestAbs.set(boom.rTest).addTo(boom.centre);
                    coordIndex = Cruncher.getVectorInt(boom.rTest);
                    boolean has = boom.thisShell.containsKey(coordIndex);
                    float old = has ? boom.thisShell.get(coordIndex)
                            : boom.rTestAbs.getExplosionResistance(boom, boom.world);
                    res += old;
                    if (has)
                    {
                        nh++;
                    }
                    else
                    {
                        noH++;
                    }
                    if (!boom.canBreak(boom.rTestAbs) || res > str)
                    {
                        stop = true;
                        break;
                    }
                }
                boom.rTestPrev.set(boom.rTest);
            }
            if (stop)
            {
                boom.thisShell.put(thisIndex, new Float(Integer.MAX_VALUE));
                continue;
            }

            boom.thisShell.put(thisIndex, new Float(resHere));
            // boom.thisShell.put(thisIndex, new Float(res));
            boom.rAbs.set(boom.r).addTo(boom.centre);
            Chunk chunk = boom.world.getChunk(boom.rAbs.getPos());
            if (chunk == null)
            {
                System.out.println("No chunk at " + boom.rAbs);
                Thread.dumpStack();
            }
            if (!boom.affected.contains(chunk)) boom.affected.add(chunk);
            boom.addChunkPosition(boom.rAbs);
            List<Entity> hits = boom.world.getEntitiesWithinAABBExcludingEntity(boom.exploder,
                    boom.rAbs.getAABB().grow(0.5, 0.5, 0.5));
            if (hits != null) for (Entity e : hits)
            {
                entityAffected.add(new HitEntity(e, (float) str));
            }
            ret.add(new BlockPos(boom.rAbs.getPos()));
        }
        boom.nextIndex = boom.currentIndex + increment;
        return new BlastResult(ret, entityAffected, done);
    }
}
