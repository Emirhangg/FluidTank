package com.kotori316.fluidtank.integration.hwyla;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.integration.Localize;
import com.kotori316.fluidtank.tiles.TileTankNoDisplay;

/*
    Use java to avoid strange java.lang.VerifyError.
 */
@WailaPlugin
public class TankWailaPlugin implements IWailaPlugin {

    static final String Waila_ModId = "waila";
    static final String NBT_Tier = TileTankNoDisplay.NBT_Tier();
    static final String NBT_ConnectionAmount = "ConnectionAmount";
    static final String NBT_ConnectionCapacity = "ConnectionCapacity";
    static final String NBT_ConnectionComparator = "Comparator";
    static final String NBT_ConnectionFluidName = "FluidName";
    static final String NBT_NonCreative = "Normal";

    @Override
    public void register(IWailaRegistrar registrar) {
        if (Config.content().enableWailaAndTOP()) {
            TankDataProvider provider = new TankDataProvider();
            registrar.registerBodyProvider(provider, TileTankNoDisplay.class);
            registrar.registerNBTProvider(provider, TileTankNoDisplay.class);

            registrar.addConfig(FluidTank.MOD_NAME, Localize.WAILA_TANK_INFO, true);
            registrar.addConfig(FluidTank.MOD_NAME, Localize.WAILA_SHORT_INFO, true);
        }
    }
}
