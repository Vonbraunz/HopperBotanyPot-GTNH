package com.vonbraunz.botanypots.blocks;

import com.vonbraunz.botanypots.client.RenderBotanyPot;
import com.vonbraunz.botanypots.tileentity.TileEntityBotanyPot;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBotanyPot extends BlockContainer {

    // Render bounds — slightly smaller than a full block, half height
    static final float MIN_XZ = 4f / 16f;
    static final float MAX_XZ = 12f / 16f;
    static final float MAX_Y  = 8f / 16f;

    public BlockBotanyPot() {
        super(Material.clay);
        setBlockName("botany_pot");
        setHardness(0.6f);
        setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(MIN_XZ, 0, MIN_XZ, MAX_XZ, MAX_Y, MAX_XZ);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister reg) {
        // Reuse vanilla flower_pot texture — already in the block atlas
        blockIcon = reg.registerIcon("minecraft:flower_pot");
    }

    @Override
    public int getRenderType() {
        return RenderBotanyPot.RENDER_ID;
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(MIN_XZ, 0, MIN_XZ, MAX_XZ, MAX_Y, MAX_XZ);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setBlockBounds(MIN_XZ, 0, MIN_XZ, MAX_XZ, MAX_Y, MAX_XZ);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityBotanyPot();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;

        TileEntityBotanyPot te = (TileEntityBotanyPot) world.getTileEntity(x, y, z);
        if (te == null) return false;

        ItemStack held = player.getHeldItem();
        if (held == null) {
            return te.onInteractEmpty(player);
        }
        return te.onInteractWithItem(player, held);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntityBotanyPot te = (TileEntityBotanyPot) world.getTileEntity(x, y, z);
        if (te != null) {
            for (int i = 0; i < te.getSizeInventory(); i++) {
                ItemStack stack = te.getStackInSlot(i);
                if (stack != null) {
                    EntityItem ei = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack.copy());
                    world.spawnEntityInWorld(ei);
                }
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }
}
