package thut.core.client.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import thut.core.common.ThutCore;
import thut.core.common.handlers.ConfigHandler;
import thut.reference.Reference;

public class ModGuiConfig extends GuiConfig
{
    private static List<IConfigElement> getConfigElements()
    {
        List<IConfigElement> list = new ArrayList<>();

        ConfigHandler config = ThutCore.instance.config;
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
        super(guiScreen, getConfigElements(), Reference.MOD_ID, false, false,
                GuiConfig.getAbridgedConfigPath(ThutCore.instance.config.getConfigFile().getAbsolutePath()));
    }
}
