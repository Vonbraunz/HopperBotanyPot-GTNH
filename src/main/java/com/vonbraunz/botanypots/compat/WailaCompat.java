package com.vonbraunz.botanypots.compat;

import com.vonbraunz.botanypots.tileentity.TileEntityBotanyPot;
import cpw.mods.fml.common.event.FMLInterModComms;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

public class WailaCompat implements IWailaDataProvider {

    private static final WailaCompat INSTANCE = new WailaCompat();

    public static void init() {
        FMLInterModComms.sendMessage("Waila", "register",
            "com.vonbraunz.botanypots.compat.WailaCompat.register");
    }

    @SuppressWarnings("unused")
    public static void register(IWailaRegistrar registrar) {
        registrar.registerBodyProvider(INSTANCE, TileEntityBotanyPot.class);
        registrar.registerNBTProvider(INSTANCE, TileEntityBotanyPot.class);
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te,
                                     NBTTagCompound tag, World world, int x, int y, int z) {
        TileEntityBotanyPot pot = (TileEntityBotanyPot) te;

        ItemStack soil = pot.getStackInSlot(TileEntityBotanyPot.SLOT_SOIL);
        ItemStack seed = pot.getStackInSlot(TileEntityBotanyPot.SLOT_SEED);

        if (soil != null) {
            tag.setString("wailaSoil", soil.getDisplayName());
        }
        if (seed != null) {
            tag.setString("wailaCrop", CropsNHCompat.isCropsNHSeed(seed)
                ? CropsNHCompat.getCropDisplayName(seed)
                : seed.getDisplayName());
            tag.setInteger("wailaProgress", (int) (pot.getGrowthProgress() * 100));
        }

        return tag;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip,
                                     IWailaDataAccessor accessor, IWailaConfigHandler config) {
        NBTTagCompound tag = accessor.getNBTData();

        String soil = tag.hasKey("wailaSoil")
            ? tag.getString("wailaSoil")
            : EnumChatFormatting.DARK_GRAY + "Empty";
        tooltip.add(EnumChatFormatting.GRAY + "Soil: " + EnumChatFormatting.WHITE + soil);

        if (tag.hasKey("wailaCrop")) {
            tooltip.add(EnumChatFormatting.GRAY + "Crop: "
                + EnumChatFormatting.WHITE + tag.getString("wailaCrop"));
            tooltip.add(EnumChatFormatting.GRAY + "Growth: "
                + progressBar(tag.getInteger("wailaProgress")));
        } else {
            tooltip.add(EnumChatFormatting.GRAY + "Crop: "
                + EnumChatFormatting.DARK_GRAY + "Empty");
        }

        return tooltip;
    }

    private static String progressBar(int percent) {
        int filled = percent / 10;
        StringBuilder bar = new StringBuilder(EnumChatFormatting.GREEN.toString());
        for (int i = 0; i < 10; i++) {
            if (i == filled) bar.append(EnumChatFormatting.DARK_GRAY);
            bar.append('|');
        }
        bar.append(EnumChatFormatting.GRAY).append(' ').append(percent).append('%');
        return bar.toString();
    }

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) { return null; }
    @Override
    public List<String> getWailaHead(ItemStack stack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) { return tooltip; }
    @Override
    public List<String> getWailaTail(ItemStack stack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) { return tooltip; }
}
