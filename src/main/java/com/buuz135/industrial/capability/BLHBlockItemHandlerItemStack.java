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
package com.buuz135.industrial.capability;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.NBTSerializer;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class BLHBlockItemHandlerItemStack implements SingleSlotStorage<ItemVariant> {

    public final ContainerItemContext context;
    public final long slotLimit;

    public BLHBlockItemHandlerItemStack(ContainerItemContext context, long slotLimit) {
        this.context = context;
        this.slotLimit = slotLimit;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (isItemValid(resource, maxAmount)) {
            long amount = slotLimit;
            long stored = getAmount();
            long inserted = Math.min(amount - stored, maxAmount);
            if (getVoid()) inserted = maxAmount;
            setStack(resource.toStack());
            setAmount(Math.min(stored + inserted, amount));
            if (inserted == maxAmount) return 0;
            return maxAmount - inserted;
        }
        return 0;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (maxAmount == 0) return 0;
        ItemStack blStack = getStack();
        long stored = getAmount();
        if (blStack.isEmpty()) return 0;
        if (stored <= maxAmount) {
            long newAmount = stored;
            setAmount(0);
            return newAmount;
        } else {
            setAmount(stored - maxAmount);
            return maxAmount;
        }
    }

    public boolean isItemValid(@Nonnull ItemVariant variant, long amount) {
        ItemStack current = getStack();
        return current.isEmpty() || (current.sameItem(variant.toStack()) && ItemStack.tagMatches(current, variant.toStack()));
    }

    @Override
    public long getAmount() {
        CompoundTag tag = getTag();
        if (tag != null && tag.contains("stored")) {
            return tag.getInt("stored");
        }
        return 0;
    }

    @Override
    public long getCapacity() {
        return slotLimit;
    }

    @Override
    public boolean isResourceBlank() {
        return getResource().isBlank();
    }

    @Override
    public ItemVariant getResource() {
        return ItemVariant.of(getStack());
    }

    public ItemStack getStack() {
        CompoundTag tag = getTag();
        if (tag != null && tag.contains("blStack")) {
            return ItemStack.of(tag.getCompound("blStack"));
        }
        return ItemStack.EMPTY;
    }

    public boolean getVoid() {
        CompoundTag tag = getTag();
        if (tag != null && tag.contains("voidItems")) {
            return tag.getBoolean("voidItems");
        }
        return true;
    }

    private void setAmount(long amount) {
        CompoundTag tag = getTag();
        ItemStack newStack = context.getItemVariant().toStack();
        if (tag == null) {
            CompoundTag compoundNBT = new CompoundTag();
            compoundNBT.put("BlockEntityTag", new CompoundTag());
            newStack.setTag(compoundNBT);
        }
        newStack.getTag().getCompound("BlockEntityTag").putLong("stored", amount);
        try (Transaction tx = TransferUtil.getTransaction()) {
            if (context.exchange(ItemVariant.of(newStack), 1, tx) == 1)
                tx.commit();
        }
    }

    private void setStack(ItemStack stack) {
        CompoundTag tag = getTag();
        ItemStack newStack = context.getItemVariant().toStack();
        if (tag == null) {
            CompoundTag compoundNBT = new CompoundTag();
            compoundNBT.put("BlockEntityTag", new CompoundTag());
            newStack.setTag(compoundNBT);
        }
        newStack.getTag().getCompound("BlockEntityTag").put("blStack", NBTSerializer.serializeNBT(stack));
        try (Transaction tx = TransferUtil.getTransaction()) {
            if (context.exchange(ItemVariant.of(newStack), 1, tx) == 1)
                tx.commit();
        }
    }

    private CompoundTag getTag() {
        if (context.getItemVariant().hasNbt() && context.getItemVariant().getNbt().contains("BlockEntityTag"))
            return context.getItemVariant().getNbt().getCompound("BlockEntityTag");
        return null;
    }
}
