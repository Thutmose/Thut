package thut.api.boom;

import java.util.List;

import com.google.common.collect.Lists;

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

    protected BlastResult getBlocksToRemove()
    {
        int ind = boom.currentIndex;
        boom.centre.set(boom.centre.getPos()).addTo(0.5, 0.5, 0.5);
        int coordIndex;
        double scaleFactor = 1500;
        double rMag;
        double str;
        int num = (int) (Math.sqrt(boom.strength * scaleFactor / 0.5));
        int max = ExplosionCustom.MAX_RADIUS * 2 + 1;
        num = Math.min(num, max);
        num = Math.min(num, 1000);
        int numCubed = num * num * num;
        double radSq = num * num / 4;
        int currentRadius = (int) Math.floor(Cruncher.cubeRoot(boom.currentIndex));
        currentRadius = (currentRadius - 1) / 2 + 1;
        int maxIndex = Math.min(numCubed, boom.nextIndex);
        int increment = 0;
        List<BlockPos> ret = Lists.newArrayList();
        List<HitEntity> entityAffected = Lists.newArrayList();
        boolean done = false;
        long start = System.currentTimeMillis();
        int thisIndex;
        int n = 0;
        int nh = 0;
        int noH = 0;
        int pre = 0;
        for (boom.currentIndex = ind; boom.currentIndex < maxIndex; boom.currentIndex++)
        {
            n++;
            increment++;
            long time = System.currentTimeMillis();
            if (time - start > 1000 && false) break;
            Cruncher.indexToVals(boom.currentIndex, boom.r);
            thisIndex = Cruncher.getVectorInt(boom.r);
            int cr = (int) Math.floor(Cruncher.cubeRoot(boom.currentIndex));
            cr = (cr - 1) / 2 + 1;
            if (currentRadius != cr)
            {
                currentRadius = cr;
                System.out.println((boom.thisShell.size() - pre) + " layer:" + n + " found:" + nh + " notFound:" + noH);
                n = 0;
                nh = 0;
                noH = 0;
                pre = boom.thisShell.size();
            }

            if (boom.r.y + boom.centre.y < 0 || boom.r.y + boom.centre.y > 255) continue;
            double rSq = boom.r.magSq();
            if (rSq > radSq || boom.thisShell.containsKey(thisIndex)) continue;
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
            boom.thisShell.put(thisIndex, new Float(resHere));
            if (resHere > str)
            {
                System.out.println("Blocked with " + resHere + " " + (((int) (str * 100))) / 100f + " " + thisIndex);
                continue;
            }
            boolean stop = false;
            rMag = boom.r.mag();
            float dj = 0.01f;
            float res = 0;
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
                    res = old + boom.thisShell.get(thisIndex);
                    boom.thisShell.put(thisIndex, new Float(res));
                    if (has)
                    {
                        nh++;
                    }
                    else
                    {
                        noH++;
                    }
                    if (!boom.canBreak(boom.rTestAbs))
                    {
                        stop = true;
                        break;
                    }
                    if (res > str)
                    {
                        stop = true;
                        break;
                    }
                    break;
                }
                boom.rTestPrev.set(boom.rTest);
            }
            if (stop)
            {
                boom.thisShell.put(thisIndex, new Float(Integer.MAX_VALUE));
                continue;
            }
            boom.rAbs.set(boom.r).addTo(boom.centre);
            Chunk chunk = boom.world.getChunkFromBlockCoords(boom.rAbs.getPos());
            if (chunk == null)
            {
                System.out.println("No chunk at " + boom.rAbs);
                Thread.dumpStack();
            }
            if (!boom.affected.contains(chunk)) boom.affected.add(chunk);
            boom.addChunkPosition(boom.rAbs);
            List<Entity> hits = boom.world.getEntitiesWithinAABBExcludingEntity(boom.exploder,
                    boom.rAbs.getAABB().expand(0.5, 0.5, 0.5));
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
