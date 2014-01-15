package thut.world.common.ticks;

import java.util.EnumSet;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandler implements ITickHandler {

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		//Ticker.updateAll();
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		Ticker.updateAll();
	}

	@Override
	public EnumSet<TickType> ticks(){
		return EnumSet.of(TickType.SERVER);}

	@Override
	public String getLabel() {return "thutWorldgen";}

}
