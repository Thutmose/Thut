package thut.api.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import thut.api.explosion.Vector3;
import thut.api.explosion.Vector3.Matrix3;
import net.minecraft.entity.Entity;


public interface IMultiBox 
{
	
	public abstract void setBoxes();
	public abstract void setOffsets();

	public abstract ConcurrentHashMap<String, Matrix3> getBoxes();
	public abstract void addBox(String name, Matrix3 box);
	
	public abstract ConcurrentHashMap<String, Vector3> getOffsets();
	public abstract void addOffset(String name, Vector3 offset);
	
	public abstract void applyEntityCollision(Entity e);
	
	public abstract Matrix3 bounds(Vector3 target);
	
	abstract void checkCollision();
	
	
}
