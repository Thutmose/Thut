package dorfgen.finite;

import javax.vecmath.Vector3f;

import dorfgen.WorldGenerator;
import dorfgen.finite.Transporter.Vector3;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FiniteHandler
{
    public FiniteHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void Wrap(EnteringChunk evt)
    {
        Vector3 pos = new Vector3(evt.getEntity().getPosition());
        pos.x = (float) evt.getEntity().posX;
        pos.y = (float) evt.getEntity().posY;
        pos.z = (float) evt.getEntity().posZ;
        int[] shift = new int[2];
        shift[0] = WorldGenerator.shift.getX();
        shift[1] = WorldGenerator.shift.getY();
        if (!isInImage(WorldGenerator.instance.dorfs.biomeMap, shift, WorldGenerator.scale, pos))
        {
            Biome[][] image = WorldGenerator.instance.dorfs.biomeMap;
            int scale = WorldGenerator.scale;
            int dx = 0, dy = 0, xMin = 0, xMax = 0, yMin = 0, yMax = 0;
            dx = shift[0];
            dy = shift[1];
            xMin = -dx;
            yMin = -dy;
            xMax = image.length * scale - dx;
            yMax = image[0].length * scale - dy;
            Vector3f posOld = new Vector3f();
            posOld.set(pos);

            if (pos.x > xMax)
            {
                pos.x = xMin + (pos.x - xMax) + 1;
            }
            else if (pos.x < xMin)
            {
                pos.x = xMax - (xMin - pos.x) - 1;
            }
            if (pos.z > yMax)
            {
                pos.z = yMin + (pos.z - yMax) + 1;
            }
            else if (pos.z < yMin)
            {
                pos.z = yMax - (yMin - pos.z) - 1;
            }
            Transporter.teleportEntity(evt.getEntity(), pos, evt.getEntity().dimension);
        }
    }

    public boolean isInImage(Object[][] image, int[] shift, int scale, Vector3f position)
    {
        int dx = 0, dy = 0, xMin = 0, xMax = 0, yMin = 0, yMax = 0;
        if (shift != null)
        {
            dx = shift[0];
            dy = shift[1];
        }
        if (image != null)
        {
            xMin = -dx;
            yMin = -dy;
            xMax = image.length * scale - dx;
            yMax = image[0].length * scale - dy;
            if (position.x < xMin || position.x > xMax) return false;
            if (position.z < yMin || position.z > yMax) return false;
            return true;
        }
        else
        {
            return true;
        }

    }
}
