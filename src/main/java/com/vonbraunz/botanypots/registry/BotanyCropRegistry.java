package com.vonbraunz.botanypots.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.vonbraunz.botanypots.config.BotanyPotsConfig;

/**
 * Registry for non-CropsNH crops. Populated automatically from OreDict
 * (seedX -> cropX pairs) in postInit, and can also be populated manually
 * or via config.
 */
public class BotanyCropRegistry {

    private static final Random rand = new Random();

    public static class OutputEntry {

        public final ItemStack stack;
        public final float chance; // 1.0 = always, 0.5 = 50% chance per harvest

        public OutputEntry(ItemStack stack, float chance) {
            this.stack = stack.copy();
            this.chance = chance;
        }
    }

    public static class CropData {

        public final List<OutputEntry> outputs;
        public final int baseGrowthTicks;

        public CropData(List<OutputEntry> outputs, int baseGrowthTicks) {
            this.outputs = outputs;
            this.baseGrowthTicks = baseGrowthTicks;
        }
    }

    private static final Map<String, CropData> crops = new HashMap<>();

    /** Single always-drop convenience overload — used by OreDict scanning. */
    public static void register(ItemStack seed, ItemStack output, int baseGrowthTicks) {
        register(seed, Collections.singletonList(new OutputEntry(output, 1.0f)), baseGrowthTicks);
    }

    public static void register(ItemStack seed, List<OutputEntry> outputs, int baseGrowthTicks) {
        if (seed == null || outputs == null || outputs.isEmpty()) return;
        crops.put(key(seed), new CropData(outputs, baseGrowthTicks));
    }

    public static boolean isKnownSeed(ItemStack stack) {
        return stack != null && crops.containsKey(key(stack));
    }

    public static int getRegisteredSeedCount() {
        return crops.size();
    }

    public static int getGrowthTicks(ItemStack seed, float soilMultiplier) {
        CropData data = crops.get(key(seed));
        int base = (data != null) ? data.baseGrowthTicks : BotanyPotsConfig.defaultGrowthTicks;
        return Math.max(20, (int) (base / soilMultiplier));
    }

    public static List<ItemStack> getDrops(ItemStack seed) {
        CropData data = crops.get(key(seed));
        if (data == null) return Collections.emptyList();

        List<ItemStack> result = new ArrayList<>();
        for (OutputEntry entry : data.outputs) {
            if (rand.nextFloat() < entry.chance) {
                result.add(entry.stack.copy());
            }
        }
        return result;
    }

    /**
     * Hardcoded vanilla sapling mappings with realistic drop tables.
     * Call during init (before postInit OreDict scan).
     */
    public static void registerDefaultSaplings() {
        int t = BotanyPotsConfig.defaultSaplingGrowthTicks;

        // Oak: always log, 50% sapling, 10% apple
        register(
            new ItemStack(Blocks.sapling, 1, 0),
            Arrays.asList(
                new OutputEntry(new ItemStack(Blocks.log, 1, 0), 1.0f),
                new OutputEntry(new ItemStack(Blocks.sapling, 1, 0), 0.5f),
                new OutputEntry(new ItemStack(Items.apple, 1, 0), 0.1f)),
            t);

        // Spruce: always log, 50% sapling
        register(
            new ItemStack(Blocks.sapling, 1, 1),
            Arrays.asList(
                new OutputEntry(new ItemStack(Blocks.log, 1, 1), 1.0f),
                new OutputEntry(new ItemStack(Blocks.sapling, 1, 1), 0.5f)),
            t);

        // Birch: always log, 50% sapling
        register(
            new ItemStack(Blocks.sapling, 1, 2),
            Arrays.asList(
                new OutputEntry(new ItemStack(Blocks.log, 1, 2), 1.0f),
                new OutputEntry(new ItemStack(Blocks.sapling, 1, 2), 0.5f)),
            t);

        // Jungle: always log, 50% sapling
        register(
            new ItemStack(Blocks.sapling, 1, 3),
            Arrays.asList(
                new OutputEntry(new ItemStack(Blocks.log, 1, 3), 1.0f),
                new OutputEntry(new ItemStack(Blocks.sapling, 1, 3), 0.5f)),
            t);

        // Acacia: always log2:0, 50% sapling
        register(
            new ItemStack(Blocks.sapling, 1, 4),
            Arrays.asList(
                new OutputEntry(new ItemStack(Blocks.log2, 1, 0), 1.0f),
                new OutputEntry(new ItemStack(Blocks.sapling, 1, 4), 0.5f)),
            t);

        // Dark oak: always log2:1, 50% sapling
        register(
            new ItemStack(Blocks.sapling, 1, 5),
            Arrays.asList(
                new OutputEntry(new ItemStack(Blocks.log2, 1, 1), 1.0f),
                new OutputEntry(new ItemStack(Blocks.sapling, 1, 5), 0.5f)),
            t);
    }

