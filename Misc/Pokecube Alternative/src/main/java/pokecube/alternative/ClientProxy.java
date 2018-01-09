package pokecube.alternative;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import pokecube.alternative.client.gui.GuiCard;
import pokecube.alternative.client.gui.GuiEvents;
import pokecube.alternative.client.gui.GuiPokemonBar;
import pokecube.alternative.client.keybindings.KeyHandler;

public class ClientProxy extends CommonProxy
{

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
        KeyHandler.init();
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(new GuiEvents());
        MinecraftForge.EVENT_BUS.register(new KeyHandler());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
        MinecraftForge.EVENT_BUS.register(new GuiPokemonBar(Minecraft.getMinecraft()));
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        return new GuiCard(player);
    }

    @Override
    public World getClientWorld()
    {
        return FMLClientHandler.instance().getClient().world;
    }

}
