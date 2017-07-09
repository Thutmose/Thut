package thut.core.client.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import thut.core.common.ThutCore;
import thut.core.common.handlers.ConfigHandler;
import thut.reference.Reference;

public class ModGuiConfig extends GuiConfig
{
    public ModGuiConfig(GuiScreen guiScreen)
    {
        super(guiScreen, ConfigHandler.getConfigElements(ThutCore.instance.config), Reference.MOD_ID, false, false,
                GuiConfig.getAbridgedConfigPath(ThutCore.instance.config.getConfigFile().getAbsolutePath()));
    }
}
