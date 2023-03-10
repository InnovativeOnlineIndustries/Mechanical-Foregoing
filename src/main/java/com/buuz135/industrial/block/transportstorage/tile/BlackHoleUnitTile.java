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
package com.buuz135.industrial.block.transportstorage.tile;

import com.buuz135.industrial.gui.component.BigItemGuiAddon;
import com.buuz135.industrial.utils.BlockUtils;
import com.buuz135.industrial.utils.FabricUtils;
import com.buuz135.industrial.utils.NumberUtils;
import com.buuz135.industrial.utils.TransferUtil2;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.api.filter.FilterSlot;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.client.screen.addon.BasicButtonAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.component.button.ButtonComponent;
import com.hrznstudio.titanium.filter.ItemStackFilter;
import com.hrznstudio.titanium.util.AssetUtil;
import com.hrznstudio.titanium.util.LangUtil;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nonnull;
import java.util.*;

public class BlackHoleUnitTile extends BHTile<BlackHoleUnitTile> {

    private static HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();

    @Save
    private ItemStack blStack;
    @Save
    private long stored;
    @Save
    private ItemStackFilter filter;
    @Save
    private boolean voidItems;
    @Save
    private boolean useStackDisplay;
    @Save
    private boolean hasNBT;

    private BlackHoleHandler handler;

