package thut.world.client;

import thut.world.client.render.*;
import thut.world.common.CommonProxy;
import thut.world.common.WorldCore;
import thut.world.common.blocks.crystals.BlockCrystal;
import thut.world.common.blocks.fluids.BlockFluid;
import thut.world.common.blocks.tileentity.*;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;

public class ClientProxy extends CommonProxy{
	
	public static ClientTickHandler TH = new ClientTickHandler();
	
	public static int renderPass;
	public static Minecraft mc;
	
	@Override
	public void initClient()
	{
		mc = FMLClientHandler.instance().getClient();
		TickRegistry.registerTickHandler(TH, Side.CLIENT);
		RenderCrystals crys = new RenderCrystals();
		RenderingRegistry.registerBlockHandler(RenderFluid.ID,new RenderFluid());
		RenderingRegistry.registerBlockHandler(RenderCrystals.ID,crys);
		BlockCrystal.renderID = crys.ID;
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
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if(te==null)
			return null;
		
		
		return null;
	}
	
	@Override
	public void loadSounds(){
		try{
			MinecraftForge.EVENT_BUS.register(new ClientProxy.sounds());}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public EntityPlayer getPlayer()
	{
		return getPlayer(null);
	}
	
	public static class sounds{
		@ForgeSubscribe
		public void onSound(SoundLoadEvent event){
		//	event.manager.soundPoolSounds.addSound("railgun.ogg", ConcreteCore.class.getResource("/mods/thutconcrete/sounds/railgun.ogg"));TODO
		}
	}
	
}
