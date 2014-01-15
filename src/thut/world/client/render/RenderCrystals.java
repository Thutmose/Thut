package thut.world.client.render;

import static org.lwjgl.opengl.GL11.*;
import static net.minecraftforge.common.ForgeDirection.*;

import org.lwjgl.opengl.GL11;

import thut.api.utils.Vector3;
import thut.world.client.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class RenderCrystals implements ISimpleBlockRenderingHandler
{
	public static final int ID = RenderingRegistry.getNextAvailableRenderId();

	private IModelCustom model;
	
	public RenderCrystals()
	{
		model = AdvancedModelLoader.loadModel("/assets/worldgen/models/crystalPlane.obj");
	}
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID,
			RenderBlocks renderer) {

	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		
		if(ClientProxy.renderPass==0)
		{
			Vector3 here = new Vector3(x, y, z);
			for(ForgeDirection side: ForgeDirection.VALID_DIRECTIONS)
			{
				if(!here.getBlock(world).shouldSideBeRendered(world, x+side.offsetX, y+side.offsetY, z+side.offsetZ, side.ordinal()))
					continue;
				if(here.offset(side).getBlockId(world)==block.blockID)
					continue;
				if(here.offset(side).getBlock(world)!=null&&here.offset(side).getBlock(world).isBlockSolid(world, x+side.offsetX, y+side.offsetY, z+side.offsetZ, side.getOpposite().ordinal()))
				{
					if(side==UP)
					{
						new RenderCuboid(Tessellator.instance, new Icon[] {block.getIcon(0, 0)}, x+0.00, z+0.00, y+0.995, x+1-0.00, z+1-0.00, y+1);
					}
					if(side==DOWN)
					{
						new RenderCuboid(Tessellator.instance, new Icon[] {block.getIcon(0, 0)}, x+0.00, z+0.00, y, x+1-0.00, z+1-0.00, y+0.005);
					}
					if(side==NORTH)
					{
						new RenderCuboid(Tessellator.instance, new Icon[] {block.getIcon(0, 0)}, x+0.00, z, y+0.00, x+1-0.00, z+0.005, y+1-0.00);
					}
					if(side==SOUTH)
					{
						new RenderCuboid(Tessellator.instance, new Icon[] {block.getIcon(0, 0)}, x+0.00, z+0.995, y+0.00, x+1-0.00, z+1, y+1-0.00);
					}
					if(side==EAST)
					{
						new RenderCuboid(Tessellator.instance, new Icon[] {block.getIcon(0, 0)}, x+0.995, z+0.00, y+0.00, x+1, z+1-0.00, y+1-0.00);
					}
					if(side==WEST)
					{
						new RenderCuboid(Tessellator.instance, new Icon[] {block.getIcon(0, 0)}, x, z+0.00, y+0.00, x+0.005, z+1-0.00, y+1-0.00);
					}
						
				}
			}
			return true;
		}
		
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory() {
		return false;
	}

	@Override
	public int getRenderId() {
		return ID;
	}

	public void renderTileEntityAt(TileEntity te, double d0, double d1,
			double d2, float f) 
	{
		glPushMatrix();

		double x = d0+0.5, y = d1, z = d2+0.5;
		GL11.glTranslated(x, y, z);
	//	glScaled(1,0.5,1);
		World world = te.worldObj;
		Block block = Block.blocksList[world.getBlockId(te.xCoord, te.yCoord, te.zCoord)];
		
		if(te.worldObj==null||block==null) 
		{
			glPopMatrix();
			return;
		}
		{
			Vector3 here = new Vector3(te);
			for(ForgeDirection side: ForgeDirection.VALID_DIRECTIONS)
			{
				if(!here.getBlock(world).shouldSideBeRendered(world, te.xCoord+side.offsetX, te.yCoord+side.offsetY, te.zCoord+side.offsetZ, side.ordinal()))
					continue;
				if(here.offset(side).getBlockId(world)==block.blockID)
				{
					continue;
				}
				if(here.offset(side).getBlock(world)!=null&&here.offset(side).getBlock(world).isBlockSolidOnSide(world, te.xCoord+side.offsetX, te.yCoord+side.offsetY, te.zCoord+side.offsetZ, side.getOpposite()))
				{
					glPushMatrix();
					FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation("thutconcrete:textures/models/crystalPlane.png"));
					
					if(side==UP)
					{
						glRotatef(180, 1, 0, 0);
						glTranslated(0, -1, 0);
						model.renderAll();
					}
					if(side==DOWN)
					{
						model.renderAll();
					}
					if(side==EAST)
					{
						glTranslated(0.5, 0.5, 0);
						glRotatef(90, 0, 0, 1);
						model.renderAll();
					}
					if(side==WEST)
					{
						glTranslated(-0.5, 0.5, 0);
						glRotatef(-90, 0, 0, 1);
						model.renderAll();
					}
					if(side==NORTH)
					{
						glTranslated(0, 0.5, -0.5);
						glRotatef(90, 1, 0, 0);
						model.renderAll();
					}
					if(side==SOUTH)
					{
						glTranslated(0, 0.5, 0.5);
						glRotatef(-90, 1, 0, 0);
						model.renderAll();
					}
					glPopMatrix();
						
				}
			}
		}
		

		glPopMatrix();
	}

}
