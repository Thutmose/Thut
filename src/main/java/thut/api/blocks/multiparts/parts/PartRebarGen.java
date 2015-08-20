package thut.api.blocks.multiparts.parts;

import java.util.Arrays;

import scala.collection.Iterator;
import thut.api.ThutBlocks;
import thut.api.blocks.IRebar;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.minecraft.McMetaPart;
import codechicken.multipart.minecraft.McSidedMetaPart;

public class PartRebarGen extends PartRebar{

	public static Block rebar;
	public static String identifier;
	public PartRebarGen(){}
	
	public PartRebarGen(int meta) {
		super(meta);
	}
}
