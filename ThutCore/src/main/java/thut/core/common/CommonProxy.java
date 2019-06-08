package thut.core.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import thut.api.network.IPlayerProvider;

public class CommonProxy implements IPlayerProvider
{

    @Override
    public PlayerEntity getPlayer()
    {
        return null;
    }

    public PlayerEntity getPlayer(String playerName)
    {
        if (playerName != null) { return getWorld().getPlayerEntityByName(playerName); }
        return null;
    }

    public World getWorld()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().worlds[0];
    }

    public void initClient()
    {
    }

    public boolean isOnClientSide()
    {
        return false;
    }

    public void loadConfiguration()
    {
    }

    public void loadSounds()
    {
    }

    public void preinit(FMLCommonSetupEvent e)
    {
    }

    public void registerEntities()
    {
    }

    public void registerTEs()
    {
    }
}
