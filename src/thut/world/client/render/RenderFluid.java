package thut.world.client.render;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScaled;
import static org.lwjgl.opengl.GL11.glTranslated;

import org.lwjgl.opengl.GL11;

import thut.api.utils.IRebar;
import thut.api.utils.Vector3;
import thut.world.client.ClientProxy;
import thut.world.common.blocks.fluids.*;
import thut.world.common.blocks.fluids.dusts.*;
import thut.world.common.blocks.fluids.liquids.*;
import thut.world.common.blocks.fluids.solids.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidBase;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
/**
 * Modified from the Default renderer for Forge fluid blocks.
 * 
 * @author King Lemming, modified by Thutmose
 * 
 */
public class RenderFluid implements ISimpleBlockRenderingHandler 
{
	public static final int ID = RenderingRegistry.getNextAvailableRenderId();
	
	RenderIRebar rebarRenderer = new RenderIRebar();
	
    static final float LIGHT_Y_NEG = 0.5F;
    static final float LIGHT_Y_POS = 1.0F;
    static final float LIGHT_XZ_NEG = 0.8F;
    static final float LIGHT_XZ_POS = 0.6F;
    static final double RENDER_OFFSET = 0;

    public float getFluidHeightAverage(float[] flow) {

        float total = 0;
        int count = 0;

        for (int i = 0; i < flow.length; i++) {
            if (flow[i] >= 1F) {
                return flow[i];
            }
            if (flow[i] >= 0) { //TODO maybe revert back to >=0?
                total += flow[i];
                count++;
            }
        }
        return total / count;
    }

    public float getFluidHeightForRender(IBlockAccess world, int x, int y, int z, BlockFluid block) {

    	int meta = world.getBlockMetadata(x, y, z);
    	int id = world.getBlockId(x, y, z);
        if (Block.blocksList[id] instanceof BlockFluid) {
            if (Block.blocksList[world.getBlockId(x, y + 1, z)]  instanceof BlockFluid) {
                return 1;
            }
            if (meta == 15) {
                return 1F;
            }
            return ((float)(meta+1))/16;
        }
        return 0;
    }
    
    public float getOverLayHeightForRender(IBlockAccess world, int x, int y, int z, Block block) {

    	int meta = world.getBlockMetadata(x, y, z);
    	int id = world.getBlockId(x, y, z);
        if (world.getBlockId(x, y, z) == block.blockID) {
            if (meta>7||world.getBlockId(x, y + 1, z) == block.blockID) {
                return 1;
            }
            if (meta == 0) {
                return 0.875F;
            }
            return (0.875F)*((float)(8-meta))/8;
        }
        return 0.00F;
    }
    
