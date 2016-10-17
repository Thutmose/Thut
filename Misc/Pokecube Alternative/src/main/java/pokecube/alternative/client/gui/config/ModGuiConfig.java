package pokecube.alternative.client.gui.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import pokecube.alternative.Config;
import pokecube.alternative.Reference;

public class ModGuiConfig extends GuiConfig
{
    private static List<IConfigElement> getConfigElements()
    {
        List<IConfigElement> list = new ArrayList<>();

        Config config = Config.instance;
        for (String cat : config.getCategoryNames())
        {
            ConfigCategory cc = config.getCategory(cat);
            if (!cc.isChild())
            {
                ConfigElement ce = new ConfigElement(cc);
                list.add(ce);
            }
        }
        return list;
    }

    public ModGuiConfig(GuiScreen guiScreen)
    {
        super(guiScreen, getConfigElements(), Reference.MODID, false, false,
                GuiConfig.getAbridgedConfigPath(Config.instance.getConfigFile().getAbsolutePath()));
    }
}
