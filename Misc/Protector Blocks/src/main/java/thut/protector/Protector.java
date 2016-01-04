package thut.protector;

import java.util.ArrayList;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = Protector.MODID, name = "Protector", version = Protector.VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = Protector.MCVERSIONS)
public class Protector
{
    public static final String MODID      = "protector";
    public static final String VERSION    = "1.0.0";
    public final static String MCVERSIONS = "[1.8.8,1.8.9]";

    @SidedProxy
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        GameRegistry.registerBlock(ProtectionBlock.instance, ProtectionBlock.name);
        GameRegistry.registerTileEntity(ProtectionTileEntity.class, ProtectionBlock.name);
    }

    @SubscribeEvent
    public void placeEvent(PlaceEvent event)
    {
        TileEntity te = event.world.getTileEntity(event.pos);
        if (te != null && te instanceof ProtectionTileEntity)
        {
            ProtectionTileEntity ownable = (ProtectionTileEntity) te;
            ownable.setPlacer(event.player);
        }
    }

    public static class CommonProxy
    {

    }

    @SideOnly(Side.CLIENT)
    public static class ClientProxy extends CommonProxy
    {

    }

    public static class ProtectionBlock extends Block implements ITileEntityProvider
    {
        public static final ProtectionBlock instance = new ProtectionBlock();
        public static final String          name     = "ProtectionBlock";

        private ProtectionBlock()
        {
            super(Material.iron);
            setCreativeTab(CreativeTabs.tabBlock);
            setUnlocalizedName(MODID + ":" + name);
        }

        @Override
        public TileEntity createNewTileEntity(World worldIn, int meta)
        {
            return new ProtectionTileEntity();
        }

        @Override
        public boolean isOpaqueCube()
        {
            return false;
        }

        @Override
        public boolean isFullCube()
        {
            return false;
        }

        @Override
        public boolean isVisuallyOpaque()
        {
            return false;
        }

        @Override
        public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                EnumFacing side, float hitX, float hitY, float hitZ)
        {
            // Open Gui for tile entity configuration?
            return false;
        }

        @Override
        public boolean hasTileEntity(IBlockState state)
        {
            return true;
        }
    }

    public static class ProtectionTileEntity extends TileEntity
    {
        public UUID              placer;
        public int               distance      = 30 * 30;
        public ArrayList<String> allowedAccess = Lists.newArrayList();

        public ProtectionTileEntity()
        {
        }

        public void setPlacer(Entity placer)
        {
            this.placer = placer.getUniqueID();
        }

        public boolean canEdit(Entity editor)
        {
            if (placer == null || placer.compareTo(editor.getUniqueID()) != 0) return false;
            return true;
        }

        @Override
        public void readFromNBT(NBTTagCompound tagCompound)
        {
            super.readFromNBT(tagCompound);
            if (tagCompound.getBoolean("owned"))
            {
                placer = new UUID(tagCompound.getLong("uuidMost"), tagCompound.getLong("uuidLeast"));
            }
        }

        @Override
        public void writeToNBT(NBTTagCompound tagCompound)
        {
            super.writeToNBT(tagCompound);
            if (placer != null)
            {
                tagCompound.setBoolean("owned", true);
                tagCompound.setLong("uuidMost", placer.getMostSignificantBits());
                tagCompound.setLong("uuidLeast", placer.getLeastSignificantBits());
            }
        }

        /** invalidates a tile entity */
        public void invalidate()
        {
            super.invalidate();
            MinecraftForge.EVENT_BUS.unregister(this);
        }

        /** validates a tile entity */
        public void validate()
        {
            super.validate();
            MinecraftForge.EVENT_BUS.register(this);
        }

        private boolean inRange(BlockPos pos)
        {
            return getPos().distanceSq(pos) < distance;
        }

        @SubscribeEvent
        public void breakEvent(BreakEvent evt)
        {
            if (placer == null || evt.getPlayer() == null || !inRange(evt.pos)) return;
            if (evt.getPlayer().getUniqueID().equals(placer)) return;
            for (String s : allowedAccess)
            {
                if (s.equals(evt.getPlayer().getUniqueID().toString())) return;
            }
            evt.getPlayer().addChatComponentMessage(new ChatComponentText("Protected Area, Access Denied"));
            evt.setCanceled(true);
        }

        @SubscribeEvent
        public void interactEvent(PlayerInteractEvent evt)
        {
            if (placer == null || evt.entityPlayer == null || !inRange(evt.pos)) return;
            if (evt.entityPlayer.getUniqueID().equals(placer)) return;
            for (String s : allowedAccess)
            {
                if (s.equals(evt.entityPlayer.getUniqueID().toString())) return;
            }
            evt.entityPlayer.addChatComponentMessage(new ChatComponentText("Protected Area, Access Denied"));
            evt.setCanceled(true);
        }

        @SubscribeEvent
        public void explosionEvent(ExplosionEvent.Start evt)
        {
            if (placer == null || evt.explosion.getExplosivePlacedBy() == null
                    || !inRange(new BlockPos(evt.explosion.getPosition())))
                return;
            if(!(evt.explosion.getExplosivePlacedBy() instanceof EntityPlayer)) return;
            EntityPlayer player = (EntityPlayer) evt.explosion.getExplosivePlacedBy();
            if (player.getUniqueID().equals(placer)) return;
            for (String s : allowedAccess)
            {
                if (s.equals(player.getUniqueID().toString())) return;
            }
            player.addChatComponentMessage(new ChatComponentText("Protected Area, Access Denied"));
            evt.setCanceled(true);
        }
    }
}
