package thut.api.entity;

import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public interface IMultiplePassengerEntity
{
    public static class MultiplePassengerManager
    {
        public static void managePassenger(Entity passenger, IMultiplePassengerEntity multipassenger)
        {
            Entity entity = (Entity) multipassenger;
            if (!entity.isPassenger(passenger)) return;
            Vector3f v = multipassenger.getSeat(passenger);
            float yaw = -multipassenger.getYaw() * 0.017453292F;
            float pitch = -multipassenger.getPitch() * 0.017453292F;
            float sinYaw = MathHelper.sin(yaw);
            float cosYaw = MathHelper.cos(yaw);
            float sinPitch = MathHelper.sin(pitch);
            float cosPitch = MathHelper.cos(pitch);
            Matrix3f matrixYaw = new Matrix3f(cosYaw, 0, sinYaw, 0, 1, 0, -sinYaw, 0, cosYaw);
            Matrix3f matrixPitch = new Matrix3f(cosPitch, -sinPitch, 0, sinPitch, cosPitch, 0, 0, 0, 1);
            Matrix3f transform = new Matrix3f();
            transform.mul(matrixYaw, matrixPitch);
            if (v == null) v = new Vector3f();
            else
            {
                v = (Vector3f) v.clone();
                transform.transform(v);
            }
            passenger.setPosition(entity.posX + v.x, entity.posY + passenger.getYOffset() + v.y, entity.posZ + v.z);
        }
    }

    /** Gets the seated location of this passenger, used for properly
     * translating onto the seat.
     * 
     * @param passenger
     * @return */
    Vector3f getSeat(Entity passenger);

    /** Gets the passenger for a seat, if this returns null, it may attempt to
     * add someone to this seat. The seat given here will always be from the
     * contents of the return of getSeats()
     * 
     * @param seat
     * @return */
    Entity getPassenger(Vector3f seat);

    /** List of seats on this entity;
     * 
     * @return */
    List<Vector3f> getSeats();

    /** Current rotation yaw, for offsetting of the ridden entitites.
     * 
     * @return */
    float getYaw();

    /** Current pitch rotation for offsetting the ridden entitites
     * 
     * @return */
    float getPitch();

    /** for rendering interpolation.
     * 
     * @return */
    float getPrevYaw();

    /** for rendering interpolation.
     * 
     * @return */
    float getPrevPitch();
}
