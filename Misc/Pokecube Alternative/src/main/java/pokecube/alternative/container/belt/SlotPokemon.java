package pokecube.alternative.container.belt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import pokecube.core.items.pokecubes.PokecubeManager;

public class SlotPokemon extends Slot {

    public SlotPokemon(IInventory par2IInventory, int par3, int par4, int par5) {
        super(par2IInventory, par3, par4, par5);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if(PokecubeManager.isFilled(stack)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return this.getStack()!=null;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

}
