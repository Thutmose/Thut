package thut.tech.client;

import thut.api.ThutBlocks;
import thut.tech.client.render.RenderLift;
import thut.tech.client.render.RenderLiftController;
import thut.tech.common.CommonProxy;
import thut.tech.common.blocks.lift.BlockLift;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.blocks.railgun.BlockRailgun;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.items.ItemLinker;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy
{

    @Override
    public void preinit(FMLPreInitializationEvent event)
    {
        ModelLoader.setCustomStateMapper(ThutBlocks.lift, (new StateMap.Builder()).withName(BlockLift.VARIANT)
                .ignore(new IProperty[] { BlockLift.CALLED, BlockLift.CURRENT }).build());

        ModelBakery.addVariantName(Item.getItemFromBlock(ThutBlocks.lift), "thuttech:lift", "thuttech:liftcontroller");
    }

    @Override
    public void initClient()
    {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLiftAccess.class, new RenderLiftController());

        RenderingRegistry.registerEntityRenderingHandler(EntityLift.class, new RenderLift());

        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(ThutBlocks.lift),
                0, new ModelResourceLocation("thuttech:lift", "inventory"));
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(ThutBlocks.lift),
                1, new ModelResourceLocation("thuttech:liftcontroller", "inventory"));

        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(
                Item.getItemFromBlock(BlockRailgun.instance), 0,
                new ModelResourceLocation("thuttech:railgun", "inventory"));

        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ItemLinker.instance, 0,
                new ModelResourceLocation("thuttech:devicelinker", "inventory"));
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
    public boolean isOnClientSide()
    {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
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
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        return null;
    }

    @Override
    public void loadSounds()
    {
    }

    public EntityPlayer getPlayer()
    {
        return getPlayer(null);
    }

}
