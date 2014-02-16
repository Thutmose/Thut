package thut.world.common;

//import powercrystals.minefactoryreloaded.api.FarmingRegistry;
import thut.world.common.blocks.tileentity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.server.FMLServerHandler;

public class CommonProxy  implements IGuiHandler
{

	public void initClient() {}
	
	public void loadConfiguration() {}
	
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
    
	public EntityPlayer getPlayer()
	{
		return null;
	}
    
    public boolean isOnClientSide()
    {
        return false;
    }

    public World getWorld()
    {
        return FMLCommonHandler.instance().getMinecraftServerInstance().worldServers[0];
    }
    
    public void registerTEs()
    {
		GameRegistry.registerTileEntity(TileEntityVolcano.class, "VolcanoTE");
    }
    
    public void registerEntities()
    {

		try {
			Class<?> registry = Class.forName("powercrystals.minefactoryreloaded.MFRRegistry");
			if(registry != null)
			{
//			FarmingRegistry.registerSafariNetBlacklist(EntityLift.class);
//			FarmingRegistry.registerSafariNetBlacklist(EntityTurret.class);
//			FarmingRegistry.registerSafariNetBlacklist(EntityBeam.class);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
		//	System.out.println("[ThutConcrete] MFR not found, lift not added to the non-existant safari net blacklist.");
		//	e.printStackTrace();
		}
    }
    
	@Override
	public Object getServerGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}
	
	@Override
	public Object getClientGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	public void loadSounds() {}
    
}
