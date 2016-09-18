package pokecube.alternative.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.alternative.PokecubeAlternative;

public class PacketOpenPokemonInventory implements IMessage, IMessageHandler<PacketOpenPokemonInventory, IMessage> {

    public PacketOpenPokemonInventory() {}

    public PacketOpenPokemonInventory(EntityPlayer player) {}

    @Override
    public void toBytes(ByteBuf buffer) {}

    @Override
    public void fromBytes(ByteBuf buffer) {}

    @Override
    public IMessage onMessage(PacketOpenPokemonInventory message, MessageContext ctx) {
        ctx.getServerHandler().playerEntity.openGui(PokecubeAlternative.instance, PokecubeAlternative.GUI, ctx.getServerHandler().playerEntity.worldObj, (int)ctx.getServerHandler().playerEntity.posX, (int)ctx.getServerHandler().playerEntity.posY, (int)ctx.getServerHandler().playerEntity.posZ);
        return null;
    }

}
