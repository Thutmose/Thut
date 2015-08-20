package thut.api.blocks.multiparts.parts;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;

import scala.collection.Iterator;
import thut.api.ThutBlocks;
import thut.api.blocks.BlockFluid;
import thut.api.blocks.multiparts.IPartMeta;
import thut.api.maths.Vector3;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TRandomUpdateTick;
import codechicken.multipart.TickScheduler;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.minecraft.McMetaPart;
import cpw.mods.fml.common.FMLCommonHandler;

public class PartFluidGen extends PartFluid {

	public PartFluidGen() {
	}

	public PartFluidGen(int meta) {
		this.meta = (byte) meta;
	}
}