    /**
     * Hardcoded vanilla crop mappings — covers seeds that may not have OreDict crop entries.
     */
    public static void registerDefaultCrops() {
        int t = BotanyPotsConfig.defaultGrowthTicks;

        // Wheat: seed → wheat + 50% seed back
        register(
            new ItemStack(Items.wheat_seeds),
            Arrays.asList(
                new OutputEntry(new ItemStack(Items.wheat), 1.0f),
                new OutputEntry(new ItemStack(Items.wheat_seeds), 0.5f)),
            t);

        // Carrot: carrot is both seed and crop
        register(
            new ItemStack(Items.carrot),
            Arrays.asList(
                new OutputEntry(new ItemStack(Items.carrot), 1.0f),
                new OutputEntry(new ItemStack(Items.carrot), 0.5f)),
            t);

        // Potato
        register(
            new ItemStack(Items.potato),
            Arrays.asList(
                new OutputEntry(new ItemStack(Items.potato), 1.0f),
                new OutputEntry(new ItemStack(Items.potato), 0.5f)),
            t);

        // Melon seeds → melon slice
        register(
            new ItemStack(Items.melon_seeds),
            Arrays.asList(
                new OutputEntry(new ItemStack(Items.melon), 1.0f),
                new OutputEntry(new ItemStack(Items.melon_seeds), 0.5f)),
            t);

        // Pumpkin seeds → pumpkin
        register(
            new ItemStack(Items.pumpkin_seeds),
            Arrays.asList(
                new OutputEntry(new ItemStack(Blocks.pumpkin), 1.0f),
                new OutputEntry(new ItemStack(Items.pumpkin_seeds), 0.5f)),
            t);

        // Nether wart
        register(
            new ItemStack(Items.nether_wart),
            Arrays.asList(
                new OutputEntry(new ItemStack(Items.nether_wart), 1.0f),
                new OutputEntry(new ItemStack(Items.nether_wart), 0.5f)),
            t);

    }

    /**
     * Scans the OreDict for seedX/cropX and treeSaplingX/logX pairs.
     * Call this in postInit so all mods have had a chance to register.
     */
    public static void registerOreDictCrops() {
        // Build a reverse map: item key -> list of OreDict names it appears in.
        // Used to resolve crop outputs for seeds registered only under "listAllSeed".
        Map<String, List<String>> itemToOreNames = new HashMap<>();
        for (String oreName : OreDictionary.getOreNames()) {
            for (ItemStack stack : OreDictionary.getOres(oreName)) {
                itemToOreNames.computeIfAbsent(key(stack), k -> new ArrayList<>()).add(oreName);
            }
        }

        for (String oreName : OreDictionary.getOreNames()) {
            if (oreName.startsWith("seed")) {
                String cropName = "crop" + oreName.substring(4);
                List<ItemStack> seedItems = OreDictionary.getOres(oreName);
                if (seedItems.isEmpty()) continue;

                List<ItemStack> cropItems = OreDictionary.getOres(cropName);
                ItemStack output = cropItems.isEmpty() ? null : cropItems.get(0);
                for (ItemStack seed : seedItems) {
                    if (isKnownSeed(seed)) continue;
                    if (output != null) {
                        register(seed, output, BotanyPotsConfig.defaultGrowthTicks);
                    } else {
                        register(seed, Collections.emptyList(), BotanyPotsConfig.defaultGrowthTicks);
                    }
                }

            } else if (oreName.startsWith("treeSapling")) {
                String logName = "log" + oreName.substring("treeSapling".length());
                List<ItemStack> saplings = OreDictionary.getOres(oreName);
                List<ItemStack> logs = OreDictionary.getOres(logName);
                if (saplings.isEmpty() || logs.isEmpty()) continue;

                ItemStack log = logs.get(0);
                for (ItemStack sapling : saplings) {
                    if (isKnownSeed(sapling)) continue;
                    register(
                        sapling,
                        Arrays.asList(new OutputEntry(log, 1.0f), new OutputEntry(sapling, 0.5f)),
                        BotanyPotsConfig.defaultSaplingGrowthTicks);
                }
            }
        }

        // Pam's HarvestCraft (and similar mods) register seeds under "listAllSeed"
        // without individual seedX/cropX entries. For each unknown seed in that list,
        // try to resolve a crop output by finding its seedX name and mapping to cropX.
        for (ItemStack seed : OreDictionary.getOres("listAllSeed")) {
            if (isKnownSeed(seed)) continue;
            ItemStack output = null;
            List<String> names = itemToOreNames.get(key(seed));
            if (names != null) {
                for (String name : names) {
                    if (name.startsWith("seed")) {
                        List<ItemStack> cropItems = OreDictionary.getOres("crop" + name.substring(4));
                        if (!cropItems.isEmpty()) {
                            output = cropItems.get(0);
                            break;
                        }
                    }
                }
            }
            if (output != null) {
                register(seed, output, BotanyPotsConfig.defaultGrowthTicks);
            } else {
                register(seed, Collections.emptyList(), BotanyPotsConfig.defaultGrowthTicks);
            }
        }
    }

    private static String key(ItemStack stack) {
        int id = Item.getIdFromItem(stack.getItem());
        int meta = stack.getItemDamage() == OreDictionary.WILDCARD_VALUE ? 0 : stack.getItemDamage();
        return id + ":" + meta;
    }
}
