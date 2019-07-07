package thut.tech.common.handlers;

import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;

public class ConfigHandler extends ConfigData
{
    @Configure
    public double LiftSpeedUp = 0.3;

    @Configure
    public double  LiftSpeedDown         = 0.35;
    @Configure
    public double  LiftSpeedDownOccupied = 0;
    @Configure
    public int     controllerProduction  = 16;
    @Configure
    public int     maxHeight             = 5;
    @Configure
    public int     maxRadius             = 2;
    @Configure
    public boolean hackyRender           = false;
    @Configure
    public boolean jitterfix             = true;
    @Configure
    public int     maxLiftEnergy         = 5000000;

    /**
     * @param MODID
     */
    public ConfigHandler(String MODID)
    {
        super(MODID);
    }

    @Override
    public void onUpdated()
    {
    }

}
