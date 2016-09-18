package pokecube.alternative.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.alternative.Reference;

public class PacketHandler
{

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE
            .newSimpleChannel(Reference.MODID.toLowerCase());

    public static void init()
    {
        int id = 0;
        INSTANCE.registerMessage(PacketOpenPokemonInventory.class, PacketOpenPokemonInventory.class, id++, Side.SERVER);
        INSTANCE.registerMessage(PacketOpenNormalInventory.class, PacketOpenNormalInventory.class, id++, Side.SERVER);
        INSTANCE.registerMessage(PacketSyncBelt.class, PacketSyncBelt.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(PacketKeyUse.class, PacketKeyUse.class, id++, Side.SERVER);
        INSTANCE.registerMessage(PacketClickPokemobSlot.class, PacketClickPokemobSlot.class, id++, Side.SERVER);
    }

}
