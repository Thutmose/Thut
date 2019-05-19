package thut.api.world.mobs;

import java.util.UUID;

import javax.annotation.Nullable;

public interface Ownable
{
    /** Sets the owner for this mob.
     * 
     * @param owner */
    void setOwnerId(UUID owner);

    /** The UUID of our owner, null means no owner.
     * 
     * @return */
    @Nullable
    UUID getOwnerId();

    /** Check getOwnerId first, if that is null, then check this. This can
     * return null even if getOwnerId does not, as that case means the owner is
     * not loaded.
     * 
     * @return */
    @Nullable
    Mob getOwner();
}
