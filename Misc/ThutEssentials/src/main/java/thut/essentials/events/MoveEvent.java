package thut.essentials.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerEvent;

/** Fired before the player is moved. */
public class MoveEvent extends PlayerEvent
{
    public MoveEvent(EntityPlayer player)
    {
        super(player);
    }

    public int[] getPos()
    {
        BlockPos pos = getEntityPlayer().getPosition();
        return new int[] { pos.getX(), pos.getY(), pos.getZ(), getEntityPlayer().dimension };
    }
}
