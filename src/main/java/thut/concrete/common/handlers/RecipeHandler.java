package thut.concrete.common.handlers;

import static net.minecraft.init.Blocks.brick_block;
import static net.minecraft.init.Blocks.stone;
import static net.minecraft.init.Blocks.stonebrick;
import static net.minecraft.init.Blocks.wool;
import static net.minecraft.init.Items.iron_ingot;
import static net.minecraft.init.Items.stick;
import static thut.api.ThutBlocks.limekiln;
import static thut.api.ThutBlocks.mixer;
import static thut.api.ThutBlocks.rebar;
import static thut.api.ThutItems.boneMeal;
import static thut.api.ThutItems.brushes;
import static thut.api.ThutItems.carbonate;
import static thut.api.ThutItems.cement;
import static thut.api.ThutItems.chalkOre;
import static thut.api.ThutItems.cookable;
import static thut.api.ThutItems.getItems;
import static thut.api.ThutItems.lime;
import static thut.api.ThutItems.limestoneOre;
import static thut.api.ThutItems.solidLava0;
import static thut.api.ThutItems.solidLava1;
import static thut.api.ThutItems.solidLava2;
import static thut.api.ThutItems.trass;
import static thut.api.ThutItems.trassOre;
import static thut.api.ThutItems.twoRebar;

import java.util.Vector;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import thut.api.ThutItems;
import cpw.mods.fml.common.registry.GameRegistry;

public class RecipeHandler {

	public static Vector<MixerRecipe> mixerRecipes = new Vector();
	public static Vector<ItemStack> validMixerInputs = new Vector();
	public static Vector<FluidStack> validInputFluids = new Vector();
	
	private static final String[] dyeNames = { "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyePurple", "dyeCyan", "dyeLightGray", "dyeGray", "dyePink", "dyeLime", "dyeYellow", "dyeLightBlue", "dyeMagenta", "dyeOrange", "dyeWhite" };
	
	public static void registerRecipes()
	{
		ThutItems.cookable.add(chalkOre);
		ThutItems.cookable.add(limestoneOre);
		ThutItems.cookable.add(carbonate);
		ThutItems.cookable.add(boneMeal);
		
		for(Item item : ThutItems.getItems())
		{
			if(item!=null&&item.getUnlocalizedName().toLowerCase().contains("bone"))
			{
				cookable.add(new ItemStack(item,1,0));
			}
		}
		
		for(Item item : getItems())
		{
			if(item!=null&&item.getUnlocalizedName().toLowerCase().contains("limestone"))
			{
				cookable.add(new ItemStack(item,1,0));
			}
		}
		
		for(int i = 0; i<16; i++)
		{
			GameRegistry.addRecipe(brushes[16].copy()," x "," y "," z ", 'x',new ItemStack(wool,1,i), 'y', iron_ingot, 'z', stick);
		}
		
		GameRegistry.addRecipe(new ItemStack(rebar,4),"x  "," x ","  x", 'x', iron_ingot);
		
		for (ItemStack steel : OreDictionary.getOres("ingotSteel")) 
		{
			GameRegistry.addRecipe(new ItemStack(rebar,16),"x  "," x ","  x", 'x', steel);
		}
		
		for (ItemStack steel : OreDictionary.getOres("ingotRefinedIron")) 
		{
			GameRegistry.addRecipe(new ItemStack(rebar,5),"x  "," x ","  x", 'x', steel);
		}
		
		GameRegistry.addRecipe(new ItemStack(limekiln,1,0),"xxx","x x","yyy", 'x', new ItemStack(brick_block,1), 'y', new ItemStack(stone,1));	
		GameRegistry.addRecipe(new ItemStack(mixer,1,0),"xxx","x x","yyy", 'x', new ItemStack(stonebrick,1), 'y', new ItemStack(stone,1));
		
	//	GameRegistry.addShapelessRecipe(eightLiquidConcrete,cement, ThutItems.gravel, ThutItems.gravel, ThutItems.gravel, ThutItems.gravel, ThutItems.sand, ThutItems.sand, ThutItems.sand, new ItemStack(water_bucket));

		GameRegistry.addShapelessRecipe(cement, lime, trass);
		GameRegistry.addShapelessRecipe(cement, lime, ThutItems.dust, ThutItems.dust, ThutItems.dust, ThutItems.dust, ThutItems.dust, ThutItems.dust, ThutItems.dust, ThutItems.dust);
		
		GameRegistry.addShapelessRecipe(trass, trassOre);
		GameRegistry.addShapelessRecipe(ThutItems.dust, solidLava0);
		GameRegistry.addShapelessRecipe(ThutItems.dust, solidLava1);
		GameRegistry.addShapelessRecipe(ThutItems.dust, solidLava2);
		
		for (ItemStack item : OreDictionary.getOres("rebar")) 
		{
			GameRegistry.addShapelessRecipe(new ItemStack(rebar,1,0), item);
		}
		
		for(Item i : getItems())
		{
			if(i!=null&&i!=twoRebar.getItem()&&i.getUnlocalizedName().toLowerCase().contains("rebar"))
			{
				GameRegistry.addShapelessRecipe(new ItemStack(rebar,1,0), new ItemStack(i,1,0));
			}
		}
		for(int i = 0; i<16; i++)
		{
			for (ItemStack dye : OreDictionary.getOres(dyeNames[i])) 
			{
				for(ItemStack brush : brushes)
				{
					GameRegistry.addShapelessRecipe(brushes[i].copy(), brush.copy(), dye.copy() );
				}
			}
		}
		
		addRecipe(new ItemStack[] {new ItemStack(Blocks.sand, 3), new ItemStack(Blocks.gravel, 4), cement.copy()}, 
				new FluidStack(FluidRegistry.WATER, 1000), 
				new FluidStack(FluidRegistry.getFluid("concrete"), 8000));
	}
	
	public static void addRecipe(ItemStack[] solids, FluidStack input, FluidStack outputs)
	{
		mixerRecipes.add(new MixerRecipe(solids, input, outputs));
	}
	
	public static class MixerRecipe
	{
		public final ItemStack[] solids;
		public final FluidStack liquid;
		public final FluidStack output;
		
		public MixerRecipe(ItemStack[] solids, FluidStack input, FluidStack outputs)
		{
			this.solids = solids;
			this.liquid = input;
			this.output = outputs;
			for(ItemStack stack: solids)
			{
				boolean has = false;
				for(ItemStack s: validMixerInputs)
				{
					if(s.isItemEqual(stack))
						has = true;
				}
				if(!has)
					validMixerInputs.add(stack.copy());
			}
			boolean has = false;
			for(FluidStack s: validInputFluids)
			{
				if(s.isFluidEqual(input))
					has = true;
			}
			if(!has)
				validInputFluids.add(input.copy());
		}
	}

}
