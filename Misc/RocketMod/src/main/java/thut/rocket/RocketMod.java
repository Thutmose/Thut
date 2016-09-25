package thut.rocket;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.entity.blockentity.RenderBlockEntity;

@Mod(modid = RocketMod.MODID, name = "Rocket Mod", version = RocketMod.VERSION)
public class RocketMod
{
    public static final String MODID   = "rocketmod";
    public static final String VERSION = "1.0.0";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        EntityRegistry.registerModEntity(EntityRocket.class, "thuttechlift", 1, this, 32, 1, true);
        DataSerializers.registerSerializer(EntityRocket.SEATSERIALIZER);
    }

    @SideOnly(Side.CLIENT)
    @EventHandler
    public void clientPreInit(FMLPreInitializationEvent event)
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityRocket.class, new IRenderFactory<EntityLivingBase>()
        {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public Render<? super EntityLivingBase> createRenderFor(RenderManager manager)
            {
                return new RenderBlockEntity(manager);
            }
        });
    }

    /** Uses player interact here to also prevent opening of inventories.
     * 
     * @param evt */
    @SubscribeEvent
    public void interactRightClickBlock(PlayerInteractEvent.RightClickBlock evt)
    {
        if (evt.getHand() == EnumHand.OFF_HAND || evt.getWorld().isRemote || evt.getItemStack() == null
                || !evt.getEntityPlayer().isSneaking() || evt.getItemStack().getItem() != Items.STICK)
            return;

        ItemStack stack = evt.getItemStack();
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
                evt.getEntityPlayer().addChatMessage(new TextComponentString("no good name."));
            }
        }
        if (min != null && max != null)
        {
            IBlockEntity.BlockEntityFormer.makeBlockEntity(evt.getWorld(), min, max, evt.getPos(), EntityRocket.class);
        }
    }
}
