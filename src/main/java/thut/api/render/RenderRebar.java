package thut.api.render;


import static org.lwjgl.opengl.GL11.*;

import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import thut.api.blocks.IRebar;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class RenderRebar implements ISimpleBlockRenderingHandler{

	public static final int ID = RenderingRegistry.getNextAvailableRenderId();
	
	public static final RenderRebar renderer = new RenderRebar();

	private RenderIRebar rebar = new RenderIRebar();

	private ResourceLocation texture;
	
	@Override
	public void renderInventoryBlock(Block parblock, int meta, int modelID,RenderBlocks renderer) 
	{
		if(!(parblock instanceof IRebar)) return;
		
		IIcon icon = parblock.getIcon(0, 0);
		boolean[] sides;
		sides = ((IRebar)parblock).getInventorySides();
		
		glPushMatrix();
		glTranslated(-0.0, -0.9, -0.0);
		glRotatef(-30,1,0,0);
		glRotatef(30,0,0,1);
		glScaled(1.45, 1.45, 1.45);
		
		GL11.glPushAttrib(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
		RenderHelper.disableStandardItemLighting();
//		
		Tessellator t = Tessellator.instance;
		
		t.startDrawing(GL11.GL_QUADS);

		rebar.tessAddRebar(t, icon, 0, 0, 0, sides, true);
		
		t.draw();
		
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopAttrib();
		GL11.glPopAttrib();
		glPopMatrix();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block parblock, int modelId, RenderBlocks renderer) {

        boolean concrete = false;
        boolean rebar = false;
		int meta = world.getBlockMetadata(x, y, z);
		boolean[] sides = null;
		IIcon icon = parblock.getIcon(0, 0);
		IIcon icon1 = parblock.getIcon(0, 0);
		IIcon[] icons = new IIcon[6];
		
		if(parblock instanceof IRebar)
		{
			rebar = true;
			IRebar temp = (IRebar) parblock;
			sides = temp.sides(world, x, y, z);
	        icon = temp.getIcon(parblock);
	        
			this.rebar.renderREConcrete(parblock, x, y, z,sides,icon, rebar);
		}
		return true;
	}


	@Override
	public int getRenderId() {
		return ID;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}

	
}
