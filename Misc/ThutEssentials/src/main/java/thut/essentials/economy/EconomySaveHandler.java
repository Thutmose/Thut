package thut.essentials.economy;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraftforge.fml.common.FMLCommonHandler;
import thut.essentials.economy.EconomyManager.Account;
import thut.essentials.economy.EconomyManager.Shop;

public class EconomySaveHandler
{
    static ExclusionStrategy exclusion = new ExclusionStrategy()
    {
        @Override
        public boolean shouldSkipField(FieldAttributes f)
        {
            String name = f.getName();
            return name.equals("shopMap");
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz)
        {
            return false;
        }
    };

    public static File getGlobalFolder()
    {
        String folder = FMLCommonHandler.instance().getMinecraftServerInstance().getFolderName();
        File file = FMLCommonHandler.instance().getSavesDirectory();
        File saveFolder = new File(file, folder);
        File teamsFolder = new File(saveFolder, "economy");
        if (!teamsFolder.exists()) teamsFolder.mkdirs();
        return teamsFolder;
    }

    public static void saveGlobalData()
    {
        Gson gson = new GsonBuilder().addSerializationExclusionStrategy(exclusion).setPrettyPrinting().create();
        EconomyManager.getInstance().version = EconomyManager.VERSION;
        String json = gson.toJson(EconomyManager.getInstance());
        File teamsFile = new File(getGlobalFolder(), "economy.json");
        try
        {
            FileUtils.writeStringToFile(teamsFile, json, "UTF-8");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void loadGlobalData()
    {
        File teamsFile = new File(getGlobalFolder(), "economy.json");
        if (teamsFile.exists())
        {
            try
            {
                Gson gson = new GsonBuilder().addDeserializationExclusionStrategy(exclusion).setPrettyPrinting()
                        .create();
                String json = FileUtils.readFileToString(teamsFile, "UTF-8");
                EconomyManager.instance = gson.fromJson(json, EconomyManager.class);
                for (Account account : EconomyManager.instance.bank.values())
                {
                    for (Shop shop : account.shops)
                    {
                        account.shopMap.put(shop.location, shop);
                        EconomyManager.instance.shopMap.put(shop.location, account);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            saveGlobalData();
        }
    }
}
