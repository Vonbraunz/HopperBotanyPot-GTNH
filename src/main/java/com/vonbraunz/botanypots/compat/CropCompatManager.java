package com.vonbraunz.botanypots.compat;

import cpw.mods.fml.common.Loader;

/**
 * Loads CropsNHCompat via reflection so no class in the normal load path has a
 * bytecode reference to CropsNH types. LaunchClassLoader eagerly resolves all
 * types in a class's constant pool, so a direct import would crash when CropsNH
 * is absent.
 */
public class CropCompatManager {

    private static ICropsNHHandler handler = null;

    public static void init() {
        if (!Loader.isModLoaded("cropsnh")) return;
        try {
            handler = (ICropsNHHandler) Class.forName("com.vonbraunz.botanypots.compat.CropsNHCompat")
                .newInstance();
        } catch (Exception e) {
            handler = null;
        }
    }

    public static boolean hasCropsNH() {
        return handler != null;
    }

    public static ICropsNHHandler get() {
        return handler;
    }
}
