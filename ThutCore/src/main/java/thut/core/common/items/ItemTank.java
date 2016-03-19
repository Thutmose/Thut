package thut.core.common.items;

import java.util.List;
import java.util.Map;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.ThutItems;
import thut.core.common.ThutCore;

public class ItemTank extends Item implements IFluidContainerItem
{
    public ItemTank()
    {
        super();
        this.setHasSubtypes(false);
        this.setUnlocalizedName("tank");
        this.setCreativeTab(ThutCore.tabThut);
        ThutItems.tank = this;
    }

    @Override
    public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain)
    {
        int exist = getAmount(container);
        int amount = Math.min(maxDrain, exist);
        FluidStack fluid;
        if ((fluid = getFluid(container)) == null) return null;

        String name = container.getTagCompound().getString("fluidName");
        NBTTagCompound tag = container.getTagCompound().getCompoundTag("fluidTag");
        if (tag.hasNoTags()) tag = null;
        if (doDrain)
        {
            name = fluid.getUnlocalizedName();
            if (name.contains("tile."))
            {
                name = name.replace("fluid.", "");
            }
            name = StatCollector.translateToLocal(name + ".name");
            container.getTagCompound().setInteger("fluidAmount", exist - amount);
            container.setStackDisplayName("Tank of " + name + " " + (exist - amount));
            if ((exist - amount) <= 0)
            {
                container.setTagCompound(null);
                container.setStackDisplayName("Empty Tank");
            }
        }

        return new FluidStack(FluidRegistry.getFluid(name), amount, tag);// ,
                                                                         // container.getTagCompound());
    }

    @Override
    public int fill(ItemStack container, FluidStack resource, boolean doFill)
    {
        int amount = resource.amount;
        int exist = getAmount(container);
        exist = Math.min(exist, 64000);

        int ret = 0;
        if (amount + exist <= 64000) ret = amount;
        else ret = 64000 - exist;

        if (doFill)
        {
            if (!container.hasTagCompound())
            {
                container.setTagCompound(new NBTTagCompound());
                container.getTagCompound().setString("fluidName", FluidRegistry.getFluidName(resource));
                container.getTagCompound().setInteger("fluidAmount", 0);
            }
            String name = container.getTagCompound().getString("fluidName");
            int amt = container.getTagCompound().getInteger("fluidAmount");

            if (name == null || name.trim().isEmpty())
            {
                container.getTagCompound().setString("fluidName", name = FluidRegistry.getFluidName(resource));
                container.getTagCompound().setInteger("fluidAmount", amt = 0);
                if (resource.tag != null) container.getTagCompound().setTag("fluidTag", resource.tag);
            }
            else if (!getFluid(container).isFluidEqual(resource)) { return 0; }
            name = resource.getUnlocalizedName();
            if (name.contains("tile."))
            {
                name = name.replace("fluid.", "");
            }
            name = StatCollector.translateToLocal(name + ".name");
            container.getTagCompound().setInteger("fluidAmount", amt + amount);
            container.setStackDisplayName("Tank of " + name + " " + (amt + amount));
        }

        return ret;
    }

    public int getAmount(ItemStack container)
    {
        if (getFluid(container) != null) return getFluid(container).amount;
        return 0;
    }

    @Override
    public int getCapacity(ItemStack container)
    {
        if (container.getItem() == this) return 64000;
        return 0;
    }

    @Override
    public FluidStack getFluid(ItemStack container)
    {

        if (!container.hasTagCompound()) return null;
        String name = container.getTagCompound().getString("fluidName");
        int amount = container.getTagCompound().getInteger("fluidAmount");
        NBTTagCompound tag = container.getTagCompound().getCompoundTag("fluidTag");
        if (tag.hasNoTags()) tag = null;
        if (FluidRegistry.getFluid(name) == null) return null;

        FluidStack ret = new FluidStack(FluidRegistry.getFluid(name), amount, tag);// ,
                                                                                   // container.getTagCompound());
        return ret;
    }

    @SideOnly(Side.CLIENT)
    @Override
    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List)
    {
        ItemStack tank = new ItemStack(par1, 1, 0);
        tank.setStackDisplayName("Empty Tank");
        par3List.add(tank);

        Map<String, Fluid> fluidMap = FluidRegistry.getRegisteredFluids();

        for (Fluid f : fluidMap.values())
        {
            if (f.getDensity() == Integer.MAX_VALUE || f.getViscosity() == Integer.MAX_VALUE) continue;

            tank = new ItemStack(par1, 1, 0);
            FluidStack fstack = new FluidStack(f, Integer.MAX_VALUE);
            fill(tank, fstack, true);
            par3List.add(tank);

        }

    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World worldObj, EntityPlayer player)
    {
        return itemstack;
    }
}
