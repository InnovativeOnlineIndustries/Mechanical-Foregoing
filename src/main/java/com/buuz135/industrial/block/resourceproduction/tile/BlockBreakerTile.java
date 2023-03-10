/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.buuz135.industrial.block.resourceproduction.tile;

import com.buuz135.industrial.IndustrialForegoing;
import com.buuz135.industrial.block.tile.IndustrialAreaWorkingTile;
import com.buuz135.industrial.block.tile.RangeManager;
import com.buuz135.industrial.config.machine.resourceproduction.BlockBreakerConfig;
import com.buuz135.industrial.module.ModuleResourceProduction;
import com.buuz135.industrial.utils.BlockUtils;
import com.buuz135.industrial.utils.TransferUtil2;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.component.energy.EnergyStorageComponent;
import com.hrznstudio.titanium.component.inventory.SidedInventoryComponent;
import io.github.fabricators_of_create.porting_lib.block.HarvestableBlock;
import io.github.fabricators_of_create.porting_lib.fake_players.FakePlayer;
import io.github.fabricators_of_create.porting_lib.util.PortingHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class BlockBreakerTile extends IndustrialAreaWorkingTile<BlockBreakerTile> {

    private int getMaxProgress;
    private int getPowerPerOperation;

    @Save
    private SidedInventoryComponent<BlockBreakerTile> output;

    public BlockBreakerTile(BlockPos blockPos, BlockState blockState) {
        super(ModuleResourceProduction.BLOCK_BREAKER, RangeManager.RangeType.BEHIND, false, BlockBreakerConfig.powerPerOperation, blockPos, blockState);
        this.addInventory(this.output = (SidedInventoryComponent<BlockBreakerTile>) new SidedInventoryComponent<BlockBreakerTile>("output", 54, 22, 3 * 6, 0).
                setColor(DyeColor.ORANGE).
                setRange(6, 3));
        this.getMaxProgress = BlockBreakerConfig.maxProgress;
        this.getPowerPerOperation = BlockBreakerConfig.powerPerOperation;
    }

    protected boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        if (state.getBlock() instanceof HarvestableBlock harvestableBlock)
            return harvestableBlock.canHarvestBlock(state, level, pos, player);
        return PortingHooks.isCorrectToolForDrops(state, player);
    }

    @Override
    public WorkAction work() {
        if (hasEnergy(getPowerPerOperation)) {
            BlockPos pointed = getPointedBlockPos();
            if (isLoaded(pointed) && !level.isEmptyBlock(pointed) && BlockUtils.canBlockBeBroken(this.level, pointed)) {
                FakePlayer fakePlayer = IndustrialForegoing.getFakePlayer(this.level, pointed);
                fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.NETHERITE_PICKAXE));
                if (this.level.getBlockState(pointed).getDestroySpeed(this.level, pointed) >= 0 && canHarvestBlock(this.level.getBlockState(pointed), this.level, pointed, fakePlayer)) {
                    for (ItemStack blockDrop : BlockUtils.getBlockDrops(this.level, pointed)) {
                        ItemStack result = TransferUtil2.insertItem(output, blockDrop, false);
                        if (!result.isEmpty()) {
                            BlockUtils.spawnItemStack(result, this.level, pointed);
                        }
                    }
                    this.level.setBlockAndUpdate(pointed, Blocks.AIR.defaultBlockState());
                    increasePointer();
                    return new WorkAction(1, getPowerPerOperation);
                }
            } else {
                increasePointer();
            }
        }
        return new WorkAction(1, 0);
    }

    @Override
    protected EnergyStorageComponent<BlockBreakerTile> createEnergyStorage() {
        return new EnergyStorageComponent<>(BlockBreakerConfig.maxStoredPower, 10, 20);
    }

    @Override
    public int getMaxProgress() {
        return getMaxProgress;
    }

    @Nonnull
    @Override
    public BlockBreakerTile getSelf() {
        return this;
    }
}
