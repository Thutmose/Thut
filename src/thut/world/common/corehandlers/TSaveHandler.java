package thut.world.common.corehandlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.NoSuchFieldException;
import java.lang.IllegalAccessException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.WorldAccessContainer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.WorldEvent.*;
import net.minecraftforge.event.ForgeSubscribe;
import thut.api.utils.ISaveable;

public class TSaveHandler {
	
	public static List<ISaveable> savelist = new ArrayList<ISaveable>();
	
	public TSaveHandler() {
	}
	
	public void addSavedData(ISaveable data) {
		
		savelist.add(data);
	}
	
	@ForgeSubscribe
	public void onSave(WorldEvent.Save saveEvent) {
		
		if(!saveEvent.world.isRemote)
		{
			File worldDirectory;
			
			if(saveEvent.world.getSaveHandler() instanceof SaveHandler)
			{
				worldDirectory = ((SaveHandler) saveEvent.world.getSaveHandler()).getWorldDirectory();
			}
			else {
				return;
			}
	
			NBTTagCompound nbttagcompound1 = new NBTTagCompound();
			
			for(ISaveable saveable : savelist)
			{
		        NBTTagCompound nbttagcompound = new NBTTagCompound();
		        saveable.save(nbttagcompound);
		        nbttagcompound1.setTag("Data_" + saveable.getName(), nbttagcompound);
			}
	
	        try
	        {
	            File file = new File(worldDirectory, "thutconcrete.dat");
	            CompressedStreamTools.writeCompressed(nbttagcompound1, new FileOutputStream(file));
	        }
	        catch (Exception exception)
	        {
	            exception.printStackTrace();
	        }
		}
	}
	
	@ForgeSubscribe
	public void onLoad(WorldEvent.Load loadEvent) {
		
		if(!loadEvent.world.isRemote)
		{
			File worldDirectory;
			
			if(loadEvent.world.getSaveHandler() instanceof SaveHandler)
			{
				worldDirectory = ((SaveHandler) loadEvent.world.getSaveHandler()).getWorldDirectory();
			}
			else {
				return;
			}
			
	        File file1 = new File(worldDirectory, "thutconcrete.dat");
	        NBTTagCompound nbttagcompound;
	        NBTTagCompound nbttagcompound1;
	
	        if (file1.exists())
	        {
	            try
	            {
	                nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file1));
	                
	                for(ISaveable saveable : savelist)
	                {	
	                	nbttagcompound1 = nbttagcompound.getCompoundTag("Data_" + saveable.getName());
	                	saveable.load(nbttagcompound1);
	                }
	            }
	            catch (Exception exception)
	            {
	                if (FMLCommonHandler.instance().shouldServerBeKilledQuietly())
	                {
	                    throw (RuntimeException)exception;
	                }
	                exception.printStackTrace();
	                return;
	            }
	        }
		}
	}
	
}