    public BlackHoleUnitTile(BasicTileBlock<BlackHoleUnitTile> basicTileBlock, BlockEntityType<?> type, Rarity rarity, BlockPos blockPos, BlockState blockState) {
        super(basicTileBlock, type, blockPos, blockState);
        this.blStack = ItemStack.EMPTY;
        this.stored = 0;
        this.voidItems = true;
        this.useStackDisplay = false;
        this.hasNBT = false;
        this.handler = new BlackHoleHandler(BlockUtils.getStackAmountByRarity(rarity));
        this.addFilter(filter = new ItemStackFilter("filter", 1));
        FilterSlot slot = new FilterSlot<>(79, 60, 0, ItemStack.EMPTY);
        slot.setColor(DyeColor.CYAN);
        this.filter.setFilter(0, slot);

        addButton(new ButtonComponent(82 + 20 * 2, 64 + 16, 18, 18) {
            @Override
            @Environment(EnvType.CLIENT)
            public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
                return Collections.singletonList(() -> new BasicButtonAddon(this) {
                    @Override
                    public void drawBackgroundLayer(PoseStack stack, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
                        AssetUtil.drawAsset(stack, screen, provider.getAsset(AssetTypes.ITEM_BACKGROUND), guiX + getPosX(), guiY + getPosY());
                        Minecraft.getInstance().getItemRenderer().renderGuiItem(new ItemStack(voidItems ? Items.MAGMA_CREAM : Items.SLIME_BALL), guiX + getPosX() + 1, guiY + getPosY() + 1);
//                        Lighting.turnOff();
//                        RenderSystem.enableAlphaTest();
                    }

                    @Override
                    public List<Component> getTooltipLines() {
                        List<Component> lines = new ArrayList<>();
                        lines.add(Component.literal(ChatFormatting.GOLD + LangUtil.getString("tooltip.industrialforegoing.bl." + (voidItems ? "void_unit" : "no_void_unit"))));
                        return lines;
                    }
                });
            }
        }.setPredicate((playerEntity, compoundNBT) -> {
            this.voidItems = !this.voidItems;
            this.syncObject(this.voidItems);
        }));
        addButton(new ButtonComponent(82 + 20, 64 + 16, 18, 18) {
            @Override
            @Environment(EnvType.CLIENT)
            public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
                return Collections.singletonList(() -> new BasicButtonAddon(this) {
                    @Override
                    public void drawBackgroundLayer(PoseStack stack, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
                        AssetUtil.drawAsset(stack, screen, provider.getAsset(AssetTypes.ITEM_BACKGROUND), guiX + getPosX(), guiY + getPosY());
                        Minecraft.getInstance().getItemRenderer().renderGuiItem(new ItemStack(useStackDisplay ? Items.IRON_BLOCK : Items.IRON_INGOT), guiX + getPosX() + 1, guiY + getPosY() + 1);
//                        Lighting.turnOff();
//                        RenderSystem.enableAlphaTest();
                    }

                    @Override
                    public List<Component> getTooltipLines() {
                        List<Component> lines = new ArrayList<>();
                        lines.add(Component.literal(ChatFormatting.GOLD + LangUtil.getString("tooltip.industrialforegoing.bl." + (useStackDisplay ? "stack_unit" : "compact_unit"))));
                        return lines;
                    }
                });
            }
        }.setPredicate((playerEntity, compoundNBT) -> {
            this.useStackDisplay = !useStackDisplay;
            this.syncObject(this.useStackDisplay);
        }));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void initClient() {
        super.initClient();
        this.addGuiAddonFactory(() -> new BigItemGuiAddon(79, 25) {
            @Override
            public ItemStack getItemStack() {
                return blStack;
            }

            @Override
            public long getAmount() {
                return stored;
            }

            @Override
            public String getAmountDisplay() {
                return getFormatedDisplayAmount();
            }
        });
    }

    @Nonnull
    @Override
    public BlackHoleUnitTile getSelf() {
        return this;
    }

    @Override
    public InteractionResult onActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ) {
        if (super.onActivated(playerIn, hand, facing, hitX, hitY, hitZ) == InteractionResult.SUCCESS) {
            return InteractionResult.SUCCESS;
        }
        if (playerIn.isShiftKeyDown()) {
            openGui(playerIn);
        } else if (facing.equals(this.getFacingDirection())) {
            ItemStack stack = playerIn.getItemInHand(hand);
            if (!stack.isEmpty() && handler.isItemValid(ItemVariant.of(stack))) {
                playerIn.setItemInHand(hand, FabricUtils.insertItem(handler, stack, false));
            } else if (System.currentTimeMillis() - INTERACTION_LOGGER.getOrDefault(playerIn.getUUID(), System.currentTimeMillis()) < 300) {
                for (ItemStack itemStack : playerIn.inventory.items) {
                    if (!itemStack.isEmpty() && FabricUtils.insertItem(handler, itemStack, true).isEmpty()) {
                        TransferUtil.insertItem(handler, itemStack.copy());
                        itemStack.setCount(0);
                    }
                }
            }
            INTERACTION_LOGGER.put(playerIn.getUUID(), System.currentTimeMillis());
        }
        return InteractionResult.SUCCESS;
    }

    public void onClicked(Player playerIn) {
        if (isServer()) {
            HitResult rayTraceResult = RayTraceUtils.rayTraceSimple(this.level, playerIn, 16, 0);
            if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockResult = (BlockHitResult) rayTraceResult;
                Direction facing = blockResult.getDirection();
                if (facing.equals(this.getFacingDirection())) {
                    try (Transaction tx = TransferUtil.getTransaction()) {
                        ItemVariant toExtract = StorageUtil.findExtractableResource(handler, tx);
                        long amount = handler.getAmount();
                        long extracted = handler.extract(toExtract, playerIn.isShiftKeyDown() ? 64 : 1, tx);
                        TransferUtil2.giveItemToPlayer(playerIn, toExtract, amount - extracted);
                    }
                }
            }
        }
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, BlackHoleUnitTile blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        if (isServer()) {
            if (!this.hasNBT && this.blStack.hasTag()) {
                ItemStack stack = this.blStack.copy();
                stack.setTag(null);
                this.setStack(stack);
            }
        }
    }

    public void setAmount(long amount) {
        boolean equal = amount == stored;
        this.stored = amount;
        if (!equal) syncObject(this.stored);
    }

    public void setStack(ItemStack stack) {
        boolean equal = blStack.sameItem(stack) && ItemStack.tagMatches(blStack, stack);
        this.blStack = stack;
        this.hasNBT = this.blStack.hasTag();
        if (!equal) syncObject(this.blStack);
    }

    @Override
    public Storage<ItemVariant> getItemStorage(Direction side) {
        return handler;
    }

    @Override
    public ItemStack getDisplayStack() {
        return blStack;
    }

    @Override
    public String getFormatedDisplayAmount() {
        if (this.useStackDisplay)
            return stored == 0 ? "0" : (stored >= 64 ? NumberUtils.getFormatedBigNumber(stored / 64) + " x64" : "") + (stored >= 64 && stored % 64 != 0 ? " + " : "") + (stored % 64 != 0 ? stored % 64 : "");
        return NumberUtils.getFormatedBigNumber(stored);
    }

    public boolean isVoidItems() {
        return voidItems;
    }

    private class BlackHoleHandler extends SnapshotParticipant<BlackHoleHandler.Snapshot> implements SingleSlotStorage<ItemVariant> {

        private long amount;

        public BlackHoleHandler(long amount) {
            this.amount = amount;
        }

        @Override
        public long getAmount() {
            return stored;
        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(blStack);
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext tx) {
            if (isItemValid(resource)) {
                long inserted = Math.min(this.amount - stored, maxAmount);
                updateSnapshots(tx);
                setStack(resource.toStack(Math.min(resource.getItem().getMaxStackSize(), (int) maxAmount)));
                setAmount(Math.min(stored + inserted, amount));
                if (voidItems) return 0;
                return inserted;
            }
            return 0;
        }

        @Override
        public long extract(ItemVariant resource, long amount, TransactionContext transaction) {
            if (amount == 0) return 0;
            if (blStack.isEmpty()) return 0;
            if (!resource.matches(blStack)) return 0;
            if (stored <= amount) {
                long newAmount = stored;
                updateSnapshots(transaction);
                setStack(ItemStack.EMPTY);
                setAmount(0);
                return newAmount;
            } else {
                updateSnapshots(transaction);
                setAmount(stored - amount);
                return amount;
            }
        }

        public boolean isItemValid(@Nonnull ItemVariant variant) {
            ItemStack stack = variant.toStack();
            ItemStack fl = blStack;
            if (!filter.getFilterSlots()[0].getFilter().isEmpty() && fl.isEmpty()) {
                fl = filter.getFilterSlots()[0].getFilter();
            }
            return fl.isEmpty() || (fl.sameItem(stack) && ItemStack.tagMatches(fl, stack));
        }

        @Override
        public long getCapacity() {
            return amount;
        }

        @Override
        protected Snapshot createSnapshot() {
            return new Snapshot(blStack.copy(), stored);
        }

        @Override
        protected void readSnapshot(Snapshot snapshot) {
            blStack = snapshot.stack();
            stored = snapshot.amount();
        }

        public record Snapshot(ItemStack stack, long amount) {}
    }
}
