package com.vonbraunz.botanypots.blocks;

import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.vonbraunz.botanypots.tileentity.TileEntityHopperBotanyPot;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockHopperBotanyPot extends BlockBotanyPot {

    // Block bounds: 12/16 wide, 8/16 tall
    private static final float HO = 2f / 16f;
    private static final float HO2 = 14f / 16f;
    private static final float HH = 8f / 16f;

    public BlockHopperBotanyPot() {
        setBlockName("hopper_botany_pot");
        setBlockBounds(HO, 0, HO, HO2, HH, HO2);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        IIcon clay = Blocks.hardened_clay.getIcon(side, meta);
        return clay != null ? clay : super.getIcon(side, meta);
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(HO, 0, HO, HO2, HH, HO2);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setBlockBounds(HO, 0, HO, HO2, HH, HO2);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityHopperBotanyPot();
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityHopperBotanyPot();
    }
}
