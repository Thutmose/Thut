package thut.core.asm;

import java.util.Arrays;

import com.google.common.eventbus.EventBus;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import thut.reference.ThutCoreReference;

public class CoreContainer extends DummyModContainer {

    public CoreContainer()
    {
        super(new ModMetadata());
        ModMetadata myMeta = super.getMetadata();
        myMeta.authorList = Arrays.asList("Thutmose");
        myMeta.description = "Uncategorized framework";
        myMeta.modId = ThutCoreReference.MOD_ID;
        myMeta.version = ThutCoreReference.VERSION;
        myMeta.name = ThutCoreReference.MOD_NAME;
        myMeta.url = "";
    }
    
    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
    	bus.register(this);
        return true;
    }
}
