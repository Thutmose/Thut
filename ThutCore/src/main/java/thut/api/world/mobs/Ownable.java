package thut.api.world.mobs;

import java.util.UUID;

import javax.annotation.Nullable;

public interface Ownable
{
    void setOwnerId();

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
