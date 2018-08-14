package pokecube.alternative.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.adventures.items.ItemBadge;
import pokecube.alternative.Config;
import pokecube.alternative.container.card.CardPlayerData;
import pokecube.core.utils.PokeType;
import thut.core.common.handlers.PlayerDataHandler;
import thut.lib.CompatWrapper;

public class TrainerCardHandler
{
    @SubscribeEvent
    public void PlayerUseItemEvent(PlayerInteractEvent.RightClickItem event)
    {
        ItemStack item = event.getItemStack();
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) return;
        if (Config.instance.trainerCard && ItemBadge.isBadge(item))
        {
            CardPlayerData data = PlayerDataHandler.getInstance().getPlayerData(player).getData(CardPlayerData.class);
            int index = -1;
            PokeType type = PokeType.values()[item.getItemDamage()];
            for (int i = 0; i < 8; i++)
            {
                if (Config.instance.badgeOrder[i].equalsIgnoreCase(type.name))
                {
                    index = i;
                    break;
                }
            }
            if (index == -1 || CompatWrapper.isValid(data.inventory.getStackInSlot(index))) return;
            int slotIndex = event.getEntityPlayer().inventory.currentItem;
            data.inventory.setInventorySlotContents(index, item.copy());
            player.inventory.setInventorySlotContents(slotIndex, ItemStack.EMPTY);
            PlayerDataHandler.getInstance().save(player.getCachedUniqueIdString(), data.getIdentifier());
            event.setCanceled(true);
            return;
        }
    }
}
