package thut.core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import thut.api.ThutItems;
import thut.api.network.PacketHandler;
import thut.core.client.render.model.ModelFluid;
import thut.core.common.CommonProxy;
import thut.core.common.items.ItemDusts;
import thut.core.common.items.ItemDusts.Dust;

public class ClientProxy extends CommonProxy {
	public static int renderPass;
	public static Minecraft mc;
	
	@Override
	public void initClient()
	{
		mc = FMLClientHandler.instance().getClient();
		PacketHandler.provider = this;
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ThutItems.tank, 0, new ModelResourceLocation("thutcore:tank", "inventory") );

        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ThutItems.spout, 0, new ModelResourceLocation("thutcore:spout", "inventory") );

        for(Integer id: ItemDusts.dusts.keySet())
        {
        	Dust dust = ItemDusts.dusts.get(id);
        	String modid = dust.modid;
        	String name = dust.name;
        	String ident =  modid+":"+name;
        	ModelBakery.registerItemVariants(ThutItems.dusts, new ResourceLocation(ident));
        	
        	Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ThutItems.dusts, id, new ModelResourceLocation(ident, "inventory") );
        }
        
        if(ThutItems.spreader != null)
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ThutItems.spreader, 0, new ModelResourceLocation("thutcore:spreader", "inventory") );
        
	}
	
    public void preinit(FMLPreInitializationEvent e)
    {
        ModelLoaderRegistry.registerLoader(ModelFluid.FluidLoader.instance);
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
