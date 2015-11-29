package thut.core.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import thut.core.common.blocks.tileentity.TileEntityBlockFluid;
import thut.core.common.blocks.tileentity.TileEntityMultiBlockPart;
import thut.core.common.blocks.tileentity.TileEntityMultiBlockPartFluids;

public class CommonProxy
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
		GameRegistry.registerTileEntity(TileEntityMultiBlockPart.class, "multiblockpart");
		GameRegistry.registerTileEntity(TileEntityMultiBlockPartFluids.class, "multiblockpartfluids");
        GameRegistry.registerTileEntity(TileEntityBlockFluid.class, "thutfluidte");
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

	public void loadSounds() {}

    public void preinit(FMLPreInitializationEvent e)
    {
        // TODO Auto-generated method stub
        
    }
}
