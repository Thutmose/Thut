package thut.api;

import codechicken.lib.packet.PacketCustom;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import thut.api.blocks.BlockFluid;
import thut.api.blocks.fluids.dusts.BlockDust;
import thut.api.blocks.fluids.dusts.BlockDustInactive;
import thut.api.blocks.multiparts.ByteClassLoader;
import thut.api.blocks.multiparts.Content;
import thut.api.blocks.multiparts.McMultipartCPH;
import thut.api.blocks.multiparts.McMultipartSPH;
import thut.api.blocks.multiparts.parts.PartFluid;
import thut.api.blocks.multiparts.parts.PartFluidGen;
import thut.api.blocks.multiparts.parts.PartRebar;
import thut.api.blocks.multiparts.parts.PartRebarGen;
import thut.api.blocks.tileentity.TileEntityMultiBlockPart;
import thut.api.blocks.tileentity.TileEntityMultiBlockPartFluids;
import thut.api.items.ItemDusts;
import thut.api.items.ItemDusts.Dust;
import thut.api.items.ItemGrinder;
import thut.api.maths.Cruncher;
import thut.reference.ThutCoreReference;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;


@Mod( modid = ThutCore.MOD_ID, name=ThutCoreReference.MOD_NAME, version=ThutCoreReference.VERSION)
public class ThutCore 
{
	public static final String MOD_ID = ThutCoreReference.MOD_ID;
	public static final String TEXTURE_PATH = MOD_ID.toLowerCase() + ":";
	
	@SidedProxy(clientSide = ThutCoreReference.CLIENT_PROXY_CLASS, serverSide = ThutCoreReference.COMMON_PROXY_CLASS)
	public static CommProxy proxy;
	
	@Instance(MOD_ID)
	public static ThutCore instance;
	public static CreativeTabThut tabThut = CreativeTabThut.tabThut;
	
	String dust;
	String dust1;
	
	public ThutCore()
	{
		new Cruncher();
	}
    
	@EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
		ThutBlocks.rebarPartloader = new ByteClassLoader(PartRebarGen.class);
		ThutBlocks.liquidPartloader = new ByteClassLoader(PartFluidGen.class);
		new BlockDust();
		new BlockDustInactive();
		Item dusts = new ItemDusts();
		addDusts();
		new ItemGrinder();
		GameRegistry.registerBlock(ThutBlocks.dust, "dustBlock");
		GameRegistry.registerBlock(ThutBlocks.inactiveDust, "inactiveDustBlock");

		GameRegistry.registerTileEntity(TileEntityMultiBlockPart.class, "thutmultiblockpartte");
		GameRegistry.registerTileEntity(TileEntityMultiBlockPartFluids.class, "thutmultiblockpartfluidste");
		
		GameRegistry.registerItem(ItemDusts.instance, "dustsItem");
		ThutItems.dust = new ItemStack(dusts);
		ThutItems.trass = new ItemStack(dusts, 1, 3);
    }
	
	  void addDusts() {
		    ItemDusts.addDust(new Dust("dust", ThutCore.MOD_ID) {
		      public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		        if(!world.isRemote && stack.getItemDamage() == 0) {
		          int x1 = ForgeDirection.getOrientation(side).offsetX + x, y1 = ForgeDirection.getOrientation(side).offsetY + y, z1 = ForgeDirection.getOrientation(side).offsetZ + z;
		          int meta = world.getBlockMetadata(x1, y1, z1);
		          Block block = world.getBlock(x1, y1, z1);

		          if(player.isSneaking() && ItemDye.applyBonemeal(stack, world, x, y, z, player)) {
		            if(!world.isRemote) {
		              world.playAuxSFX(2005, x, y, z, 0);
		            }

		            return true;
		          }

		          if(block instanceof BlockDust || block instanceof BlockDustInactive && meta != 15) {
		            world.setBlockMetadataWithNotify(x1, y1, z1, meta + 1, 3);
		            if(!player.capabilities.isCreativeMode) {
		              stack.splitStack(1);
		            }
		            return true;
		          } else if(world.getBlock(x1, y1, z1) instanceof BlockDust || world.getBlock(x1, y1, z1) instanceof BlockDustInactive && meta != 15) {
		            world.setBlockMetadataWithNotify(x1, y1, z1, meta + 1, 3);
		            if(!player.capabilities.isCreativeMode) {
		              stack.splitStack(1);
		            }
		            return true;
		          } else if(block == Blocks.air || block.getMaterial().isReplaceable()) {
		            world.setBlock(x1, y1, z1, ThutBlocks.dust, Math.min(15, stack.stackSize), 3);
		            if(!player.capabilities.isCreativeMode) {
		              stack.splitStack(Math.min(stack.stackSize, 16));
		            }
		            return true;
		          }
		        }
		        return false;
		      }
		    });
		    ItemDusts.addDust(new Dust("dustCaCO3", ThutCore.MOD_ID));
		    ItemDusts.addDust(new Dust("dustCaO", ThutCore.MOD_ID));
		    ItemDusts.addDust(new Dust("dustTrass", ThutCore.MOD_ID));
		    ItemDusts.addDust(new Dust("dustCement", ThutCore.MOD_ID));
		    ItemDusts.addDust(new Dust("dustSulfur", ThutCore.MOD_ID));

		  }

	
	@EventHandler
    public void load(FMLInitializationEvent evt)
    {
		ThutBlocks.initAllBlocks();
		new Content().init();
		MinecraftForge.EVENT_BUS.register(new thut.api.blocks.multiparts.EventHandler());
        PacketCustom.assignHandler(this, new McMultipartSPH());
        if(FMLCommonHandler.instance().getSide().isClient())
            PacketCustom.assignHandler(this, new McMultipartCPH());
    }
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent e)
	{
		initFluids();
	}
	

	
	public static void initFluids()
	{
//		for(Block b: ThutBlocks.getAllBlocks())
//		{
//			if(b instanceof BlockFluid)
//			{
//				System.out.println(b);
//				((BlockFluid)b).setData();
//			}
//		}
	}
}
