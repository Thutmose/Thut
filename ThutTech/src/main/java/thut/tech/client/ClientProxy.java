package thut.tech.client;

import net.minecraft.block.properties.IProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import thut.api.ThutBlocks;
import thut.tech.client.render.RenderLift;
import thut.tech.client.render.RenderLiftController;
import thut.tech.common.CommonProxy;
import thut.tech.common.blocks.lift.BlockLift;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.items.ItemLinker;

public class ClientProxy extends CommonProxy
{

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        return null;
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

    @SuppressWarnings("unchecked")
    @Override
    public void initClient()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLiftAccess.class, new RenderLiftController());
//        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDoor.class, new RenderDoor());

        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(ThutBlocks.lift),
                0, new ModelResourceLocation("thuttech:lift", "inventory"));
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(ThutBlocks.lift),
                1, new ModelResourceLocation("thuttech:liftcontroller", "inventory"));

//        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(
//                Item.getItemFromBlock(BlockRailgun.instance), 0,
//                new ModelResourceLocation("thuttech:railgun", "inventory"));

        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ItemLinker.instance, 0,
                new ModelResourceLocation("thuttech:devicelinker", "inventory"));
    }

    @Override
    public boolean isOnClientSide()
    {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
    }

    @Override
    public void loadSounds()
    {
    }

    @Override
    public void preinit(FMLPreInitializationEvent event)
    {
        ModelLoader.setCustomStateMapper(ThutBlocks.lift, (new StateMap.Builder()).withName(BlockLift.VARIANT)
                .ignore(new IProperty[] { BlockLift.CALLED, BlockLift.CURRENT }).build());

        ModelBakery.registerItemVariants(Item.getItemFromBlock(ThutBlocks.lift), new ResourceLocation("thuttech:lift"),
                new ResourceLocation("thuttech:liftcontroller"));

        RenderingRegistry.registerEntityRenderingHandler(EntityLift.class, new IRenderFactory<EntityLivingBase>()
        {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public Render<? super EntityLivingBase> createRenderFor(RenderManager manager)
            {
                return new RenderLift(manager);
            }
        });
    }

}
