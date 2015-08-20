package thut.api;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class ClientProxy extends CommProxy {

	public static int renderPass;

	public ClientProxy() {
		// TODO Auto-generated constructor stub
	}

	public EntityPlayer getPlayer()
	{
		return Minecraft.getMinecraft().thePlayer;
	}
}
