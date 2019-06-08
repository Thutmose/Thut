package thut.core.client.render.particle;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thut.api.maths.Vector3;

public class ParticleHandler
{
    private static ParticleHandler instance;

    private static class ParticlePacket
    {
        final Vector3   location;
        final IParticle particle;

        public ParticlePacket(Vector3 v, IParticle p)
        {
            location = v;
            particle = p;
        }

        public void kill()
        {
            particle.kill();
        }
    }

    public static ParticleHandler Instance()
    {
        if (instance == null)
        {
            instance = new ParticleHandler();
            MinecraftForge.EVENT_BUS.register(instance);
        }
        return instance;
    }

    List<ParticlePacket> particles = Lists.newArrayList();

    public void addParticle(Vector3 location, IParticle particle)
    {
        if (particle == null || location == null || Minecraft.getInstance().gameSettings.particleSetting > 1) return;
        synchronized (particles)
        {
            particles.add(new ParticlePacket(location.copy(), particle));
        }
    }

    public void clear()
    {
        particles.clear();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderWorldPost(RenderFogEvent event)
    {
        try
        {
            synchronized (particles)
            {
                GL11.glPushMatrix();
                List<ParticlePacket> list = Lists.newArrayList();
                for (int i = 0; i < particles.size(); i++)
                {
                    ParticlePacket packet = this.particles.get(i);
                    IParticle particle = packet.particle;
                    Vector3 target = packet.location;
                    if (particle.getDuration() < 0)
                    {
                        packet.kill();
                        list.add(packet);
                        continue;
                    }
                    PlayerEntity player = Minecraft.getInstance().player;
                    Vector3 source = Vector3.getNewVector().set(player.lastTickPosX, player.lastTickPosY,
                            player.lastTickPosZ);
                    GL11.glPushMatrix();
                    source.set(target.subtract(source));
                    GL11.glTranslated(source.x, source.y, source.z);
                    double d0 = (-player.posX + player.lastTickPosX) * event.getRenderPartialTicks();
                    double d1 = (-player.posY + player.lastTickPosY) * event.getRenderPartialTicks();
                    double d2 = (-player.posZ + player.lastTickPosZ) * event.getRenderPartialTicks();
                    source.set(d0, d1, d2);
                    GL11.glTranslated(source.x, source.y, source.z);
                    particle.render(event.getRenderPartialTicks());
                    GL11.glPopMatrix();
                    if (particle.lastTick() != event.getEntity().getEntityWorld().getGameTime())
                    {
                        particle.setDuration(particle.getDuration() - 1);
                        particle.setLastTick(event.getEntity().getEntityWorld().getGameTime());
                    }
                    if (particle.getDuration() < 0)
                    {
                        packet.kill();
                        list.add(packet);
                    }
                }
                GL11.glPopMatrix();
                for (int i = 0; i < list.size(); i++)
                {
                    particles.remove(list.get(i));
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void WorldUnloadEvent(Unload evt)
    {
        if (evt.getWorld().dimension.getDimension() == 0
                && FMLCommonHandler.instance().getEffectiveSide() == Dist.CLIENT)
        {
            clear();
        }

    }
}
