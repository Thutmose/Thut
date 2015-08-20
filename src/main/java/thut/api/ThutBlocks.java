package thut.api;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;

import scala.actors.threadpool.Arrays;
import thut.api.blocks.multiparts.ByteClassLoader;
import thut.api.blocks.multiparts.parts.PartFluid;
import thut.api.blocks.multiparts.parts.PartRebar;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.minecraft.McBlockPart;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class ThutBlocks extends Blocks 
{
	//Thut WorldGen Blocks
	public static Block dust;
	public static Block inactiveDust;
	public static ByteClassLoader liquidPartloader;
	public static ByteClassLoader rebarPartloader;
	
	//Thut Concrete Blocks
	public static Block liquidConcrete;
	public static Block liquidREConcrete;
	public static Block concrete;
	public static Block reConcrete;
	public static Block liquidAsphalt;
	public static Block solidAsphalt;
	
	public static Block rebar;
	public static Block limekiln;
	public static Block mixer;
	
	//Thut Tech Blocks
	public static Block liftRail;
	public static Block lift;

	public static Block volcano;
	public static Block[] solidLavas = new Block[16];
	public static Block[] lavas = new Block[16];
	
	private static HashSet<Block> allBlocks = new HashSet<Block>();

	public static HashMap<Block, Class> parts = new HashMap();
	public static HashMap<String, Class> parts2 = new HashMap();

	public static McBlockPart getPart(Block block)
	{
		return makeFromClass(parts.get(block));
	}
	
	public static TMultiPart getPart(String identifier)
	{
		return makeFromClass(parts2.get(identifier));
	}
	
	public static void addPart(Block block, Class<?> class1)
	{
		parts.put(block, class1);
	}
	
	public static void addFluidPart(Block block, String partIdentifier)
	{		
		Class clazz = null;
		
		try {
			clazz =  liquidPartloader.generateClass(partIdentifier);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		parts.put(block, clazz);
	}
	
	public static void addRebarPart(Block block, String partIdentifier)
	{		
		Class clazz = null;
		
		try {
			clazz =  rebarPartloader.generateClass(partIdentifier);
		} catch (Exception e) {
			e.printStackTrace();
		}
		parts2.put(partIdentifier, clazz);
		parts.put(block, clazz);
	}
	
	private static McBlockPart makeFromClass(Class class_)
	{
		McBlockPart ret = null;
		
		try {
			ret = (McBlockPart) class_.getConstructor().newInstance();
		} catch (Exception e) {
			
		}
		
		return ret;
	}
	
	public static void initAllBlocks()
	{
		allBlocks.clear();
		for(int i = 0; i<4096; i++)
		{
			if(Block.getBlockById(i)!=null)
				allBlocks.add(Block.getBlockById(i));
		}
	}
	public static HashSet<Block> getAllBlocks()
	{
		if(allBlocks.size()==0)
		{
			initAllBlocks();
		}
		return allBlocks;
	}
}
