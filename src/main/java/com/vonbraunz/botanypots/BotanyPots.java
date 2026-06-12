package com.vonbraunz.botanypots;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.vonbraunz.botanypots.blocks.BlockHopperBotanyPot;
import com.vonbraunz.botanypots.compat.CropCompatManager;
import com.vonbraunz.botanypots.compat.WailaCompat;
import com.vonbraunz.botanypots.config.BotanyPotsConfig;
import com.vonbraunz.botanypots.proxy.CommonProxy;
import com.vonbraunz.botanypots.registry.BotanyCropRegistry;
import com.vonbraunz.botanypots.registry.SoilRegistry;
import com.vonbraunz.botanypots.tileentity.TileEntityHopperBotanyPot;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = BotanyPots.MODID, name = BotanyPots.NAME, version = BotanyPots.VERSION)
public class BotanyPots {

    public static final String MODID = "botanypots";
    public static final String NAME = "Botany Pots 1710";
    public static final String VERSION = "1.0.0";

    @SidedProxy(
        clientSide = "com.vonbraunz.botanypots.proxy.ClientProxy",
        serverSide = "com.vonbraunz.botanypots.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static Block blockHopperBotanyPot;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        BotanyPotsConfig.init(event.getSuggestedConfigurationFile());

        CropCompatManager.init();

        blockHopperBotanyPot = new BlockHopperBotanyPot();
        GameRegistry.registerBlock(blockHopperBotanyPot, "hopper_botany_pot");
        GameRegistry.registerTileEntity(TileEntityHopperBotanyPot.class, "TileEntityHopperBotanyPot");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        WailaCompat.init();
        SoilRegistry.registerDefaults();
        BotanyCropRegistry.registerDefaultSaplings();
        BotanyCropRegistry.registerDefaultCrops();

        GameRegistry.addRecipe(
            new ItemStack(blockHopperBotanyPot),
            "SHS",
            "HPH",
            " H ",
            'S',
            new ItemStack(Blocks.hardened_clay),
            'H',
            new ItemStack(Blocks.hopper),
            'P',
            new ItemStack(Items.flower_pot));

        proxy.registerRenderers();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Run after all mods have registered their OreDict entries
        BotanyCropRegistry.registerOreDictCrops();
    }
}
