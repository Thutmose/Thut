package pokecube.alternative.container.card;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import pokecube.adventures.items.ItemBadge;
import pokecube.alternative.Config;
import pokecube.core.utils.PokeType;
import thut.core.common.handlers.PlayerDataHandler;

public class ContainerCard extends Container
{
    public ContainerCard(EntityPlayer player)
    {
        InventoryBasic inv = PlayerDataHandler.getInstance().getPlayerData(player)
                .getData(CardPlayerData.class).inventory;
        int dx = 14;
        int dy = 69;
        int size = 19;
        for (int i1 = 0; i1 < 8; ++i1)
        {
            this.addSlotToContainer(new Slot(inv, i1, dx + i1 * size, dy)
            {
                @Override
                public boolean isItemValid(ItemStack stack)
                {
                    if (stack.isEmpty()) return true;
                    if (!(stack.getItem() instanceof ItemBadge)) return false;
                    int index = -1;
                    PokeType type = PokeType.values()[stack.getItemDamage()];
                    for (int i = 0; i < 8; i++)
                    {
                        if (Config.instance.badgeOrder[i].equalsIgnoreCase(type.name))
                        {
                            index = i;
                            break;
                        }
                    }
                    if (index != this.getSlotIndex()) return false;
                    return true;
                }

                @Override
                public boolean canTakeStack(EntityPlayer playerIn)
                {
                    return true;
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
