package thut.tech.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler
{

    @Override
    public Object getClientGuiElement(int guiID, PlayerEntity player, World world, int x, int y, int z)
    {
        return null;
    }

    public PlayerEntity getPlayer()
    {
        return null;
    }

    public PlayerEntity getPlayer(String playerName)
    {
        if (playerName != null)
        {
            return getWorld().getPlayerEntityByName(playerName);
        }
        else
        {
            return null;
        }
    }

    @Override
    public Object getServerGuiElement(int guiID, PlayerEntity player, World world, int x, int y, int z)
    {
        return null;
    }

    public World getWorld()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().worlds[0];
    }

    public void initClient()
    {
    }

    public void registerBlockModels()
    {
    }

    public void registerItemModels()
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

    public void preinit(FMLCommonSetupEvent event)
    {

    }

    public void registerEntities()
    {
    }

}
