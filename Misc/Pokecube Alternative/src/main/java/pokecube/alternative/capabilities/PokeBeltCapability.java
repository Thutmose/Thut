package pokecube.alternative.capabilities;

import java.util.concurrent.Callable;

import net.minecraft.item.ItemStack;

public class PokeBeltCapability implements IBeltCapability
{
    public static class Factory implements Callable<IBeltCapability>
    {
        @Override
        public IBeltCapability call() throws Exception
        {
            return new PokeBeltCapability();
        }
    }

    boolean     firstTime = true;

    ItemStack[] cubes     = new ItemStack[6];

    int slot = 1;

    @Override
    public ItemStack getCube(int index)
    {
        return cubes[index];
    }

    @Override
    public void setCube(int index, ItemStack stack)
    {
        cubes[index] = stack;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void setSlot(int index) {
        slot = index;
    }

}
