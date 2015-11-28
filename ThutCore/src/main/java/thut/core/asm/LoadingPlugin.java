package thut.core.asm;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import thut.reference.ThutCoreReference;

@IFMLLoadingPlugin.Name(value = ThutCoreReference.MOD_ID)
@IFMLLoadingPlugin.TransformerExclusions(value = {"thut.core.asm."})
public class LoadingPlugin implements IFMLLoadingPlugin
{

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[]{};
    }

    @Override
    public String getModContainerClass()
    {
        return "thut.core.asm.CoreContainer";
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data)
    {
    }

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }
}