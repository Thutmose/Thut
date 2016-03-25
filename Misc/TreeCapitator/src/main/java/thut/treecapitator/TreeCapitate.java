package thut.treecapitator;

import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.moves.TreeRemover;
import thut.api.maths.Vector3;

@Mod(modid = "thuttreeremove", name = "TreeCapitator - Thut edition", version = "1.0.0", acceptableRemoteVersions = "*")
public class TreeCapitate
{
    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void breakBlock2(BreakEvent event)
    {
        ItemStack stack;
        if (event.getPlayer() != null && (stack = event.getPlayer().getHeldItem()) != null
                && stack.getItem() instanceof ItemAxe && TreeRemover.woodTypes.contains(event.state.getBlock()))
        {
            System.out.println(event.state + " " + event.world + " " + event.getPlayer() + " " + event.pos);
            Vector3 pos = Vector3.getNewVector().set(event.pos);
            TreeRemover remover = new TreeRemover(event.world, pos);
            int num = remover.cut(true) * 2;
            int avail = stack.getMaxDamage() - stack.getItemDamage();
            if (avail > num)
            {
                stack.attemptDamageItem(num, event.world.rand);
                event.setCanceled(true);
                remover.cut(false);
            }
        }
    }
}