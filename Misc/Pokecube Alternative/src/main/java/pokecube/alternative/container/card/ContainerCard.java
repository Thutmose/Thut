package pokecube.alternative.container.card;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import thut.core.common.handlers.PlayerDataHandler;

public class ContainerCard extends Container
{
    public ContainerCard(EntityPlayer player)
    {
        InventoryBasic inv = PlayerDataHandler.getInstance().getPlayerData(player)
                .getData(CardPlayerData.class).inventory;
        int dx = 9;
        int dy = 69;
        for (int i1 = 0; i1 < 8; ++i1)
        {
            this.addSlotToContainer(new Slot(inv, i1, dx + i1 * 20, dy)
            {
                @Override
                public boolean canTakeStack(EntityPlayer playerIn)
                {
                    return false;
                }
            });
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }
}
