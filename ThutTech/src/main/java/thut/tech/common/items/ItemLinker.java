package thut.tech.common.items;

import java.util.UUID;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thut.api.ThutBlocks;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.maths.Vector3;
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
        if (FMLCommonHandler.instance().getSide() == Dist.CLIENT) MinecraftForge.EVENT_BUS.register(this);
        instance = this;
    }

    @Override
    /** If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client. */
    public boolean getShareTag()
    {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void RenderBounds(DrawBlockHighlightEvent event)
    {
        ItemStack held;
        PlayerEntity player = event.getPlayer();
        if ((held = player.getHeldItemMainhand()) != null || (held = player.getHeldItemOffhand()) != null)
        {
            BlockPos pos = event.getTarget().getBlockPos();
            if (pos == null) return;
            if (!player.getEntityWorld().getBlockState(pos).getMaterial().isSolid())
            {
                Vec3d loc = player.getPositionVector().add(0, player.getEyeHeight(), 0)
                        .add(player.getLookVec().scale(2));
                pos = new BlockPos(loc);
            }

            if (held.getItem() == this && held.getTag() != null && held.getTag().hasKey("min"))
            {
                BlockPos min = Vector3.readFromNBT(held.getTag().getCompound("min"), "").getPos();
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
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
    {
        return onItemRightClick(player.getHeldItem(hand), world, player, hand);
    }

    // 1.10
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World worldIn, PlayerEntity playerIn,
            Hand hand)
    {
        if (itemstack.hasTag() && playerIn.isSneaking() && itemstack.getTag().hasKey("min"))
        {
            CompoundNBT minTag = itemstack.getTag().getCompound("min");
            Vec3d loc = playerIn.getPositionVector().add(0, playerIn.getEyeHeight(), 0)
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
                if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message));
                return new ActionResult<>(ActionResultType.PASS, itemstack);
            }
            int num = (dw + 1) * (max.getY() - min.getY() + 1);
            int count = 0;
            for (ItemStack item : playerIn.inventory.mainInventory)
            {
                if (item != null)
                {
                    ItemStack test = item.copy();
                    test.setCount(liftblocks.getCount());
                    if (ItemStack.areItemStacksEqual(test, liftblocks)) count += item.getCount();
                }
            }
            if (!playerIn.capabilities.isCreativeMode && count < num)
            {
                String message = "msg.lift.noblock";
                if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message, num));
                return new ActionResult<>(ActionResultType.PASS, itemstack);
            }
            else if (!playerIn.capabilities.isCreativeMode)
            {
                playerIn.inventory.clearMatchingItems(liftblocks.getItem(), liftblocks.getItemDamage(), num,
                        liftblocks.getTag());
            }
            if (!worldIn.isRemote)
            {
                EntityLift lift = IBlockEntity.BlockEntityFormer.makeBlockEntity(worldIn, min, max, mid,
                        EntityLift.class);
                if (lift != null) lift.owner = playerIn.getUniqueID();
                String message = lift != null ? "msg.lift.create" : "msg.lift.fail";
                playerIn.sendMessage(new TranslationTextComponent(message));
            }
            itemstack.getTag().remove("min");
        }
        return new ActionResult<>(ActionResultType.PASS, itemstack);
    }

    // 1.11
    public ActionResultType onItemUse(PlayerEntity playerIn, World worldIn, BlockPos pos, Hand hand,
            Direction side, float hitX, float hitY, float hitZ)
    {
        return onItemUse(playerIn.getHeldItem(hand), playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ);
    }

    // 1.10
    public ActionResultType onItemUse(ItemStack stack, PlayerEntity playerIn, World worldIn, BlockPos pos,
            Hand hand, Direction facing, float hitX, float hitY, float hitZ)
    {
        if (stack.getTag() == null) stack.setTag(new CompoundNBT());
        boolean hasLift = stack.getTag().hasKey("lift");
        if (!playerIn.isSneaking() && !hasLift)
        {
            stack.setTag(new CompoundNBT());
            CompoundNBT min = new CompoundNBT();
            Vector3.getNewVector().set(pos).writeToNBT(min, "");
            stack.getTag().setTag("min", min);
            String message = "msg.lift.setcorner";
            if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message, pos));
            return ActionResultType.SUCCESS;
        }
        else if (playerIn.isSneaking() && stack.hasTag() && stack.getTag().hasKey("min"))
        {

            CompoundNBT minTag = stack.getTag().getCompound("min");
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
                if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message));
                return ActionResultType.FAIL;
            }
            int num = (dw + 1) * (max.getY() - min.getY() + 1);
            int count = 0;
            for (ItemStack item : playerIn.inventory.mainInventory)
            {
                if (item != null)
                {
                    ItemStack test = item.copy();
                    test.setCount(liftblocks.getCount());
                    if (ItemStack.areItemStacksEqual(test, liftblocks)) count += item.getCount();
                }
            }
            if (!playerIn.capabilities.isCreativeMode && count < num)
            {
                String message = "msg.lift.noblock";
                if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message, num));
                return ActionResultType.FAIL;
            }
            else if (!playerIn.capabilities.isCreativeMode)
            {
                playerIn.inventory.clearMatchingItems(liftblocks.getItem(), liftblocks.getItemDamage(), num,
                        liftblocks.getTag());
            }
            if (!worldIn.isRemote)
            {
                EntityLift lift = IBlockEntity.BlockEntityFormer.makeBlockEntity(worldIn, min, max, mid,
                        EntityLift.class);
                if (lift != null) lift.owner = playerIn.getUniqueID();
                String message = lift != null ? "msg.lift.create" : "msg.lift.fail";
                playerIn.sendMessage(new TranslationTextComponent(message));
            }
            stack.getTag().remove("min");
            return ActionResultType.SUCCESS;
        }

        if (stack.getTag() == null)
        {
            return ActionResultType.PASS;
        }
        else
        {
            BlockState state = worldIn.getBlockState(pos);

            if (state.getBlock() == ThutBlocks.lift
                    && state.getValue(BlockLift.VARIANT) == BlockLift.EnumType.CONTROLLER && !playerIn.isSneaking())
            {
                TileEntityLiftAccess te = (TileEntityLiftAccess) worldIn.getTileEntity(pos);
                te.setSide(facing, true);
                return ActionResultType.SUCCESS;
            }

            UUID liftID;
            try
            {
                liftID = UUID.fromString(stack.getTag().getString("lift"));
            }
            catch (Exception e)
            {
                liftID = new UUID(0000, 0000);
            }
            EntityLift lift = EntityLift.getLiftFromUUID(liftID, worldIn);
            if (playerIn.isSneaking() && lift != null && state.getBlock() == ThutBlocks.lift
                    && state.getValue(BlockLift.VARIANT) == BlockLift.EnumType.CONTROLLER)
            {
                if (facing != Direction.UP && facing != Direction.DOWN)
                {
                    TileEntityLiftAccess te = (TileEntityLiftAccess) worldIn.getTileEntity(pos);
                    te.setLift(lift);
                    int floor = te.getButtonFromClick(facing, hitX, hitY, hitZ);
                    te.setFloor(floor);
                    if (floor >= 64) floor = 64 - floor;
                    String message = "msg.floorSet.name";
                    if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message, floor));
                    return ActionResultType.SUCCESS;
                }
            }
            else if (playerIn.isSneaking() && state.getBlock() == ThutBlocks.lift
                    && state.getValue(BlockLift.VARIANT) == BlockLift.EnumType.CONTROLLER)
            {
                if (facing != Direction.UP && facing != Direction.DOWN)
                {
                    TileEntityLiftAccess te = (TileEntityLiftAccess) worldIn.getTileEntity(pos);
                    te.editFace[facing.ordinal()] = !te.editFace[facing.ordinal()];
                    te.setSidePage(facing, 0);
                    String message = "msg.editMode.name";
                    if (!worldIn.isRemote)
                        playerIn.sendMessage(new TranslationTextComponent(message, te.editFace[facing.ordinal()]));
                    return ActionResultType.SUCCESS;
                }
            }
            else if (playerIn.isSneaking())
            {
                stack.setTag(new CompoundNBT());
                String message = "msg.linker.reset";
                if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message));
            }
        }
        return ActionResultType.PASS;
    }

    public void setLift(EntityLift lift, ItemStack stack)
    {
        if (stack.getTag() == null)
        {
            stack.setTag(new CompoundNBT());
        }
        stack.getTag().putString("lift", lift.getCachedUniqueIdString());
    }

    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
    {
        if (tab != getCreativeTab()) return;
        subItems.add(new ItemStack(this, 1, 0));
        subItems.add(TechCore.getInfoBook());
    }
}
