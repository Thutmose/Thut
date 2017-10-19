package thut.tech.common.items;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.ThutBlocks;
import thut.api.boom.ExplosionCustom;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.maths.Cruncher;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.BlockLift;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.handlers.ConfigHandler;

public class ItemLinker extends Item
{
    public static Item      instance;
    public static ItemStack liftblocks;

    public ItemLinker()
    {
        super();
        this.setHasSubtypes(true);
        this.setUnlocalizedName("devicelinker");
        this.setCreativeTab(TechCore.tabThut);
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) MinecraftForge.EVENT_BUS.register(this);
        instance = this;
    }

    @Override
    /** If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client. */
    public boolean getShareTag()
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void RenderBounds(DrawBlockHighlightEvent event)
    {
        ItemStack held;
        EntityPlayer player = event.getPlayer();
        if ((held = player.getHeldItemMainhand()) != null || (held = player.getHeldItemOffhand()) != null)
        {
            BlockPos pos = event.getTarget().getBlockPos();
            if (pos == null) return;
            if (!player.getEntityWorld().getBlockState(pos).getMaterial().isSolid())
            {
                Vec3d loc = player.getPositionVector().addVector(0, player.getEyeHeight(), 0)
                        .add(player.getLookVec().scale(2));
                pos = new BlockPos(loc);
            }

            if (held.getItem() == this && held.getTagCompound() != null && held.getTagCompound().hasKey("min"))
            {
                BlockPos min = Vector3.readFromNBT(held.getTagCompound().getCompoundTag("min"), "").getPos();
                BlockPos max = pos;
                AxisAlignedBB box = new AxisAlignedBB(min, max);
                min = new BlockPos(box.minX, box.minY, box.minZ);
                max = new BlockPos(box.maxX, box.maxY, box.maxZ).add(1, 1, 1);
                box = new AxisAlignedBB(min, max);
                float partialTicks = event.getPartialTicks();
                double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
                double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
                double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;
                box = box.offset(-d0, -d1, -d2);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ZERO);
                GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
                GlStateManager.glLineWidth(2.0F);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                GlStateManager.color(1.0F, 0.0F, 0.0F, 1F);
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder vertexbuffer = tessellator.getBuffer();
                vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
                vertexbuffer.pos(box.minX, box.minY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.minY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.minY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.minY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.minY, box.minZ).endVertex();
                tessellator.draw();
                vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
                vertexbuffer.pos(box.minX, box.maxY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.maxY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.maxY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.maxY, box.minZ).endVertex();
                tessellator.draw();
                vertexbuffer.begin(1, DefaultVertexFormats.POSITION);
                vertexbuffer.pos(box.minX, box.minY, box.minZ).endVertex();
                vertexbuffer.pos(box.minX, box.maxY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.minY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.maxY, box.minZ).endVertex();
                vertexbuffer.pos(box.maxX, box.minY, box.maxZ).endVertex();
                vertexbuffer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.minY, box.maxZ).endVertex();
                vertexbuffer.pos(box.minX, box.maxY, box.maxZ).endVertex();
                tessellator.draw();
                GlStateManager.depthMask(true);
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
            }
        }
    }

    // 1.11
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        return onItemRightClick(player.getHeldItem(hand), world, player, hand);
    }

    // 1.10
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World worldIn, EntityPlayer playerIn,
            EnumHand hand)
    {
        if (itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("min") && hand == EnumHand.MAIN_HAND)
        {
            if (!playerIn.isSneaking())
            {
                itemstack.setTagCompound(new NBTTagCompound());
                String message = "msg.linker.reset";
                if (!worldIn.isRemote) playerIn.sendMessage(new TextComponentTranslation(message));
                return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
            }

            NBTTagCompound minTag = itemstack.getTagCompound().getCompoundTag("min");
            Vec3d loc = playerIn.getPositionVector().addVector(0, playerIn.getEyeHeight(), 0)
                    .add(playerIn.getLookVec().scale(2));
            BlockPos pos = new BlockPos(loc);
            BlockPos min = pos;
            BlockPos max = Vector3.readFromNBT(minTag, "").getPos();
            AxisAlignedBB box = new AxisAlignedBB(min, max);
            min = new BlockPos(box.minX, box.minY, box.minZ);
            max = new BlockPos(box.maxX, box.maxY, box.maxZ);
            BlockPos mid = min.add((max.getX() - min.getX()) / 2, 0, (max.getZ() - min.getZ()) / 2);
            min = min.subtract(mid);
            max = max.subtract(mid);

            int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
            if (max.getY() - min.getY() > ConfigHandler.maxHeight || dw > 2 * ConfigHandler.maxRadius + 1)
            {
                String message = "msg.lift.toobig";
                if (!worldIn.isRemote) playerIn.sendMessage(new TextComponentTranslation(message));
                return new ActionResult<>(EnumActionResult.PASS, itemstack);
            }
            int num = (dw + 1) * (max.getY() - min.getY() + 1);
            int count = 0;
            for (ItemStack item : playerIn.inventory.mainInventory)
            {
                if (item != null)
                {
                    ItemStack test = item.copy();
                    CompatWrapper.setStackSize(test, CompatWrapper.getStackSize(liftblocks));
                    if (ItemStack.areItemStacksEqual(test, liftblocks)) count += CompatWrapper.getStackSize(item);
                }
            }
            if (!playerIn.capabilities.isCreativeMode && count < num)
            {
                String message = "msg.lift.noblock";
                if (!worldIn.isRemote) playerIn.sendMessage(new TextComponentTranslation(message, num));
                return new ActionResult<>(EnumActionResult.PASS, itemstack);
            }
            else if (!playerIn.capabilities.isCreativeMode)
            {
                playerIn.inventory.clearMatchingItems(liftblocks.getItem(), liftblocks.getItemDamage(), num,
                        liftblocks.getTagCompound());
            }
            if (!worldIn.isRemote)
            {
                EntityLift lift = IBlockEntity.BlockEntityFormer.makeBlockEntity(worldIn, min, max, mid,
                        EntityLift.class);
                lift.owner = playerIn.getUniqueID();
                String message = "msg.lift.create";
                playerIn.sendMessage(new TextComponentTranslation(message));
            }
            itemstack.getTagCompound().removeTag("min");
            return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
        }
        return new ActionResult<>(EnumActionResult.PASS, itemstack);
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

    // 1.11
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing side, float hitX, float hitY, float hitZ)
    {
        return onItemUse(playerIn.getHeldItem(hand), playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ);
    }

    // 1.10
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        boolean hasLift = stack.getTagCompound() != null && stack.getTagCompound().hasKey("lift");

        if (!playerIn.isSneaking() && !hasLift)
        {
            stack.setTagCompound(new NBTTagCompound());
            NBTTagCompound min = new NBTTagCompound();
            Vector3.getNewVector().set(pos).writeToNBT(min, "");
            stack.getTagCompound().setTag("min", min);
            String message = "msg.lift.setcorner";
            if (!worldIn.isRemote) playerIn.sendMessage(new TextComponentTranslation(message, pos));
            return EnumActionResult.SUCCESS;
        }
        else if (playerIn.isSneaking() && stack.hasTagCompound() && stack.getTagCompound().hasKey("min"))
        {

            NBTTagCompound minTag = stack.getTagCompound().getCompoundTag("min");
            BlockPos min = pos;
            BlockPos max = Vector3.readFromNBT(minTag, "").getPos();
            AxisAlignedBB box = new AxisAlignedBB(min, max);
            min = new BlockPos(box.minX, box.minY, box.minZ);
            max = new BlockPos(box.maxX, box.maxY, box.maxZ);
            BlockPos mid = min.add((max.getX() - min.getX()) / 2, 0, (max.getZ() - min.getZ()) / 2);
            min = min.subtract(mid);
            max = max.subtract(mid);
            int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
            if (max.getY() - min.getY() > ConfigHandler.maxHeight || dw > 2 * ConfigHandler.maxRadius + 1)
            {
                String message = "msg.lift.toobig";
                if (!worldIn.isRemote) playerIn.sendMessage(new TextComponentTranslation(message));
                return EnumActionResult.FAIL;
            }
            int num = (dw + 1) * (max.getY() - min.getY() + 1);
            int count = 0;
            for (ItemStack item : playerIn.inventory.mainInventory)
            {
                if (item != null)
                {
                    ItemStack test = item.copy();
                    CompatWrapper.setStackSize(test, CompatWrapper.getStackSize(liftblocks));
                    if (ItemStack.areItemStacksEqual(test, liftblocks)) count += CompatWrapper.getStackSize(item);
                }
            }
            if (!playerIn.capabilities.isCreativeMode && count < num)
            {
                String message = "msg.lift.noblock";
                if (!worldIn.isRemote) playerIn.sendMessage(new TextComponentTranslation(message, num));
                return EnumActionResult.FAIL;
            }
            else if (!playerIn.capabilities.isCreativeMode)
            {
                playerIn.inventory.clearMatchingItems(liftblocks.getItem(), liftblocks.getItemDamage(), num,
                        liftblocks.getTagCompound());
            }
            if (!worldIn.isRemote)
            {
                EntityLift lift = IBlockEntity.BlockEntityFormer.makeBlockEntity(worldIn, min, max, mid,
                        EntityLift.class);
                lift.owner = playerIn.getUniqueID();
                String message = "msg.lift.create";
                playerIn.sendMessage(new TextComponentTranslation(message));
            }
            stack.getTagCompound().removeTag("min");
            return EnumActionResult.SUCCESS;
        }

        if (stack.getTagCompound() == null)
        {
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
                stack.setTagCompound(new NBTTagCompound());
                String message = "msg.linker.reset";
                if (!worldIn.isRemote) playerIn.sendMessage(new TextComponentTranslation(message));
                return EnumActionResult.SUCCESS;
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
                    if (!worldIn.isRemote) playerIn.sendMessage(new TextComponentTranslation(message, te.callPanel));
                }
                else
                {
                    te.setLift(lift);
                    int floor = te.getButtonFromClick(facing, hitX, hitY, hitZ);
                    te.setFloor(floor);
                    String message = "msg.floorSet.name";
                    if (!worldIn.isRemote) playerIn.sendMessage(new TextComponentTranslation(message, floor));
                }
                return EnumActionResult.SUCCESS;
            }
            else if (playerIn.isSneaking())
            {
                stack.setTagCompound(new NBTTagCompound());
                String message = "msg.linker.reset";
                if (!worldIn.isRemote) playerIn.sendMessage(new TextComponentTranslation(message));
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
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
    {
        if (!this.isInCreativeTab(tab)) return;
        subItems.add(new ItemStack(this, 1, 0));
        subItems.add(TechCore.getInfoBook());
    }
}
