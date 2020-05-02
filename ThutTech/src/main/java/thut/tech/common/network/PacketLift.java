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
        final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(37));
        buffer.writeByte(0);
        buffer.writeInt(-1); // No known lift here
        buffer.writeBlockPos(controller);
        buffer.writeInt(button);
        buffer.writeBoolean(callPanel);
        final PacketLift packet = new PacketLift(buffer);
        TechCore.packets.sendToServer(packet);
    }

    public static void sendButtonPress(final EntityLift lift, final BlockPos controller, final int button,
            final boolean callPanel)
    {
        if (lift == null)
        {
            PacketLift.sendButtonPress(controller, button, callPanel);
            return;
        }
        final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(37));
        buffer.writeByte(1);
        buffer.writeInt(lift.getEntityId());
        buffer.writeBlockPos(controller);
        buffer.writeInt(button);
        buffer.writeBoolean(callPanel);
        final PacketLift packet = new PacketLift(buffer);
        TechCore.packets.sendToServer(packet);
    }

    public static final byte BUTTONFROMTILE = 0;
    public static final byte BUTTONFROMMOB  = 1;
    public static final byte SETFLOOR       = 2;

    byte             key   = 0;
    int              mobId = -1;
    private BlockPos pos;
    private int      button;
    private boolean  call;

    public PacketLift()
    {
    }

    public PacketLift(final PacketBuffer buffer)
    {
        this.key = buffer.readByte();
        switch (this.key)
        {
        case 0:
        case 1:
            this.mobId = buffer.readInt();
            this.pos = buffer.readBlockPos();
            this.button = buffer.readInt();
            this.call = buffer.readBoolean();
            break;
        case 2:
            break;
        }
    }

    /*
     * Handles Server side interaction.
     */
    @Override
    public void handleServer(final ServerPlayerEntity player)
    {
        TileEntity tile = player.getEntityWorld().getTileEntity(this.pos);
        EntityLift lift = null;
        final Entity mob = player.getEntityWorld().getEntityByID(this.mobId);

        switch (this.key)
        {
        case 0:
        case 1:
            if (mob instanceof EntityLift && !(tile instanceof ControllerTile))
            {
                lift = (EntityLift) mob;
                final IBlockEntityWorld world = lift.getFakeWorld();
                tile = world.getTile(this.pos);
            }
            if (tile instanceof ControllerTile)
            {
                final ControllerTile te = (ControllerTile) tile;
                if (lift != null) te.setLift(lift);
                if (te.getLift() == null) return;
                te.buttonPress(this.button, this.call);
            }
            break;
        case 2:
            break;
        }
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
        buffer.writeByte(this.key);

        switch (this.key)
        {
        case 0:
        case 1:
            buffer.writeInt(this.mobId);
            buffer.writeBlockPos(this.pos);
            buffer.writeInt(this.button);
            buffer.writeBoolean(this.call);
            break;
        case 2:
            break;
        }
    }
}