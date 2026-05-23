package com.vonbraunz.botanypots.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class BotanyPotsConfig {

    public static int defaultGrowthTicks = 3600; // 3 minutes
    public static int defaultSaplingGrowthTicks = 1200; // 60 seconds

    public static void init(File file) {
        Configuration cfg = new Configuration(file);
        cfg.load();
        defaultGrowthTicks = cfg.getInt(
            "defaultGrowthTicks",
            "general",
            3600,
            20,
            Integer.MAX_VALUE,
            "Base growth time in ticks for OreDict crops (20 ticks = 1 second)");
        defaultSaplingGrowthTicks = cfg.getInt(
            "defaultSaplingGrowthTicks",
            "general",
            1200,
            20,
            Integer.MAX_VALUE,
            "Base growth time in ticks for saplings (20 ticks = 1 second)");
        if (cfg.hasChanged()) cfg.save();
    }
}
