package thut.spawndefuzz;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;

@Mod(modid = SpawnDefuzz.MODID, name = "Spawn Defuzzer", version = SpawnDefuzz.VERSION, acceptableRemoteVersions = "*")
public class SpawnDefuzz
{
    public static final String MODID   = "spawn_defuzzer";
    public static final String VERSION = "1.1.0";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void deFuzzRespawn(PlayerRespawnEvent event)
    {
        if (event.player.worldObj.isRemote) return;
        BlockPos worldSpawn = event.player.worldObj.getSpawnPoint();
        BlockPos playerSpawn = event.player.getBedLocation();
        if (playerSpawn == null)
        {
            new PlayerMover(event.player, worldSpawn);
        }
    }

    @SubscribeEvent
    public void deFuzzSpawn(ServerConnectionFromClientEvent event)
    {
        if (event.handler instanceof NetHandlerPlayServer)
        {
            MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
            EntityPlayer player = ((NetHandlerPlayServer) event.handler).playerEntity;
            BlockPos worldSpawn = null;
            World playerWorld = mcServer.worldServerForDimension(player.dimension);
            GameProfile gameprofile = player.getGameProfile();
            PlayerProfileCache playerprofilecache = mcServer.getPlayerProfileCache();
            GameProfile gameprofile1 = playerprofilecache.getProfileByUUID(gameprofile.getId());
            if (gameprofile1 == null && playerWorld != null)
            {
                worldSpawn = playerWorld.provider.getSpawnPoint();
            }

            if (worldSpawn != null)
            {
                new PlayerMover(player, worldSpawn);
            }
        }
    }

    static class PlayerMover
    {
        EntityPlayer player;
        BlockPos     moveTo;

        public PlayerMover(EntityPlayer toMove, BlockPos pos)
        {
            moveTo = pos;
            player = toMove;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void deFuzzRespawn(PlayerTickEvent event)
        {
            if (event.player == player && event.phase == Phase.START)
            {
                player.setPositionAndUpdate(moveTo.getX()+0.5, moveTo.getY()+0.5, moveTo.getZ()+0.5);
                MinecraftForge.EVENT_BUS.unregister(this);
            }
        }
    }
}
