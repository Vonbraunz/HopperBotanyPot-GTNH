package com.vonbraunz.botanypots.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class BotanyPotsConfig {
    public static int defaultGrowthTicks = 6000;        // 5 minutes
    public static int defaultSaplingGrowthTicks = 24000; // 20 minutes

    public static void init(File file) {
        Configuration cfg = new Configuration(file);
        cfg.load();
        defaultGrowthTicks = cfg.getInt(
            "defaultGrowthTicks", "general", 6000, 20, Integer.MAX_VALUE,
            "Base growth time in ticks for OreDict crops (20 ticks = 1 second)"
        );
        defaultSaplingGrowthTicks = cfg.getInt(
            "defaultSaplingGrowthTicks", "general", 24000, 20, Integer.MAX_VALUE,
            "Base growth time in ticks for saplings"
        );
        if (cfg.hasChanged()) cfg.save();
    }
}
