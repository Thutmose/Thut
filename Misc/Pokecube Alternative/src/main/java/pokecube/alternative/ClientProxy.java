package pokecube.alternative;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import pokecube.alternative.client.gui.GuiEvents;
import pokecube.alternative.client.gui.GuiPlayerPokemon;
import pokecube.alternative.client.gui.GuiPokemonBar;
import pokecube.alternative.client.keybindings.KeyHandler;
import pokecube.alternative.event.PokemonOverlayEventHandler;

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
        MinecraftForge.EVENT_BUS.register(new PokemonOverlayEventHandler());
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
        if (world instanceof WorldClient)
        {
            switch (ID)
            {
            case PokecubeAlternative.GUI:
                return new GuiPlayerPokemon(player);
            }
        }
        return null;
    }

    @Override
    public World getClientWorld()
    {
        return FMLClientHandler.instance().getClient().theWorld;
    }

}
