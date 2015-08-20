package thut.tech.client;

import thut.api.render.RenderRebar;
import thut.tech.client.render.RenderLift;
import thut.tech.client.render.RenderLiftController;
import thut.tech.common.CommonProxy;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy{
	
	public static int renderPass;
	public static Minecraft mc;
	public static int fluidRenderID;
	public static int rebarRenderID;
	
	@Override
	public void initClient()
	{
		mc = FMLClientHandler.instance().getClient();
		RenderingRegistry.registerBlockHandler(RenderRebar.ID,RenderRebar.renderer);

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLiftAccess.class, new RenderLiftController());

		RenderingRegistry.registerEntityRenderingHandler(EntityLift.class, new RenderLift());
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
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(te==null)
			return null;

		
		return null;
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
