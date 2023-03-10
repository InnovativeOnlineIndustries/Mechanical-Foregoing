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
package com.buuz135.industrial.block.transportstorage.transporter;

import com.buuz135.industrial.api.IBlockContainer;
import com.buuz135.industrial.api.transporter.FilteredTransporterType;
import com.buuz135.industrial.api.transporter.TransporterType;
import com.buuz135.industrial.api.transporter.TransporterTypeFactory;
import com.buuz135.industrial.block.transportstorage.tile.TransporterTile;
import com.buuz135.industrial.proxy.block.filter.IFilter;
import com.buuz135.industrial.proxy.block.filter.RegulatorFilter;
import com.buuz135.industrial.proxy.client.render.TransporterTESR;
import com.buuz135.industrial.utils.FabricUtils;
import com.buuz135.industrial.utils.IndustrialTags;
import com.buuz135.industrial.utils.Reference;
import com.buuz135.industrial.utils.TransferUtil2;
import com.google.common.collect.Sets;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.util.TileUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.NBTSerializer;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import me.alphamode.forgetags.Tags;;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class TransporterItemType extends FilteredTransporterType<ResourceAmount<ItemVariant>, Storage<ItemVariant>> {

    public static final int QUEUE_SIZE = 6;

    private HashMap<Direction, List<ItemStack>> queue;
    private StorageView<ItemVariant> extractSlot;

    public TransporterItemType(IBlockContainer container, TransporterTypeFactory factory, Direction side, TransporterTypeFactory.TransporterAction action) {
        super(container, factory, side, action);
        this.queue = new HashMap<>();
        this.extractSlot = null;
        for (Direction value : Direction.values()) {
            while (this.queue.computeIfAbsent(value, direction -> new ArrayList<>()).size() < QUEUE_SIZE) {
                this.queue.get(value).add(0, ItemStack.EMPTY);
            }
            if (this.queue.size() > QUEUE_SIZE) {
                this.queue.get(value).remove(this.queue.get(value).size() - 1);
            }
        }
    }

    @Override
    public RegulatorFilter<ResourceAmount<ItemVariant>, Storage<ItemVariant>> createFilter() {
        return new RegulatorFilter<ResourceAmount<ItemVariant>, Storage<ItemVariant>>(20, 20, 5, 3, 16, 64, 1024 * 8, "") {
            @Override
            public long matches(ResourceAmount<ItemVariant> stack, Storage<ItemVariant> itemHandler, boolean isRegulated) {
                if (isEmpty()) return stack.amount();
                long amount = 0;
                if (isRegulated) {
                    for (StorageView<ItemVariant> view : itemHandler) {
                        if (view.getResource().toStack().sameItem(stack.resource().toStack())) {
                            amount += view.getAmount();
                        }
                    }
                }

                for (IFilter.GhostSlot slot : this.getFilter()) {
                    if (stack.resource().toStack().sameItem(slot.getStack())) {
                        long maxAmount = isRegulated ? slot.getAmount() : Long.MAX_VALUE;
                        long returnAmount = Math.min(stack.amount(), maxAmount - amount);
                        if (returnAmount > 0) return returnAmount;
                    }
                }
                return 0;
            }
        };
    }

    @Override
    public void update() {
        super.update();
        float speed = getSpeed();
        if (!getLevel().isClientSide && getLevel().getGameTime() % (Math.max(1, 4 - speed)) == 0) {
            IBlockContainer container = getContainer();
            if (getAction() == TransporterTypeFactory.TransporterAction.EXTRACT && container instanceof TransporterTile) {
                for (Direction direction : ((TransporterTile) container).getTransporterTypeMap().keySet()) {
                    TransporterType transporterType = ((TransporterTile) container).getTransporterTypeMap().get(direction);
                    if (transporterType instanceof TransporterItemType && transporterType.getAction() == TransporterTypeFactory.TransporterAction.INSERT) {
                        FabricUtils.getStorage(ItemStorage.SIDED, getLevel(), getPos().relative(this.getSide()), getSide().getOpposite()).ifPresent(origin -> {
                            FabricUtils.getStorage(ItemStorage.SIDED, getLevel(), getPos().relative(direction), direction.getOpposite()).ifPresent(destination -> {
                                if (extractSlot.isResourceBlank()
                                        || !filter(this.getFilter(), this.isWhitelist(), new ResourceAmount<>(extractSlot.getResource(), extractSlot.getAmount()), origin, false)
                                        || !filter(((TransporterItemType) transporterType).getFilter(), ((TransporterItemType) transporterType).isWhitelist(), new ResourceAmount<>(extractSlot.getResource(), extractSlot.getAmount()), destination, ((TransporterItemType) transporterType).isRegulated()))
                                    findSlot(origin, ((TransporterItemType) transporterType).getFilter(), ((TransporterItemType) transporterType).isWhitelist(), destination, ((TransporterItemType) transporterType).isRegulated());
                                if (!extractSlot.isResourceBlank()) {
                                    int amount = (int) (1 * getEfficiency());
                                    ItemStack extracted = FabricUtils.extractItemView(extractSlot, amount, true);
                                    var resource = new ResourceAmount<>(ItemVariant.of(extracted), extracted.getCount());
                                    long simulatedAmount = ((TransporterItemType) transporterType).getFilter().matches(resource, destination, ((TransporterItemType) transporterType).isRegulated());
                                    if (!extracted.isEmpty() && filter(this.getFilter(), this.isWhitelist(), resource, origin, false) && filter(((TransporterItemType) transporterType).getFilter(), ((TransporterItemType) transporterType).isWhitelist(), new ResourceAmount<>(extractSlot.getResource(), extractSlot.getAmount()), destination, ((TransporterItemType) transporterType).isRegulated()) && simulatedAmount > 0) {
                                        ItemStack returned = TransferUtil2.insertItem(destination, extracted, true);
                                        if (returned.isEmpty() || amount - returned.getCount() > 0) {
                                            extracted = FabricUtils.extractItemView(extractSlot, returned.isEmpty() ? simulatedAmount : simulatedAmount - returned.getCount(), false);
                                            TransferUtil2.insertItem(destination, extracted, false);
                                            ((TransporterItemType) transporterType).addTransferedStack(getSide(), extracted);
                                        } else {
                                            for (StorageView<ItemVariant> view : origin) {
                                                if (view.getUnderlyingView() != extractSlot.getUnderlyingView()) {
                                                    this.extractSlot = view;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                container.requestSync();
                            });
                        });
                    }
                }
            }
        }
    }

    private boolean filter(RegulatorFilter<ResourceAmount<ItemVariant>, Storage<ItemVariant>> filter, boolean whitelist, ResourceAmount<ItemVariant> stack, Storage<ItemVariant> handler, boolean isRegulated) {
        long accepts = filter.matches(stack, handler, isRegulated);
        if (whitelist && filter.isEmpty()) {
            return false;
        }
        return filter.isEmpty() != (whitelist == (accepts > 0));
    }

    @Override
    public void updateClient() {
        super.updateClient();
        for (Direction value : Direction.values()) {
            while (this.queue.computeIfAbsent(value, direction -> new ArrayList<>()).size() < QUEUE_SIZE) {
                this.queue.get(value).add(0, ItemStack.EMPTY);
            }
            this.queue.get(value).add(0, ItemStack.EMPTY);
            while (this.queue.get(value).size() > QUEUE_SIZE) {
                this.queue.get(value).remove(this.queue.get(value).size() - 1);
            }
        }
    }

    private void findSlot(Storage<ItemVariant> itemHandler, RegulatorFilter<ResourceAmount<ItemVariant>, Storage<ItemVariant>> otherFilter, boolean otherWhitelist, Storage<ItemVariant> otherItemHandler, boolean otherRegulated) {
        for (StorageView<ItemVariant> view : itemHandler) {
            if (view.getUnderlyingView() == extractSlot.getUnderlyingView())
                continue;
            if (!view.isResourceBlank() && filter(this.getFilter(), this.isWhitelist(), new ResourceAmount<>(view.getResource(), view.getAmount()), itemHandler, false) && filter(otherFilter, otherWhitelist, new ResourceAmount<>(view.getResource(), view.getAmount()), otherItemHandler, otherRegulated)) {
                this.extractSlot = view;
                return;
            }
        }
        this.extractSlot = null;
    }

    public void addTransferedStack(Direction direction, ItemStack stack) {
        syncRender(direction, NBTSerializer.serializeNBTCompound(stack));
    }

    @Override
    public void handleRenderSync(Direction origin, CompoundTag compoundNBT) {
        this.queue.computeIfAbsent(origin, direction -> new ArrayList<>()).add(0, ItemStack.of(compoundNBT));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void renderTransfer(Vector3f pos, Direction direction, int step, PoseStack stack, int combinedOverlayIn, MultiBufferSource buffer, float frame) {
        super.renderTransfer(pos, direction, step, stack, combinedOverlayIn, buffer, frame);
        if (step < queue.computeIfAbsent(direction, v -> new ArrayList<>()).size()) {
            float scale = 0.10f;
            stack.scale(scale, scale, scale);
            ItemStack itemStack = queue.get(direction).get(step);
            if (!itemStack.isEmpty()) {
                Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, ItemTransforms.TransformType.NONE, 0xF000F0, combinedOverlayIn, stack, buffer, 0);
            } else {
                stack.pushPose();
                stack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
                stack.mulPose(Vector3f.ZP.rotationDegrees(90f));
                stack.mulPose(Vector3f.XP.rotationDegrees(90f));
                VertexConsumer buffer1 = buffer.getBuffer(TransporterTESR.TYPE);

                float pX1 = 1;
                float u = 1;
                float pX2 = 0;
                float u2 = 0;
                Color CLOSE = Color.CYAN;
                Color FAR = new Color(0x6800FF);
                double ratio = (step + 2.5) / (double) QUEUE_SIZE;
                float xOffset = -0.75f;
                float yOffset = -0f;
                float zOffset = -0.75f;
                int alpha = 1;
                stack.scale(0.25f, 0.25f, 0.25f);
                float red = (int) Math.abs((ratio * FAR.getRed()) + ((1 - ratio) * CLOSE.getRed())) / 256F;
                float green = (int) Math.abs((ratio * FAR.getGreen()) + ((1 - ratio) * CLOSE.getGreen())) / 256F;
                float blue = (int) Math.abs((ratio * FAR.getBlue()) + ((1 - ratio) * CLOSE.getBlue())) / 256F;
                Matrix4f matrix = stack.last().pose();
                buffer1.vertex(matrix, pX2 + xOffset, yOffset, 0 + zOffset).color(red, green, blue, alpha).uv(u2, 0).endVertex();
                buffer1.vertex(matrix, pX1 + xOffset + 0.5f, yOffset, 0 + zOffset).color(red, green, blue, alpha).uv(u, 0).endVertex();
                buffer1.vertex(matrix, pX1 + xOffset + 0.5f, yOffset, 1.5f + zOffset).color(red, green, blue, alpha).uv(u, 1).endVertex();
                buffer1.vertex(matrix, pX2 + xOffset, yOffset, 1.5f + zOffset).color(red, green, blue, alpha).uv(u2, 1).endVertex();
                stack.popPose();
            }
        }
    }

    public static class Factory extends TransporterTypeFactory {

        public Factory() {
            super("item");
        }

        @Override
        public TransporterType create(IBlockContainer container, Direction face, TransporterAction action) {
            return new TransporterItemType(container, this, face, action);
        }

        @Override
        @Nonnull
        public ResourceLocation getModel(Direction upgradeSide, TransporterAction action) {
            return new ResourceLocation(Reference.MOD_ID, "block/transporters/item_transporter_" + action.name().toLowerCase() + "_" + upgradeSide.getSerializedName().toLowerCase());
        }

        @Override
        public Set<ResourceLocation> getTextures() {
            return Sets.newHashSet(new ResourceLocation("industrialforegoing:blocks/transporters/item"), new ResourceLocation("industrialforegoing:blocks/base/bottom"));
        }

        @Override
        public boolean canBeAttachedAgainst(Level world, BlockPos pos, Direction face) {
            return TransferUtil.getItemStorage(world, pos, face) != null;
        }

        @Nonnull
        @Override
        public ResourceLocation getItemModel() {
            return new ResourceLocation(Reference.MOD_ID, "block/transporters/item_transporter_" + TransporterAction.EXTRACT.name().toLowerCase() + "_" + Direction.NORTH.getSerializedName().toLowerCase());
        }

        @Override
        public void registerRecipe(Consumer<FinishedRecipe> consumer) {
            TitaniumShapedRecipeBuilder.shapedRecipe(getUpgradeItem(), 2)
                    .pattern("IPI").pattern("GMG").pattern("ICI")
                    .define('I', Tags.Items.DUSTS_REDSTONE)
                    .define('P', Items.ENDER_PEARL)
                    .define('G', Tags.Items.INGOTS_GOLD)
                    .define('M', IndustrialTags.Items.MACHINE_FRAME_PITY)
                    .define('C', Items.PISTON)
                    .save(consumer);
        }
    }
}
