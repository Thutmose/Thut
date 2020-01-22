package thut.tech.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import thut.core.common.network.Packet;
import thut.tech.common.blocks.lift.ControllerTile;

public class PacketLift extends Packet
{
    private BlockPos pos;
    private int      button;
    private boolean  call;

    public PacketLift()
    {
    }

    public PacketLift(final PacketBuffer buffer)
    {
        this.pos = buffer.readBlockPos();
        this.button = buffer.readInt();
        this.call = buffer.readBoolean();
    }

    /*
     * Handles Server side interaction.
     */
    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        final TileEntity tile = player.getEntityWorld().getTileEntity(this.pos);
        if (tile instanceof ControllerTile)
        {
            final ControllerTile te = (ControllerTile) tile;
            if (te.lift == null) return;
            te.buttonPress(this.button, this.call);
            te.calledFloor = te.lift.getDestinationFloor();
        }
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
        buffer.writeBlockPos(this.pos);
        buffer.writeInt(this.button);
        buffer.writeBoolean(this.call);
    }
}