    public float getOverlayHeightAverage(float[] flow) {

        float total = 0;
        int count = 0;

        for (int i = 0; i < flow.length; i++) {
            if (flow[i] >= 0.875F) 
            {
                return flow[i];
            }
            if (flow[i] > 0) { //TODO maybe revert back to >=0?
                total += flow[i];
                count++;
            }
        }
        if(count==0)
        	count = 1;
        return total / count;
    }
    
    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {

        if (!(block instanceof BlockFluid)) {
            return false;
        }
        
        boolean solid = ((BlockFluid)block).solid;
        
        int bMeta = world.getBlockMetadata(x, y, z);
        if(bMeta==15)
        {
        	renderer.renderStandardBlock(block, x, y, z);
        	return true;
        }

        BlockFluid theFluid = (BlockFluid) block;
        
        if(theFluid.isInWater(world, x, y, z)&&ClientProxy.renderPass!=0)
        {
        	//renderFluidOverLay(world, x, y, z, Block.waterStill, renderer);
        }
        if(solid)
        {
        	renderer.renderStandardBlock(block, x, y, z);
        	return true;
        }
        
        
        Tessellator tessellator = Tessellator.instance;
        if(block instanceof IRebar)
        {
        	tessellator.setColorOpaque_F(1, 1, 1);
        	IRebar b = (IRebar) block;
            boolean rebar = true;
    		int meta = world.getBlockMetadata(x, y, z);
    		boolean[] sides = b.sides(world, (int)x, (int)y, (int)z);
    		Icon icon = block.getIcon(0, 0);
    		if(rebar)
    			rebarRenderer.tessAddRebar(tessellator, icon, x, y, z, sides, true);
        }
        
        int color = block.colorMultiplier(world, x, y, z);
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;


        boolean renderTop = world.getBlockId(x, y + 1, z) != theFluid.blockID;

        boolean renderBottom = block.shouldSideBeRendered(world, x, y - 1, z, 0) && world.getBlockId(x, y - 1, z) != theFluid.blockID;

        boolean[] renderSides = new boolean[] { block.shouldSideBeRendered(world, x, y, z - 1, 2), block.shouldSideBeRendered(world, x, y, z + 1, 3),
                block.shouldSideBeRendered(world, x - 1, y, z, 4), block.shouldSideBeRendered(world, x + 1, y, z, 5) };

        if (!renderTop && !renderBottom && !renderSides[0] && !renderSides[1] && !renderSides[2] && !renderSides[3]) {
            return false;
        } else {
            boolean rendered = false;

            double heightNW, heightSW, heightSE, heightNE;
            
            float flow11 = getFluidHeightForRender(world, x, y, z, theFluid);

            if (flow11 != 1) {
                float flow00 = getFluidHeightForRender(world, x - 1, y, z - 1, theFluid);
                float flow01 = getFluidHeightForRender(world, x - 1, y, z, theFluid);
                float flow02 = getFluidHeightForRender(world, x - 1, y, z + 1, theFluid);
                float flow10 = getFluidHeightForRender(world, x, y, z - 1, theFluid);
                float flow12 = getFluidHeightForRender(world, x, y, z + 1, theFluid);
                float flow20 = getFluidHeightForRender(world, x + 1, y, z - 1, theFluid);
                float flow21 = getFluidHeightForRender(world, x + 1, y, z, theFluid);
                float flow22 = getFluidHeightForRender(world, x + 1, y, z + 1, theFluid);

                heightNW = getFluidHeightAverage(new float[] { flow00, flow01, flow10, flow11 });
                heightSW = getFluidHeightAverage(new float[] { flow01, flow02, flow12, flow11 });
                heightSE = getFluidHeightAverage(new float[] { flow12, flow21, flow22, flow11 });
                heightNE = getFluidHeightAverage(new float[] { flow10, flow20, flow21, flow11 });
            } else {
                heightNW = flow11;
                heightSW = flow11;
                heightSE = flow11;
                heightNE = flow11;
            }
            
            boolean same = heightNE == heightSE && heightSE == heightSW && heightNW == heightSW && heightNW == heightNE;
            if(same)
            {
            	renderer.renderStandardBlock(block, x, y, z);
            	return true;
            }
            {
            if (renderer.renderAllFaces || renderTop) {
                rendered = true;

                Icon iconStill = block.getBlockTexture(world, x, y, z, 1);
                
                float flowDir = (float) BlockFluidBase.getFlowDirection(world, x, y, z);

                if (flowDir > -999.0F) {
                    iconStill = block.getBlockTexture(world, x, y, z, 1);
                }
//                heightNW -= RENDER_OFFSET;
//                heightSW -= RENDER_OFFSET;
//                heightSE -= RENDER_OFFSET;
//                heightNE -= RENDER_OFFSET;

                double u1, u2, u3, u4, v1, v2, v3, v4;

                if (flowDir < -999.0F) {
                    u2 = iconStill.getInterpolatedU(0.0D);
                    v2 = iconStill.getInterpolatedV(0.0D);
                    u1 = u2;
                    v1 = iconStill.getInterpolatedV(16.0D);
                    u4 = iconStill.getInterpolatedU(16.0D);
                    v4 = v1;
                    u3 = u4;
                    v3 = v2;
                } else {
                    float xFlow = MathHelper.sin(flowDir) * 0.25F;
                    float zFlow = MathHelper.cos(flowDir) * 0.25F;
                    u2 = iconStill.getInterpolatedU(8.0F + (-zFlow - xFlow) * 16.0F);
                    v2 = iconStill.getInterpolatedV(8.0F + (-zFlow + xFlow) * 16.0F);
                    u1 = iconStill.getInterpolatedU(8.0F + (-zFlow + xFlow) * 16.0F);
                    v1 = iconStill.getInterpolatedV(8.0F + (zFlow + xFlow) * 16.0F);
                    u4 = iconStill.getInterpolatedU(8.0F + (zFlow + xFlow) * 16.0F);
                    v4 = iconStill.getInterpolatedV(8.0F + (zFlow - xFlow) * 16.0F);
                    u3 = iconStill.getInterpolatedU(8.0F + (zFlow - xFlow) * 16.0F);
                    v3 = iconStill.getInterpolatedV(8.0F + (-zFlow - xFlow) * 16.0F);
                }
            //    System.out.println(block.getMixedBrightnessForBlock(world, x, y, z));
                tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
                tessellator.setColorOpaque_F(LIGHT_Y_POS * red, LIGHT_Y_POS * green, LIGHT_Y_POS * blue);

                tessellator.addVertexWithUV(x + 0, y + heightNW, z + 0, u2, v2);
                tessellator.addVertexWithUV(x + 0, y + heightSW, z + 1, u1, v1);
                tessellator.addVertexWithUV(x + 1, y + heightSE, z + 1, u4, v4);
                tessellator.addVertexWithUV(x + 1, y + heightNE, z + 0, u3, v3);
                
            }

            if (renderer.renderAllFaces || renderBottom) {
                rendered = true;

                tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y - 1, z));

                tessellator.setColorOpaque_F(LIGHT_Y_NEG, LIGHT_Y_NEG, LIGHT_Y_NEG);
                renderer.renderFaceYNeg(block, x, y, z, block.getBlockTexture(world, x, y, z, 0));//y + RENDER_OFFSET

            }
            for (int side = 0; side < 4; ++side) {
                int x2 = x;
                int z2 = z;

                switch (side) {
                case 0:
                    --z2;
                    break;
                case 1:
                    ++z2;
                    break;
                case 2:
                    --x2;
                    break;
                case 3:
                    ++x2;
                    break;
                }
                Icon iconFlow = block.getBlockTexture(world, x, y, z, side+2);

                if (renderer.renderAllFaces || renderSides[side]) {
                    rendered = true;

                    double ty1;
                    double tx1;
                    double ty2;
                    double tx2;
                    double tz1;
                    double tz2;

                    if (side == 0) {
                        ty1 = heightNW;
                        ty2 = heightNE;
                        tx1 = x;
                        tx2 = x + 1;
                        tz1 = z;// + RENDER_OFFSET;
                        tz2 = z;// + RENDER_OFFSET;
                    } else if (side == 1) {
                        ty1 = heightSE;
                        ty2 = heightSW;
                        tx1 = x + 1;
                        tx2 = x;
                        tz1 = z + 1;// - RENDER_OFFSET;
                        tz2 = z + 1;// - RENDER_OFFSET;
                    } else if (side == 2) {
                        ty1 = heightSW;
                        ty2 = heightNW;
                        tx1 = x;// + RENDER_OFFSET;
                        tx2 = x;// + RENDER_OFFSET;
                        tz1 = z + 1;
                        tz2 = z;
                    } else {
                        ty1 = heightNE;
                        ty2 = heightSE;
                        tx1 = x + 1;// - RENDER_OFFSET;
                        tx2 = x + 1;// - RENDER_OFFSET;
                        tz1 = z;
                        tz2 = z + 1;
                    }
                    float u1Flow = iconFlow.getInterpolatedU(0.0D);
                    float u2Flow = theFluid.solid?iconFlow.getMaxU():iconFlow.getInterpolatedU(8.0D);
                    float v1Flow = iconFlow.getInterpolatedV((1.0D - ty1) * 16.0D * 0.5D);
                    float v2Flow = iconFlow.getInterpolatedV((1.0D - ty2) * 16.0D * 0.5D);
                    float v3Flow = theFluid.solid?iconFlow.getMaxV():iconFlow.getInterpolatedV(8.0D);
                    tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x2, y, z2));
                    float sideLighting = 1.0F;

                    if (side < 2) {
                        sideLighting = LIGHT_XZ_NEG;
                    } else {
                        sideLighting = LIGHT_XZ_POS;
                    }
                    tessellator.setColorOpaque_F(LIGHT_Y_POS * sideLighting * red, LIGHT_Y_POS * sideLighting * green, LIGHT_Y_POS * sideLighting * blue);

                    tessellator.addVertexWithUV(tx1, y + ty1, tz1, u2Flow, v1Flow);
                    tessellator.addVertexWithUV(tx2, y + ty2, tz2, u1Flow, v2Flow);
                    tessellator.addVertexWithUV(tx2, y + 0, tz2, u1Flow, v3Flow);
                    tessellator.addVertexWithUV(tx1, y + 0, tz1, u2Flow, v3Flow);
                    
                }
            }
            renderer.renderMinY = 0;
            renderer.renderMaxY = 1;
            }
            
            return rendered;
        }
    }
    
    public boolean renderFluidOverLay(IBlockAccess world, int x, int y, int z, Block block, RenderBlocks renderer)
    {
    	Tessellator tessellator = Tessellator.instance;
        int color = block.colorMultiplier(world, x, y, z);
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
   
        int bMeta = world.getBlockMetadata(x, y, z);

        int up = world.getBlockId(x, y+1, z);
        
        boolean renderTop = world.getBlockId( x, y + 1 , z)!=block.blockID;

        boolean renderBottom = false;

        boolean[] renderSides = new boolean[] { world.getBlockId( x, y, z - 1)!=block.blockID, world.getBlockId( x, y, z + 1)!=block.blockID,
        		world.getBlockId( x - 1, y, z)!=block.blockID,world.getBlockId( x + 1, y, z)!=block.blockID };
        
        
        if((up==Block.waterStill.blockID||up==Block.waterMoving.blockID))
        {
            tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
            tessellator.setColorOpaque_F(LIGHT_Y_POS * red, LIGHT_Y_POS * green, LIGHT_Y_POS * blue);

            for (int side = 0; side < 4; ++side) {
                int x2 = x;
                int z2 = z;

                switch (side) {
                case 0:
                    --z2;
                    break;
                case 1:
                    ++z2;
                    break;
                case 2:
                    --x2;
                    break;
                case 3:
                    ++x2;
                    break;
                }
                Icon iconFlow = block.getIcon(side+2, bMeta);

                if (renderer.renderAllFaces || renderSides[side]) {

                    double ty1;
                    double tx1;
                    double ty2;
                    double tx2;
                    double tz1;
                    double tz2;

                    if (side == 0) {
                        ty1 = 1;
                        ty2 = 1;
                        tx1 = x;
                        tx2 = x + 1;
                        tz1 = z + RENDER_OFFSET;
                        tz2 = z + RENDER_OFFSET;
                    } else if (side == 1) {
                        ty1 = 1;
                        ty2 = 1;
                        tx1 = x + 1;
                        tx2 = x;
                        tz1 = z + 1 - RENDER_OFFSET;
                        tz2 = z + 1 - RENDER_OFFSET;
                    } else if (side == 2) {
                        ty1 = 1;
                        ty2 = 1;
                        tx1 = x + RENDER_OFFSET;
                        tx2 = x + RENDER_OFFSET;
                        tz1 = z + 1;
                        tz2 = z;
                    } else {
                        ty1 = 1;
                        ty2 = 1;
                        tx1 = x + 1 - RENDER_OFFSET;
                        tx2 = x + 1 - RENDER_OFFSET;
                        tz1 = z;
                        tz2 = z + 1;
                    }
                    float u1Flow = iconFlow.getInterpolatedU(0.0D);
                    float u2Flow = iconFlow.getInterpolatedU(8.0D);
                    float v1Flow = iconFlow.getInterpolatedV((1.0D - ty1) * 16.0D * 0.5D);
                    float v2Flow = iconFlow.getInterpolatedV((1.0D - ty2) * 16.0D * 0.5D);
                    float v3Flow = iconFlow.getInterpolatedV(8.0D);
                    tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x2, y, z2));
                    float sideLighting = 1.0F;

                    if (side < 2) {
                        sideLighting = LIGHT_XZ_NEG;
                    } else {
                        sideLighting = LIGHT_XZ_POS;
                    }
                    tessellator.setColorOpaque_F(LIGHT_Y_POS * sideLighting * red, LIGHT_Y_POS * sideLighting * green, LIGHT_Y_POS * sideLighting * blue);

                    tessellator.addVertexWithUV(tx1, y + ty1, tz1, u2Flow, v1Flow);
                    tessellator.addVertexWithUV(tx2, y + ty2, tz2, u1Flow, v2Flow);
                    tessellator.addVertexWithUV(tx2, y + 0, tz2, u1Flow, v3Flow);
                    tessellator.addVertexWithUV(tx1, y + 0, tz1, u2Flow, v3Flow);
                    
                }
            }

        	return true;
        }
        //renderSides = new boolean[] {false,false,false,false};
        
        if (!renderTop && !renderBottom && !renderSides[0] && !renderSides[1] && !renderSides[2] && !renderSides[3]) {
            return false;
        } else {
            boolean rendered = false;

            double heightNW, heightSW, heightSE, heightNE;
            
            float flow11 = getOverLayHeightForRender(world, x, y, z, block);

            if (flow11 != 1) {
                float flow00 = getOverLayHeightForRender(world, x - 1, y, z - 1, block);
                float flow01 = getOverLayHeightForRender(world, x - 1, y, z, block);
                float flow02 = getOverLayHeightForRender(world, x - 1, y, z + 1, block);
                float flow10 = getOverLayHeightForRender(world, x, y, z - 1, block);
                float flow12 = getOverLayHeightForRender(world, x, y, z + 1, block);
                float flow20 = getOverLayHeightForRender(world, x + 1, y, z - 1, block);
                float flow21 = getOverLayHeightForRender(world, x + 1, y, z, block);
                float flow22 = getOverLayHeightForRender(world, x + 1, y, z + 1, block);

                heightNW = getOverlayHeightAverage(new float[] { flow00, flow01, flow10, flow11 });
                heightSW = getOverlayHeightAverage(new float[] { flow01, flow02, flow12, flow11 });
                heightSE = getOverlayHeightAverage(new float[] { flow12, flow21, flow22, flow11 });
                heightNE = getOverlayHeightAverage(new float[] { flow10, flow20, flow21, flow11 });
            } else {
                heightNW = flow11;
                heightSW = flow11;
                heightSE = flow11;
                heightNE = flow11;
            }
            boolean same = heightNW==heightSW&&heightSE==heightNE&&heightNW==heightNE;
       //     same = same || (theFluid.hasFloatState&&theFluid.isFloating(world, x, y, z));
            if(same&&bMeta==0)
            {
                rendered = true;
            	renderer.renderStandardBlock(block, x, y, z);
            	return rendered;
            }
            {
            if (renderer.renderAllFaces || renderTop) {
                rendered = true;

                Icon iconStill = block.getIcon(1, bMeta);
                
                float flowDir = (float) BlockFluidBase.getFlowDirection(world, x, y, z);

//                if (flowDir > -999.0F) {
//                    iconStill = block.getIcon(2, bMeta);
//                }
                heightNW -= 0.0010000000474974513D;
                heightSW -= 0.0010000000474974513D;
                heightSE -= 0.0010000000474974513D;
                heightNE -= 0.0010000000474974513D;

                double u1, u2, u3, u4, v1, v2, v3, v4;

                if (flowDir < -999.0F) {
                    u2 = iconStill.getInterpolatedU(0.0D);
                    v2 = iconStill.getInterpolatedV(0.0D);
                    u1 = u2;
                    v1 = iconStill.getInterpolatedV(16.0D);
                    u4 = iconStill.getInterpolatedU(16.0D);
                    v4 = v1;
                    u3 = u4;
                    v3 = v2;
                } else {
                    float xFlow = MathHelper.sin(flowDir) * 0.25F;
                    float zFlow = MathHelper.cos(flowDir) * 0.25F;
                    u2 = iconStill.getInterpolatedU(8.0F + (-zFlow - xFlow) * 16.0F);
                    v2 = iconStill.getInterpolatedV(8.0F + (-zFlow + xFlow) * 16.0F);
                    u1 = iconStill.getInterpolatedU(8.0F + (-zFlow + xFlow) * 16.0F);
                    v1 = iconStill.getInterpolatedV(8.0F + (zFlow + xFlow) * 16.0F);
                    u4 = iconStill.getInterpolatedU(8.0F + (zFlow + xFlow) * 16.0F);
                    v4 = iconStill.getInterpolatedV(8.0F + (zFlow - xFlow) * 16.0F);
                    u3 = iconStill.getInterpolatedU(8.0F + (zFlow - xFlow) * 16.0F);
                    v3 = iconStill.getInterpolatedV(8.0F + (-zFlow - xFlow) * 16.0F);
                }
                tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
                tessellator.setColorOpaque_F(LIGHT_Y_POS * red, LIGHT_Y_POS * green, LIGHT_Y_POS * blue);

                tessellator.addVertexWithUV(x + 0, y + heightNW, z + 0, u2, v2);
                tessellator.addVertexWithUV(x + 0, y + heightSW, z + 1, u1, v1);
                tessellator.addVertexWithUV(x + 1, y + heightSE, z + 1, u4, v4);
                tessellator.addVertexWithUV(x + 1, y + heightNE, z + 0, u3, v3);
                
            }

            if (renderer.renderAllFaces || renderBottom) {
                rendered = true;

                tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y - 1, z));

                tessellator.setColorOpaque_F(LIGHT_Y_NEG, LIGHT_Y_NEG, LIGHT_Y_NEG);
                renderer.renderFaceYNeg(block, x, y + RENDER_OFFSET, z, block.getIcon(0, bMeta));

            }
            for (int side = 0; side < 4; ++side) {
                int x2 = x;
                int z2 = z;

                switch (side) {
                case 0:
                    --z2;
                    break;
                case 1:
                    ++z2;
                    break;
                case 2:
                    --x2;
                    break;
                case 3:
                    ++x2;
                    break;
                }
                Icon iconFlow = block.getIcon(side+2, bMeta);

                if (renderer.renderAllFaces || renderSides[side]) {
                    rendered = true;

                    double ty1;
                    double tx1;
                    double ty2;
                    double tx2;
                    double tz1;
                    double tz2;

                    if (side == 0) {
                        ty1 = heightNW;
                        ty2 = heightNE;
                        tx1 = x;
                        tx2 = x + 1;
                        tz1 = z + RENDER_OFFSET;
                        tz2 = z + RENDER_OFFSET;
                    } else if (side == 1) {
                        ty1 = heightSE;
                        ty2 = heightSW;
                        tx1 = x + 1;
                        tx2 = x;
                        tz1 = z + 1 - RENDER_OFFSET;
                        tz2 = z + 1 - RENDER_OFFSET;
                    } else if (side == 2) {
                        ty1 = heightSW;
                        ty2 = heightNW;
                        tx1 = x + RENDER_OFFSET;
                        tx2 = x + RENDER_OFFSET;
                        tz1 = z + 1;
                        tz2 = z;
                    } else {
                        ty1 = heightNE;
                        ty2 = heightSE;
                        tx1 = x + 1 - RENDER_OFFSET;
                        tx2 = x + 1 - RENDER_OFFSET;
                        tz1 = z;
                        tz2 = z + 1;
                    }
                    float u1Flow = iconFlow.getInterpolatedU(0.0D);
                    float u2Flow = iconFlow.getInterpolatedU(8.0D);
                    float v1Flow = iconFlow.getInterpolatedV((1.0D - ty1) * 16.0D * 0.5D);
                    float v2Flow = iconFlow.getInterpolatedV((1.0D - ty2) * 16.0D * 0.5D);
                    float v3Flow = iconFlow.getInterpolatedV(8.0D);
                    tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x2, y, z2));
                    float sideLighting = 1.0F;

                    if (side < 2) {
                        sideLighting = LIGHT_XZ_NEG;
                    } else {
                        sideLighting = LIGHT_XZ_POS;
                    }
                    tessellator.setColorOpaque_F(LIGHT_Y_POS * sideLighting * red, LIGHT_Y_POS * sideLighting * green, LIGHT_Y_POS * sideLighting * blue);

                    tessellator.addVertexWithUV(tx1, y + ty1, tz1, u2Flow, v1Flow);
                    tessellator.addVertexWithUV(tx2, y + ty2, tz2, u1Flow, v2Flow);
                    tessellator.addVertexWithUV(tx2, y + 0, tz2, u1Flow, v3Flow);
                    tessellator.addVertexWithUV(tx1, y + 0, tz1, u2Flow, v3Flow);
                    
                }
            }
            renderer.renderMinY = 0;
            renderer.renderMaxY = 1;
            }
            
            return rendered;
        }
    }
    
    
    

	@Override
	public boolean shouldRender3DInInventory() 
	{
		return true;
	}

	@Override
	public int getRenderId() {
		return ID;
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID,
			RenderBlocks renderer) {
		glPushMatrix();
		glTranslated(-0.25, -0.25, -0.25);
		
		GL11.glPushAttrib(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
		RenderHelper.disableStandardItemLighting();
		Tessellator t = Tessellator.instance;
		t.startDrawing(GL11.GL_QUADS);
		new RenderCuboid(t, new Icon[] {block.getIcon(0, metadata)}, 0, 0, 0, 1, 1, 1);
		t.draw();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopAttrib();
		GL11.glPopAttrib();
		glPopMatrix();
	}

}
