package thut.tech.common;

import net.minecraftforge.common.config.Configuration;
import thut.api.network.PacketPipeline;
import thut.core.common.CreativeTabThut;
import thut.tech.common.handlers.BlockHandler;
import thut.tech.common.handlers.ConfigHandler;
import thut.tech.common.handlers.ItemHandler;
import thut.tech.common.network.PacketThutTech;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod( modid = TechCore.ID, name="Thut's Tech", version="1.0.0")
public class TechCore 
{
	@SidedProxy(clientSide = "thut.tech.client.ClientProxy", serverSide = "thut.tech.common.CommonProxy")
	public static CommonProxy proxy;
	
	public static final String ID = "thuttech";
	
	@Instance(ID)
	public static TechCore instance;
	
	private static final String[]  LANGUAGES_SUPPORTED	= new String[] { "en_UK", "en_US" , "de_DE"};
	
	public static CreativeTabThut tabThut = CreativeTabThut.tabThut;
    
	@EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
		BlockHandler.registerBlocks();
		ItemHandler.registerItems();
		
		Configuration config =  new Configuration(e.getSuggestedConfigurationFile());
		ConfigHandler.load(config);
		
    }
	
	@EventHandler
    public void load(FMLInitializationEvent evt)
    {
		proxy.initClient();
		PacketPipeline.packetPipeline.initalise();
		PacketPipeline.packetPipeline.registerPacket(PacketThutTech.class);
    }
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent e)
	{

		PacketPipeline.packetPipeline.postInitialise();;
	}
	
	
}
