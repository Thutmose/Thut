package pokecube.alternative.player;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import pokecube.alternative.container.InventoryPokemon;

public class PlayerHandler
{

    private static HashMap<String, InventoryPokemon> playerPokemon = new HashMap<String, InventoryPokemon>();

    public static void clearPlayerPokemon(EntityPlayer player)
    {
        playerPokemon.remove(player.getDisplayNameString());
    }

    public static InventoryPokemon getPlayerPokemon(EntityPlayer player)
    {
        return new InventoryPokemon(player);
    }
}
