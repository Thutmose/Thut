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
        INSTANCE.registerMessage(PacketSyncBelt.class, PacketSyncBelt.class, id++, Side.CLIENT);
        INSTANCE.registerMessage(PacketKeyUse.class, PacketKeyUse.class, id++, Side.SERVER);
        INSTANCE.registerMessage(PacketPokemobGui.class, PacketPokemobGui.class, id++, Side.SERVER);
        INSTANCE.registerMessage(PacketSyncEnabled.class, PacketSyncEnabled.class, id++, Side.CLIENT);
    }

}
