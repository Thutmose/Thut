package thut.essentials.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class ClaimLandEvent extends Event
{
    public final BlockPos     land;
    public final String       team;
    public final EntityPlayer claimer;
    public final int          dimension;

    public ClaimLandEvent(BlockPos land, int dimension, EntityPlayer claimer, String team)
    {
        this.land = land;
        this.dimension = dimension;
        this.team = team;
        this.claimer = claimer;
    }
}
