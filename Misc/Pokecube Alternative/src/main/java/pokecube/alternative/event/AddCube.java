package pokecube.alternative.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityEvent;
import pokecube.alternative.container.belt.BeltPlayerData;
import pokecube.alternative.container.belt.IPokemobBelt;

public class AddCube extends EntityEvent
{
    public final ItemStack    cube;
    public final IPokemobBelt cap;

    public AddCube(EntityPlayer player, ItemStack cube)
    {
        super(player);
        this.cap = BeltPlayerData.getBelt(player);
        this.cube = cube;
    }

    public static class OnRecall extends AddCube
    {
        public OnRecall(EntityPlayer player, ItemStack cube)
        {
            super(player, cube);
        }
    }

}
