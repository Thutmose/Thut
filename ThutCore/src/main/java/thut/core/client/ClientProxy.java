package thut.core.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import thut.api.ThutItems;
import thut.api.network.PacketHandler;
import thut.core.common.CommonProxy;
import thut.core.common.ThutCore;
import thut.core.common.blocks.fluids.BlockDust;
import thut.core.common.blocks.fluids.BlockMelt;
import thut.core.common.blocks.fluids.BlockSolidMelt;
import thut.core.common.items.ItemDusts;
import thut.core.common.items.ItemDusts.Dust;
import thut.reference.ThutCoreReference;

public class ClientProxy extends CommonProxy
{
    public static class UpdateNotifier
    {
        public UpdateNotifier()
        {
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onPlayerJoin(TickEvent.PlayerTickEvent event)
        {
            if (event.player.worldObj.isRemote && event.player == FMLClientHandler.instance().getClientPlayerEntity())
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                Object o = Loader.instance().getIndexedModList().get(ThutCoreReference.MOD_ID);
                CheckResult result = ForgeVersion.getResult(((ModContainer) o));
                if (result.status == Status.OUTDATED)
                {
                    IChatComponent mess = getOutdatedMessage(result, "Thut Core");
                    (event.player).addChatMessage(mess);
                }

            }
        }
    }

    public static int       renderPass;

    public static Minecraft mc;

    public static IChatComponent getOutdatedMessage(CheckResult result, String name)
    {
        String linkName = "[" + EnumChatFormatting.GREEN + name + " " + result.target + EnumChatFormatting.WHITE;
        String link = "" + result.url;
        String linkComponent = "{\"text\":\"" + linkName + "\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\""
                + link + "\"}}";

        String info = "\"" + EnumChatFormatting.RED + "New " + name
                + " version available, please update before reporting bugs.\nClick the green link for the page to download.\n"
                + "\"";
        String mess = "[" + info + "," + linkComponent + ",\"]\"]";
        return IChatComponent.Serializer.jsonToComponent(mess);
    }

    @Override
    public EntityPlayer getPlayer()
    {
        return getPlayer(null);
    }

    @Override
    public EntityPlayer getPlayer(String playerName)
    {
        if (isOnClientSide())
        {
            if (playerName != null)
            {
                return getWorld().getPlayerEntityByName(playerName);
            }
            else
            {
                return Minecraft.getMinecraft().thePlayer;
            }
        }
        else
        {
            return super.getPlayer(playerName);
        }
    }

    @Override
    public World getWorld()
    {
        if (isOnClientSide())
        {
            return Minecraft.getMinecraft().theWorld;
        }
        else
        {
            return super.getWorld();
        }
    }

    @Override
    public void initClient()
    {
        mc = FMLClientHandler.instance().getClient();
        PacketHandler.provider = this;
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ThutItems.tank, 0,
                new ModelResourceLocation("thutcore:tank", "inventory"));

        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ThutItems.spout, 0,
                new ModelResourceLocation("thutcore:spout", "inventory"));

        for (Integer id : ItemDusts.dusts.keySet())
        {
            Dust dust = ItemDusts.dusts.get(id);
            String modid = dust.modid;
            String name = dust.name;
            String ident = modid + ":" + name;
            ModelBakery.registerItemVariants(ThutItems.dusts, new ResourceLocation(ident));

            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ThutItems.dusts, id,
                    new ModelResourceLocation(ident, "inventory"));
        }

        if (ThutItems.spreader != null) Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
                .register(ThutItems.spreader, 0, new ModelResourceLocation("thutcore:spreader", "inventory"));

        new UpdateNotifier();
    }

    @Override
    public boolean isOnClientSide()
    {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
    }

    @Override
    public void loadSounds()
    {
        try
        {

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void preinit(FMLPreInitializationEvent e)
    {
        super.preinit(e);
        ModelBakery.registerItemVariants(Item.getItemFromBlock(BlockSolidMelt.INSTANCE));
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(BlockSolidMelt.INSTANCE), new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                return new ModelResourceLocation(ThutCore.modid.toLowerCase() + ":" + "solidmelt", "level=0");
            }
        });

        ModelBakery.registerItemVariants(Item.getItemFromBlock(BlockMelt.INSTANCE));
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(BlockMelt.INSTANCE), new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                return new ModelResourceLocation(ThutCore.modid.toLowerCase() + ":" + "melt", "fluid");
            }
        });

        ModelBakery.registerItemVariants(Item.getItemFromBlock(BlockDust.INSTANCE));
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(BlockDust.INSTANCE), new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                return new ModelResourceLocation(ThutCore.modid.toLowerCase() + ":" + "dust", "fluid");
            }
        });

        ModelLoader.setCustomStateMapper(BlockMelt.INSTANCE, new StateMapperBase()
        {
            protected ModelResourceLocation getModelResourceLocation(IBlockState state)
            {
                return new ModelResourceLocation(ThutCore.modid.toLowerCase() + ":" + "melt", "fluid");
            }
        });

        ModelLoader.setCustomStateMapper(BlockDust.INSTANCE, new StateMapperBase()
        {
            protected ModelResourceLocation getModelResourceLocation(IBlockState state)
            {
                return new ModelResourceLocation(ThutCore.modid.toLowerCase() + ":" + "dust", "fluid");
            }
        });
    }
}
