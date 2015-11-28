package thut.api.entity;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;


public interface IMultibox 
{
	public abstract void setBoxes();
	public abstract void setOffsets();

	public abstract HashMap<String, Matrix3> getBoxes();
	
	public abstract HashMap<String, Vector3> getOffsets();
	
	public abstract void applyEntityCollision(Entity e);
	
	public abstract Matrix3 bounds(Vector3 target);
	
	public abstract void checkCollision();
	
}
