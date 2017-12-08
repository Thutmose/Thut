package thut.core.common.compat.immersiveengineering;

import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Optional.Method;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.entity.blockentity.IBlockEntity.ITileRemover;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;

public class ImmersiveEngineeringCompat
{

    @Method(modid = "immersiveengineering")
    @CompatClass(phase = Phase.POST)
    public static void postInitIE()
    {
        try
        {
            ITileRemover remover = new ITileRemover()
            {
                @Override
                public void preBlockRemoval(TileEntity tileIn)
                {
                    ((TileEntityMultiblockPart<?>)tileIn).formed = false;
                    ((TileEntityMultiblockPart<?>)tileIn).master().formed = false;
                    tileIn.setWorld(null);
                }

                @Override
                public void postBlockRemoval(TileEntity tileIn)
                {
                }
            };
            IBlockEntity.addRemover(remover, TileEntityMultiblockPart.class);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
