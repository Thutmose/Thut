package thut.core.common.compat;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.google.common.collect.Maps;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import thut.core.common.ThutCore;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;
import thut.lib.CompatParser;
import thut.reference.Reference;

@Mod(modid = "thutcore_compat", name = "ThutCore Compat", version = "1.0", acceptedMinecraftVersions = "*", acceptableRemoteVersions = "*")
public class Compat
{
    @Instance("thutcore_compat")
    public static Compat                                  instance;
    Map<CompatClass.Phase, Set<java.lang.reflect.Method>> initMethods = Maps.newHashMap();

    public Compat()
    {
        for (Phase phase : Phase.values())
        {
            initMethods.put(phase, new HashSet<java.lang.reflect.Method>());
        }
        CompatParser.findClasses(getClass().getPackage().getName(), initMethods);
        doPhase(Phase.CONSTRUCT, null);
    }

    private void doMetastuff()
    {
        ModMetadata meta = FMLCommonHandler.instance().findContainerFor(this).getMetadata();
        meta.parent = Reference.MOD_ID;
    }

    @EventHandler
    public void init(FMLInitializationEvent evt)
    {
        doPhase(Phase.INIT, evt);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt)
    {
        doPhase(Phase.POST, evt);
    }

    @EventHandler
    public void preInit(FMLCommonSetupEvent evt)
    {
        doMetastuff();
        doPhase(Phase.PRE, evt);
    }

    private void doPhase(Phase pre, Object event)
    {
        for (java.lang.reflect.Method m : initMethods.get(pre))
        {
            try
            {
                CompatClass comp = m.getAnnotation(CompatClass.class);
                if (comp.takesEvent()) m.invoke(null, event);
                else m.invoke(null);
            }
            catch (Exception e)
            {
                ThutCore.logger.log(Level.SEVERE, "Error with ", e);
            }
        }
    }
}
