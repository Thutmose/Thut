package thut.essentials.land;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import thut.essentials.land.LandManager.LandTeam;

public class LandSaveHandler
{

    static ExclusionStrategy exclusion = new ExclusionStrategy()
    {
        @Override
        public boolean shouldSkipField(FieldAttributes f)
        {
            String name = f.getName();
            return name.equals("landMap") || name.equals("teamMap");
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz)
        {
            return false;
        }
    };

    private static void loadTeamsOld()
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;

        try
        {
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
            File file = world.getSaveHandler().getMapFileFromName("PokecubeTeams");
            if (file != null && file.exists())
            {
                FileInputStream fileinputstream = new FileInputStream(file);
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();

                boolean old = !nbttagcompound.hasKey("VERSION");
                if (old) LandManager.getInstance().loadFromNBTOld(nbttagcompound.getCompoundTag("Data"));
                else LandManager.getInstance().loadFromNBT(nbttagcompound.getCompoundTag("Data"));
            }
            File upperDir = new File(file.getParentFile().getAbsolutePath());
            File dir = new File(upperDir, "PokeTeams");
            if (dir.exists() && dir.isDirectory())
            {
                for (File f : dir.listFiles())
                {
                    try
                    {
                        FileInputStream fileinputstream = new FileInputStream(f);
                        NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                        fileinputstream.close();
                        LandManager.getInstance().loadTeamFromNBT(nbttagcompound.getCompoundTag("Data"));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public static File getGlobalFolder()
    {
        String folder = FMLCommonHandler.instance().getMinecraftServerInstance().getFolderName();
        File file = FMLCommonHandler.instance().getSavesDirectory();
        File saveFolder = new File(file, folder);
        File teamsFolder = new File(saveFolder, "land");
        if (!teamsFolder.exists()) teamsFolder.mkdirs();
        return teamsFolder;
    }

    public static File getTeamFolder()
    {
        File teamFolder = new File(getGlobalFolder(), "teams");
        if (!teamFolder.exists()) teamFolder.mkdirs();
        return teamFolder;
    }

    public static void saveGlobalData()
    {
        Gson gson = new GsonBuilder().addSerializationExclusionStrategy(exclusion).setPrettyPrinting().create();
        String json = gson.toJson(LandManager.getInstance());
        File teamsFile = new File(getGlobalFolder(), "landData.json");
        try
        {
            FileUtils.writeStringToFile(teamsFile, json);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void loadGlobalData()
    {
        File teamsFile = new File(getGlobalFolder(), "landData.json");
        if (teamsFile.exists())
        {
            try
            {
                Gson gson = new GsonBuilder().addDeserializationExclusionStrategy(exclusion).setPrettyPrinting()
                        .create();
                String json = FileUtils.readFileToString(teamsFile, "UTF-8");
                LandManager.instance = gson.fromJson(json, LandManager.class);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            loadTeams();
        }
        else
        {
            loadTeamsOld();
            saveGlobalData();
        }
    }

    private static void loadTeams()
    {
        File folder = getTeamFolder();
        for (File file : folder.listFiles())
        {
            try
            {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = FileUtils.readFileToString(file, "UTF-8");
                LandTeam team = gson.fromJson(json, LandTeam.class);
                LandManager.getInstance().teamMap.put(team.teamName, team);
                for (LandChunk land : team.land.land)
                    LandManager.getInstance().addTeamLand(team.teamName, land, false);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void saveTeam(String team)
    {
        File folder = getTeamFolder();
        File teamFile = new File(folder, team + ".json");
        LandTeam land;
        if ((land = LandManager.getInstance().getTeam(team, false)) != null)
        {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(land);
            try
            {
                FileUtils.writeStringToFile(teamFile, json);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}
