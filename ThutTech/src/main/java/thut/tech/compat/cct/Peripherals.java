package thut.tech.compat.cct;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.lua.ArgumentHelper;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thut.core.common.ThutCore;
import thut.tech.common.blocks.lift.ControllerTile;

public class Peripherals
{
    private static boolean reged = false;

    public static void register()
    {
        if (!Peripherals.reged)
        {
            Peripherals.reged = true;
            ThutCore.LOGGER.info("Registering CC Peripheral!");
            ComputerCraftAPI.registerPeripheralProvider(new ElevatorPeripheralProvider());
        }
    }

    public static class ElevatorPeripheral implements IPeripheral
    {
        public static String[] METHODS = { "move_by", "goto_floor", "find", "has" };

        private final ControllerTile tile;

        public ElevatorPeripheral(final ControllerTile tile)
        {
            this.tile = tile;
        }

        @Override
        public String getType()
        {
            return "lift";
        }

        @Override
        public String[] getMethodNames()
        {
            return ElevatorPeripheral.METHODS;
        }

        @Override
        public Object[] callMethod(final IComputerAccess computer, final ILuaContext context, final int method,
                final Object[] arguments) throws LuaException, InterruptedException
        {
            if (method != 3 && this.tile.getLift() == null) throw new LuaException("No Elevator Linked!");
            String dir;
            int dist;
            switch (method)
            {
            case 0:
                if (arguments.length != 2) throw new LuaException("Arguments: [x|y|z] [distance]");
                dir = ArgumentHelper.getString(arguments, 0);
                dist = ArgumentHelper.getInt(arguments, 1);
                if (dir.equalsIgnoreCase("x")) this.tile.getLift().setDestX((float) (this.tile.getLift().posX + dist));
                if (dir.equalsIgnoreCase("y")) this.tile.getLift().setDestY((float) (this.tile.getLift().posY + dist));
                if (dir.equalsIgnoreCase("z")) this.tile.getLift().setDestZ((float) (this.tile.getLift().posZ + dist));
                break;
            case 1:
                if (arguments.length != 1) throw new LuaException("Arguments: [floor]");
                dist = ArgumentHelper.getInt(arguments, 0);
                if (dist < 0) dist = 64 - dist;
                if (dist - 1 >= this.tile.getLift().maxFloors()) throw new LuaException("Floor not in range");
                if (!this.tile.getLift().hasFloor(dist)) throw new LuaException("Floor not found.");
                this.tile.getLift().call(dist);
                break;
            case 2:
                return new Object[] { this.tile.getLift().posX, this.tile.getLift().posY, this.tile.getLift().posZ };
            case 3:
                return new Object[] { this.tile.liftID != null ? true : false };
            }

            return null;
        }

        @Override
        public boolean equals(final IPeripheral other)
        {
            return other instanceof ElevatorPeripheral && ((ElevatorPeripheral) other).tile == this.tile;
        }

    }

    public static class ElevatorPeripheralProvider implements IPeripheralProvider
    {
        @Override
        public IPeripheral getPeripheral(final World world, final BlockPos pos, final Direction side)
        {
            final TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof ControllerTile) return new ElevatorPeripheral((ControllerTile) tile);
            return null;
        }
    }

}
