package thut.tech.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.state.IProperty;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLCommonSetupEvent;
import net.minecraftforge.api.distmarker.Dist;
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
    public Object getClientGuiElement(int id, PlayerEntity player, World world, int x, int y, int z)
    {
        return null;
    }

    @Override
    public PlayerEntity getPlayer()
    {
        return getPlayer(null);
    }

    @Override
    public PlayerEntity getPlayer(String playerName)
    {
        if (isOnClientSide())
        {
            if (playerName != null)
            {
                return getWorld().getPlayerEntityByName(playerName);
            }
            else
            {
                return Minecraft.getInstance().player;
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
            return Minecraft.getInstance().world;
        }
        else
        {
            return super.getWorld();
        }
    }

    @Override
    public void registerItemModels()
    {
        super.registerItemModels();
        Item lift = Item.getItemFromBlock(ThutBlocks.lift);
        ModelBakery.registerItemVariants(lift, new ModelResourceLocation("thuttech:liftcontroller", "inventory"),
                new ModelResourceLocation("thuttech:lift", "inventory"));
    }

    @Override
    public void registerBlockModels()
    {
        super.registerBlockModels();
        ModelLoader.setCustomStateMapper(ThutBlocks.lift, (new StateMap.Builder()).withName(BlockLift.VARIANT)
                .ignore(new IProperty[] { BlockLift.CALLED, BlockLift.CURRENT }).build());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLiftAccess.class, new RenderLiftController<>());
    }

    @Override
    public void initClient()
    {
        Item lift = Item.getItemFromBlock(ThutBlocks.lift);
        Minecraft.getInstance().getRenderItem().getItemModelMesher().register(lift, 0,
                new ModelResourceLocation("thuttech:lift", "inventory"));
        Minecraft.getInstance().getRenderItem().getItemModelMesher().register(lift, 1,
                new ModelResourceLocation("thuttech:liftcontroller", "inventory"));
        Minecraft.getInstance().getRenderItem().getItemModelMesher().register(ItemLinker.instance, 0,
                new ModelResourceLocation("thuttech:devicelinker", "inventory"));
    }

    @Override
    public boolean isOnClientSide()
    {
        return FMLCommonHandler.instance().getEffectiveSide() == Dist.CLIENT;
    }

    @Override
    public void loadSounds()
    {
    }

    @Override
    public void preinit(FMLCommonSetupEvent event)
    {
        super.preinit(event);
        RenderingRegistry.registerEntityRenderingHandler(EntityLift.class, new IRenderFactory<LivingEntity>()
        {
            @Override
            public Render<? super LivingEntity> createRenderFor(RenderManager manager)
            {
                return new RenderLift(manager);
            }
        });
    }

}
