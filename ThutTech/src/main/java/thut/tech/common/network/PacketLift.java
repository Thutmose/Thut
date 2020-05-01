package thut.tech.common.network;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import thut.api.entity.blockentity.world.IBlockEntityWorld;
import thut.core.common.network.Packet;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.ControllerTile;
import thut.tech.common.entity.EntityLift;

public class PacketLift extends Packet
{
    public static void sendButtonPress(final BlockPos controller, final int button, final boolean callPanel)
    {
        final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(33));
        buffer.writeBoolean(false);
        buffer.writeBlockPos(controller);
        buffer.writeInt(button);
        buffer.writeBoolean(callPanel);
        final PacketLift packet = new PacketLift(buffer);
        TechCore.packets.sendToServer(packet);
    }

    public static void sendButtonPress(final EntityLift lift, final BlockPos controller, final int button,
            final boolean callPanel)
    {

        final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(37));
        buffer.writeBoolean(true);
        buffer.writeInt(lift.getEntityId());
        buffer.writeBlockPos(controller);
        buffer.writeInt(button);
        buffer.writeBoolean(callPanel);
        final PacketLift packet = new PacketLift(buffer);
        TechCore.packets.sendToServer(packet);
    }

    boolean          isMob = false;
    int              mobId = -1;
    private BlockPos pos;
    private int      button;
    private boolean  call;

    public PacketLift()
    {
    }

    public PacketLift(final PacketBuffer buffer)
    {
        this.isMob = buffer.readBoolean();
        if (this.isMob) this.mobId = buffer.readInt();
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
        TileEntity tile = null;
        if (this.isMob)
        {
            final Entity mob = player.getEntityWorld().getEntityByID(this.mobId);

            if (mob instanceof EntityLift)
            {
                final EntityLift lift = (EntityLift) mob;
                final IBlockEntityWorld world = lift.getFakeWorld();
                tile = world.getTile(this.pos);
                if (tile instanceof ControllerTile) ((ControllerTile) tile).setLift(lift);
            }
        }
        else tile = player.getEntityWorld().getTileEntity(this.pos);

        if (tile == null) return;

        if (tile instanceof ControllerTile)
        {
            final ControllerTile te = (ControllerTile) tile;
            if (te.getLift() == null) return;
            te.buttonPress(this.button, this.call);
        }
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
        buffer.writeBoolean(this.isMob);
        if (this.isMob) buffer.writeInt(this.mobId);
        buffer.writeBlockPos(this.pos);
        buffer.writeInt(this.button);
        buffer.writeBoolean(this.call);
    }
}