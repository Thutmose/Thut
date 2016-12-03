package pokecube.alternative.container.belt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerPlayerPokemon extends Container
{
    /** The crafting matrix inventory. */
    public InventoryPokemon pokemon;
    /** Determines if inventory manipulation should be handled. */
    public boolean          isLocalWorld;
    final EntityPlayer      thePlayer;

    public ContainerPlayerPokemon(InventoryPlayer playerInv, boolean par2, EntityPlayer player)
    {
        this.isLocalWorld = par2;
        this.thePlayer = player;
        pokemon = new InventoryPokemon(player);
        pokemon.setEventHandler(this);
        for (int c = 0; c < 6; c++)
        {
            this.addSlotToContainer(new SlotPokemon(pokemon, c, 8 - 32, 7 + 47 + c * 18));
        }
    }

    /** Callback for when the crafting matrix is changed. */
    @Override
    public void onCraftMatrixChanged(IInventory par1IInventory)
    {
    }

    /** Called when the container is closed. */
    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);
        if (!player.worldObj.isRemote)
        {
            pokemon.closeInventory(player);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer par1EntityPlayer)
    {
        return true;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
    {
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    /** Called when a player shift-clicks on a slot. You must override this or
     * you will crash when someone does that. */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
    {
        Slot slot = this.inventorySlots.get(par2);
        return slot.getStack();
    }

    @Override
    public boolean canMergeSlot(ItemStack par1ItemStack, Slot par2Slot)
    {
        return super.canMergeSlot(par1ItemStack, par2Slot);
    }

    public ItemStack getStackInSlot(int index)
    {
        return pokemon.getStackInSlot(index);
    }

}
