package thut.core.common;

import thut.core.common.blocks.tileentity.TileEntityBlockFluid;
import thut.core.common.blocks.tileentity.TileEntityMultiBlockPart;
import thut.core.common.blocks.tileentity.TileEntityMultiBlockPartFluids;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;

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
		GameRegistry.registerTileEntity(TileEntityBlockFluid.class, "paintableTE");
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
}
