package thut.concrete.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import thut.concrete.common.blocks.tileentity.crafting.ContainerLimekiln;
import thut.concrete.common.blocks.tileentity.crafting.TileEntityMultiFurnace;

public class GuiLimekiln extends GuiContainer
{
	private TileEntityMultiFurnace tileEntity;
	private ResourceLocation texture = new ResourceLocation("thutconcrete","gui/limeKiln.png");
	private String invTitle;
	
	public GuiLimekiln(InventoryPlayer playerInventory, TileEntityMultiFurnace tileEntity)
	{
		super(new ContainerLimekiln(playerInventory, tileEntity));
		
		this.tileEntity = tileEntity;
		invTitle = tileEntity.type==0?"Lime Kiln":"Dust Furnace";
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		//final String invTitle = "Lime Kiln";
		fontRendererObj.drawString(invTitle, xSize / 2 - fontRendererObj.getStringWidth(invTitle) / 2, 6, 4210752);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		GL11.glColor4f(1f, 1f, 1f, 1f);
		
		mc.renderEngine.bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		int i1;
		
		if(tileEntity.isBurning())
		{
			i1 = tileEntity.getBurnTimeRemainingScaled(12);
			drawTexturedModalRect(x + 126, y + 39 + 12 - i1, 176, 12 - i1, 14, i1 + 2);
		}
		
		i1 = tileEntity.getCookProgressScaled(24);
		drawTexturedModalRect(x + 74, y + 36, 176, 14, 17, i1);
	}
}
