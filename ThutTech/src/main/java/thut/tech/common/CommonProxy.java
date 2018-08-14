package thut.tech.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler
{

    @Override
    public Object getClientGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z)
    {
        return null;
    }

    public EntityPlayer getPlayer()
    {
        return null;
    }

    public EntityPlayer getPlayer(String playerName)
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
    public Object getServerGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z)
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

    public void preinit(FMLPreInitializationEvent event)
    {

    }

    public void registerEntities()
    {
    }

}
