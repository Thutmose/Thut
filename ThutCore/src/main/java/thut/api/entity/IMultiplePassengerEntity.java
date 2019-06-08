package thut.api.entity;

import java.util.List;
import java.util.UUID;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.math.MathHelper;

public interface IMultiplePassengerEntity
{
    public static class Seat
    {
        public static final UUID BLANK = new UUID(0, 0);
        public Vector3f          seat;
        public UUID              entityId;

        public Seat(Vector3f vector3f, UUID readInt)
        {
            seat = vector3f;
            entityId = readInt != null ? readInt : BLANK;
        }

        public Seat(PacketBuffer buf)
        {
            seat = new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
            entityId = new UUID(buf.readLong(), buf.readLong());
        }

        public void writeToBuf(PacketBuffer buf)
        {
            buf.writeFloat(seat.x);
            buf.writeFloat(seat.y);
            buf.writeFloat(seat.z);
            buf.writeLong(entityId.getMostSignificantBits());
            buf.writeLong(entityId.getLeastSignificantBits());
        }

        public void writeToNBT(CompoundNBT tag)
        {
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(8));
            writeToBuf(buffer);
            tag.putByteArray("v", buffer.array());
        }

        public static Seat readFromNBT(CompoundNBT tag)
        {
            byte[] arr = tag.getByteArray("v");
            PacketBuffer buf = new PacketBuffer(Unpooled.copiedBuffer(arr));
            return new Seat(buf);
        }
    }

    public static final IDataSerializer<Seat> SEATSERIALIZER = new IDataSerializer<Seat>()
    {
        @Override
        public void write(PacketBuffer buf, Seat value)
        {
            value.writeToBuf(buf);
        }

        @Override
        public Seat read(PacketBuffer buf)
        {
            return new Seat(buf);
        }

        @Override
        public DataParameter<Seat> createKey(int id)
        {
            return new DataParameter<>(id, this);
        }

        @Override
        public Seat copyValue(Seat value)
        {
            return new Seat((Vector3f) value.seat.clone(), value.entityId);
        }
    };

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
