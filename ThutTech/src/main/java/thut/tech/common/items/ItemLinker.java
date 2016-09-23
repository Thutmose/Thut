package thut.tech.common.items;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.ThutBlocks;
import thut.api.boom.ExplosionCustom;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.maths.Cruncher;
import thut.api.maths.Vector3;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.BlockLift;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;

public class ItemLinker extends Item
{
    public static Item instance;

    public ItemLinker()
    {
        super();
        this.setHasSubtypes(true);
        this.setUnlocalizedName("devicelinker");
        this.setCreativeTab(TechCore.tabThut);
        instance = this;
    }

    @Override
    /** If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client. */
    public boolean getShareTag()
    {
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World world, EntityPlayer player,
            EnumHand hand)
    {
        return super.onItemRightClick(itemstack, world, player, hand);
    }

    public void tryBoom(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        int dy = -0;
        if (!playerIn.isSneaking())// && !worldIn.isRemote)
        {
            float strength = 0.5f * 1f;
            ExplosionCustom.MAX_RADIUS = 255;
            ExplosionCustom.MINBLASTDAMAGE = 0.5f;
            ExplosionCustom.AFFECTINAIR = false;
            ExplosionCustom boom = new ExplosionCustom(worldIn, playerIn, pos.getX() + 0.5, pos.getY() + 0.5 + dy,
                    pos.getZ() + 0.5, strength);
            // boom.maxPerTick[0] = 1000;
            // boom.maxPerTick[1] = 10000;
            // boom.doExplosion();

            HashMap<BlockPos, Float> resists = new HashMap<BlockPos, Float>();
            // used to speed up the checking of if a resist exists in the
            // map
            Set<BlockPos> blocked = Sets.newHashSet();
            Vector3 r = Vector3.getNewVector(), rAbs = Vector3.getNewVector(), rHat = Vector3.getNewVector(),
                    rTest = Vector3.getNewVector(), rTestPrev = Vector3.getNewVector(),
                    rTestAbs = Vector3.getNewVector();

            Vector3 centre = Vector3.getNewVector().set(pos.getX() + 0.5, pos.getY() + 0.5 + dy, pos.getZ() + 0.5);
            int ind = 0;
            BlockPos index;
            BlockPos index2;
            double scaleFactor = 1500;
            double rMag;
            float resist;
            double str;
            int num = (int) (Math.sqrt(strength * scaleFactor / 0.5));
            int max = 4 * 2 + 1;
            num = Math.min(num, max);
            num = Math.min(num, 1000);
            int numCubed = num * num * num;
            double radSq = num * num / 4;
            int maxIndex = numCubed;
            for (int currentIndex = ind; currentIndex < maxIndex; currentIndex++)
            {
                Cruncher.indexToVals(currentIndex, r);
                if (r.y + centre.y < 0 || r.y + centre.y > 255) continue;
                double rSq = r.magSq();
                if (rSq > radSq) continue;
                rMag = Math.sqrt(rSq);
                str = strength * scaleFactor / rSq;
                if (str <= 0.5)
                {
                    System.out.println("Terminating at distance " + rMag);
                    break;
                }
                rAbs.set(r).addTo(centre);
                rHat.set(r).norm();
                resist = rAbs.getExplosionResistance(boom, worldIn);
                rTestPrev.set(r);
                if (rMag >= 1)
                {
                    double dj = 1 - ((rMag - 1) / rMag);
                    for (double scale = 1; scale >= (rMag - 1) / rMag; scale -= dj)
                    {
                        rTest.set(r).scalarMultBy(scale);
                        if (rTestPrev.sameBlock(rTest)) continue;
                        rTestAbs.set(rTest).addTo(centre);
                        index2 = new BlockPos(rTest.getPos());
                        if (blocked.contains(index2))
                        {
                            resist = -1;
                        }
                        resist += resists.get(index2);
                        rTestPrev.set(rTest);
                        break;
                    }
                }
                index = new BlockPos(r.getPos());
                if (resist < str && resist >= 0)
                {
                    resists.put(index.toImmutable(), resist);
                    if (!rAbs.isAir(worldIn)) rAbs.setBlock(worldIn, Blocks.AIR.getDefaultState());
                }
                else
                {
                    blocked.add(index.toImmutable());
                }
            }
            System.out.println("Done");
        }
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        String[] vals = stack.getDisplayName().split(",");
        if (vals.length == 6 && !playerIn.isSneaking())
        {
            // TODO check for lift block in inventory to consume, of not there,
            // deny creation.
            String[] arr = stack.getDisplayName().split(",");
            BlockPos min = null;
            BlockPos max = null;
            if (arr.length == 6)
            {
                try
                {
                    min = new BlockPos(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
                    max = new BlockPos(Integer.parseInt(arr[3]), Integer.parseInt(arr[4]), Integer.parseInt(arr[5]));
                }
                catch (NumberFormatException e)
                {
                    String message = "msg.lift.badformat";
                    if (!worldIn.isRemote) playerIn.addChatMessage(new TextComponentTranslation(message));
                }
            }

            if (min != null && max != null && !worldIn.isRemote)
            {
                int lx = (max.getX() - min.getX());
                int ly = (max.getY() - min.getY());
                int lz = (max.getZ() - min.getZ());
                int volume = lx * ly * lz;
                if (volume == 0 || lx < 0 || ly < 0 || lz < 0)
                {
                    String message = "msg.lift.nosize";
                    if (!worldIn.isRemote) playerIn.addChatMessage(new TextComponentTranslation(message));
                    return EnumActionResult.FAIL;
                }
                EntityLift lift = IBlockEntity.BlockEntityFormer.makeBlockEntity(worldIn, min, max, pos,
                        EntityLift.class);
                lift.owner = playerIn.getUniqueID();
                worldIn.spawnEntityInWorld(lift);
                String message = "msg.lift.create";
                playerIn.addChatMessage(new TextComponentTranslation(message));
            }
            stack.setStackDisplayName("Device Linker");
            return EnumActionResult.SUCCESS;
        }

        if (stack.getTagCompound() == null)
        {
            // tryBoom(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY,
            // hitZ);
            return EnumActionResult.PASS;
        }
        else
        {
            IBlockState state = worldIn.getBlockState(pos);

            if (state.getBlock() == ThutBlocks.lift
                    && state.getValue(BlockLift.VARIANT) == BlockLift.EnumType.CONTROLLER && !playerIn.isSneaking())
            {
                TileEntityLiftAccess te = (TileEntityLiftAccess) worldIn.getTileEntity(pos);
                te.setSide(facing, true);
                return EnumActionResult.SUCCESS;
            }

            UUID liftID;
            try
            {
                liftID = UUID.fromString(stack.getTagCompound().getString("lift"));
            }
            catch (Exception e)
            {
                return EnumActionResult.FAIL;
            }

            EntityLift lift = EntityLift.getLiftFromUUID(liftID, worldIn);

            if (playerIn.isSneaking() && lift != null && state.getBlock() == ThutBlocks.lift
                    && state.getValue(BlockLift.VARIANT) == BlockLift.EnumType.CONTROLLER)
            {
                TileEntityLiftAccess te = (TileEntityLiftAccess) worldIn.getTileEntity(pos);

                if (facing == EnumFacing.UP)
                {
                    te.callPanel = !te.callPanel;
                    String message = "msg.callPanel.name";
                    if (worldIn.isRemote) playerIn.addChatMessage(new TextComponentTranslation(message, te.callPanel));
                }
                else
                {
                    te.setLift(lift);
                    int floor = te.getButtonFromClick(facing, hitX, hitY, hitZ);
                    te.setFloor(floor);
                    String message = "msg.floorSet.name";
                    if (worldIn.isRemote) playerIn.addChatMessage(new TextComponentTranslation(message, floor));
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    public void setLift(EntityLift lift, ItemStack stack)
    {
        if (stack.getTagCompound() == null)
        {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setString("lift", lift.getCachedUniqueIdString());
    }

    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems)
    {
        subItems.add(new ItemStack(itemIn, 1, 0));
        subItems.add(TechCore.getInfoBook());
    }
}
