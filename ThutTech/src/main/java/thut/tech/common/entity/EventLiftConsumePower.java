package thut.tech.common.entity;

public class EventLiftConsumePower extends Event
{
    public final EntityLift lift;
    public final long toConsume;

    public EventLiftConsumePower(EntityLift lift, long toConsume)
    {
        this.lift = lift;
        this.toConsume = toConsume;
    }
}
