package thut.core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import thut.core.client.render.RenderFluid;
import thut.core.common.CommonProxy;
import thut.core.common.blocks.BlockFluid;

public class ClientProxy extends CommonProxy {
	public static int renderPass;
	public static Minecraft mc;
	
	@Override
	public void initClient()
	{
		mc = FMLClientHandler.instance().getClient();
		RenderingRegistry.registerBlockHandler(RenderFluid.ID,new RenderFluid());
		BlockFluid.renderID = RenderFluid.ID;
	}
	
	
    @Override
    public EntityPlayer getPlayer(String playerName)
    {
        if (isOnClientSide())
        {
            if (playerName != null)
            {
                return getWorld().getPlayerEntityByName(playerName);
            }
            else
            {
                return Minecraft.getMinecraft().thePlayer;
            }
        }
        else
        {
            return super.getPlayer(playerName);
        }
    }
    
    @Override
    public boolean isOnClientSide()
    {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
    }
    
    @Override
    public World getWorld()
    {
        if (isOnClientSide())
        {
            return Minecraft.getMinecraft().theWorld;
        }
        else
        {
            return super.getWorld();
        }
    }
	
	
	@Override
	public void loadSounds(){
		try{
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public EntityPlayer getPlayer()
	{
		return getPlayer(null);
	}
}
