package com.vonbraunz.botanypots.compat;

import com.gtnewhorizon.cropsnh.api.ICropCard;
import com.gtnewhorizon.cropsnh.api.ISeedStats;
import com.gtnewhorizon.cropsnh.farming.SeedStats;
import com.gtnewhorizon.cropsnh.farming.registries.CropRegistry;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CropsNHCompat {

    private static final Random rand = new Random();

    public static boolean isCropsNHSeed(ItemStack stack) {
        return stack != null && CropRegistry.instance.get(stack) != null;
    }

    public static int getGrowthTicks(ItemStack seed, float soilMultiplier) {
        ICropCard crop = CropRegistry.instance.get(seed);
        if (crop == null) return 6000;

        ISeedStats stats = SeedStats.getStatsFromStack(seed);
        int growthStat = (stats != null) ? Math.max(1, stats.getGrowth()) : 1;

        int ticks = crop.getGrowthDuration() / growthStat;
        return Math.max(20, (int) (ticks / soilMultiplier));
    }

    public static List<ItemStack> getDrops(ItemStack seed) {
        ICropCard crop = CropRegistry.instance.get(seed);
        if (crop == null) return new ArrayList<>();

        ISeedStats stats = SeedStats.getStatsFromStack(seed);
        int gainStat = (stats != null) ? Math.max(1, stats.getGain()) : 1;

        double dropMult = crop.getDropChance() * Math.pow(1.03D, gainStat);

        List<ItemStack> result = new ArrayList<>();
        Map<ItemStack, Integer> table = crop.getDropTable();
        if (table == null || table.isEmpty()) return result;

        for (Map.Entry<ItemStack, Integer> entry : table.entrySet()) {
            float chance = entry.getValue() / 10000.0f;

            double expectedCount = dropMult * chance;
            int count = (int) expectedCount;
            if (rand.nextDouble() < (expectedCount - count)) count++;
            if (rand.nextFloat() < gainStat * 0.01f) count++;

            if (count > 0) {
                ItemStack drop = entry.getKey().copy();
                drop.stackSize = count;
                result.add(drop);
            }
        }
        return result;
    }

    /** Returns "CropName (G:15 Ga:20 R:8)" for WAILA display. */
    public static String getCropDisplayName(ItemStack seed) {
        ICropCard crop = CropRegistry.instance.get(seed);
        String name = (crop != null) ? crop.getCropName() : seed.getDisplayName();

        ISeedStats stats = SeedStats.getStatsFromStack(seed);
        if (stats != null) {
            name += String.format(" (G:%d Ga:%d R:%d)",
                stats.getGrowth(), stats.getGain(), stats.getResistance());
        }
        return name;
    }
}
