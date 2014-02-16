package thut.world.client.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL40;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util. IIcon;

public class RenderCuboid {

	public RenderCuboid(Tessellator tessellator,  IIcon[]  IIcons, double xMin, double zMin, double yMin, double xMax, double zMax, double yMax) {
		tessAddCuboid(tessellator,  IIcons, xMin, zMin, yMin, xMax, zMax, yMax);
	}

	private void tessAddCuboid(Tessellator tessellator,  IIcon[]  IIcons, double xMin, double zMin, double yMin, double xMax, double zMax, double yMax){
		
		if( IIcons.length!=6)
		{
			 IIcon i =  IIcons[0];
			 IIcons = new  IIcon[] {i,i,i,i,i,i};
		}
		
		
		
		
        double d0 = (double) IIcons[3].getMinU();
        double d1 = (double) IIcons[3].getMaxU();
        double d3 = (double) IIcons[3].getMinV();
        double d4 = (double) IIcons[3].getMaxV();

        ///////////////side1///////////////
        tessellator.addVertexWithUV(xMin, yMax, zMax, d0, d3);
        tessellator.addVertexWithUV(xMin, yMin, zMax, d0, d4);
        
        tessellator.addVertexWithUV(xMax, yMin, zMax, d1, d4);
        tessellator.addVertexWithUV(xMax, yMax, zMax, d1, d3);
        
        tessellator.addVertexWithUV(xMax, yMax, zMax, d0, d3);
        tessellator.addVertexWithUV(xMax, yMin, zMax, d0, d4);
        
        tessellator.addVertexWithUV(xMin, yMin, zMax, d1, d4);
        tessellator.addVertexWithUV(xMin, yMax, zMax, d1, d3);
		////////////////////////////////////////* /
        
        d0 = (double) IIcons[5].getMinU();
        d1 = (double) IIcons[5].getMaxU();
        d3 = (double) IIcons[5].getMinV();
        d4 = (double) IIcons[5].getMaxV();
        
        ///////////////side2///////////////
        tessellator.addVertexWithUV(xMax, yMax, zMin, d0, d3);
        tessellator.addVertexWithUV(xMax, yMin, zMin, d0, d4);
        
        tessellator.addVertexWithUV(xMax, yMin, zMax, d1, d4);
        tessellator.addVertexWithUV(xMax, yMax, zMax, d1, d3);
        
        tessellator.addVertexWithUV(xMax, yMax, zMax, d0, d3);
        tessellator.addVertexWithUV(xMax, yMin, zMax, d0, d4);
        
        tessellator.addVertexWithUV(xMax, yMin, zMin, d1, d4);
        tessellator.addVertexWithUV(xMax, yMax, zMin, d1, d3);
		////////////////////////////////////////* /
        
        
        d0 = (double) IIcons[4].getMinU();
        d1 = (double) IIcons[4].getMaxU();
        d3 = (double) IIcons[4].getMinV();
        d4 = (double) IIcons[4].getMaxV();
        
        
        
        ///////////////side3///////////////
        tessellator.addVertexWithUV(xMin, yMax, zMax, d0, d3);
        tessellator.addVertexWithUV(xMin, yMin, zMax, d0, d4);
        
        tessellator.addVertexWithUV(xMin, yMin, zMin, d1, d4);
        tessellator.addVertexWithUV(xMin, yMax, zMin, d1, d3);
        
        tessellator.addVertexWithUV(xMin, yMax, zMin, d0, d3);
        tessellator.addVertexWithUV(xMin, yMin, zMin, d0, d4);
        
        tessellator.addVertexWithUV(xMin, yMin, zMax, d1, d4);
        tessellator.addVertexWithUV(xMin, yMax, zMax, d1, d3);
		////////////////////////////////////////*/
        
        
        d0 = (double) IIcons[2].getMinU();
        d1 = (double) IIcons[2].getMaxU();
        d3 = (double) IIcons[2].getMinV();
        d4 = (double) IIcons[2].getMaxV();
        
        
        ///////////////side4///////////////
        tessellator.addVertexWithUV(xMax, yMax, zMin, d0, d3);
        tessellator.addVertexWithUV(xMax, yMin, zMin, d0, d4);
        
        tessellator.addVertexWithUV(xMin, yMin, zMin, d1, d4);
        tessellator.addVertexWithUV(xMin, yMax, zMin, d1, d3);
        
        tessellator.addVertexWithUV(xMin, yMax, zMin, d0, d3);
        tessellator.addVertexWithUV(xMin, yMin, zMin, d0, d4);
        
        tessellator.addVertexWithUV(xMax, yMin, zMin, d1, d4);
        tessellator.addVertexWithUV(xMax, yMax, zMin, d1, d3);
		////////////////////////////////////////*/
        
        
        d0 = (double) IIcons[0].getMinU();
        d1 = (double) IIcons[0].getMaxU();
        d3 = (double) IIcons[0].getMinV();
        d4 = (double) IIcons[0].getMaxV(); 
        
        
        
        ///////////////side5///////////////

        tessellator.addVertexWithUV(xMax, yMin, zMin, d0, d3);
        tessellator.addVertexWithUV(xMax, yMin, zMax, d0, d4);
        
        tessellator.addVertexWithUV(xMin, yMin, zMax, d1, d4);
        tessellator.addVertexWithUV(xMin, yMin, zMin, d1, d3);
        
        
        tessellator.addVertexWithUV(xMax, yMin, zMax, d0, d3);
        tessellator.addVertexWithUV(xMax, yMin, zMin, d0, d4);
        
        tessellator.addVertexWithUV(xMin, yMin, zMin, d1, d4);
        tessellator.addVertexWithUV(xMin, yMin, zMax, d1, d3);
       //* 
       
		////////////////////////////////////////*/   
        
        d0 = (double) IIcons[1].getMinU();
        d1 = (double) IIcons[1].getMaxU();
        d4 = (double) IIcons[1].getMinV();
        d3 = (double) IIcons[1].getMaxV();  
        
        
        
        ///////////////side6///////////////
        
        tessellator.addVertexWithUV(xMax, yMax, zMax, d0, d3);
        tessellator.addVertexWithUV(xMax, yMax, zMin, d0, d4);
        
        tessellator.addVertexWithUV(xMin, yMax, zMin, d1, d4);
        tessellator.addVertexWithUV(xMin, yMax, zMax, d1, d3);
       //* 
        
        tessellator.addVertexWithUV(xMin, yMax, zMax, d1, d4);
        tessellator.addVertexWithUV(xMin, yMax, zMin, d1, d3);
        
        tessellator.addVertexWithUV(xMax, yMax, zMin, d0, d3);
        tessellator.addVertexWithUV(xMax, yMax, zMax, d0, d4);
       
		////////////////////////////////////////*/          
	}
	
}
