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

package com.buuz135.industrial.module;

import com.buuz135.industrial.block.MachineFrameBlock;
import com.buuz135.industrial.block.core.DissolutionChamberBlock;
import com.buuz135.industrial.block.core.FluidExtractorBlock;
import com.buuz135.industrial.block.core.LatexProcessingUnitBlock;
import com.buuz135.industrial.block.core.tile.FluidExtractorTile;
import com.buuz135.industrial.fluid.OreFluidInstance;
import com.buuz135.industrial.fluid.OreTitaniumFluidType;
import com.buuz135.industrial.item.FertilizerItem;
import com.buuz135.industrial.item.ItemStraw;
import com.buuz135.industrial.item.LaserLensItem;
import com.buuz135.industrial.item.RecipelessCustomItem;
import com.buuz135.industrial.item.addon.EfficiencyAddonItem;
import com.buuz135.industrial.item.addon.ProcessingAddonItem;
import com.buuz135.industrial.item.addon.RangeAddonItem;
import com.buuz135.industrial.item.addon.SpeedAddonItem;
import com.buuz135.industrial.recipe.*;
import com.buuz135.industrial.registry.IFRegistries;
import com.buuz135.industrial.utils.Reference;
import com.buuz135.industrial.utils.SimpleRecipeType;
import com.buuz135.industrial.utils.apihandlers.straw.*;
import com.chocohead.mm.api.ClassTinkerers;
import com.hrznstudio.titanium.fluid.TitaniumAttributeHandler;
import com.hrznstudio.titanium.fluid.TitaniumFluidInstance;
import com.hrznstudio.titanium.module.DeferredRegistryHelper;
import com.hrznstudio.titanium.recipe.serializer.GenericSerializer;
import com.hrznstudio.titanium.tab.AdvancedTitaniumTab;
import io.github.fabricators_of_create.porting_lib.event.client.TextureStitchCallback;
import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Consumer;

public class ModuleCore implements IModule {

    public static Rarity PITY_RARITY;
    public static Rarity SIMPLE_RARITY;
    public static Rarity ADVANCED_RARITY;
    public static Rarity SUPREME_RARITY;

    public static AdvancedTitaniumTab TAB_CORE = new AdvancedTitaniumTab(Reference.MOD_ID + "_core", true);

    public static RegistryObject<Item> TINY_DRY_RUBBER;
    public static RegistryObject<Item> DRY_RUBBER;
    public static RegistryObject<Item> PLASTIC;
    public static RegistryObject<Item> FERTILIZER;
    public static RegistryObject<Item> PINK_SLIME_ITEM;
    public static RegistryObject<Item> PINK_SLIME_INGOT;
    public static RegistryObject<Item> STRAW;
    public static RegistryObject<Block> PITY;
    public static RegistryObject<Block> SIMPLE;
    public static RegistryObject<Block> ADVANCED;
    public static RegistryObject<Block> SUPREME;
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> FLUID_EXTRACTOR;
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> LATEX_PROCESSING;
    public static Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> DISSOLUTION_CHAMBER;
    public static RegistryObject<Item>[] RANGE_ADDONS = new RegistryObject[12];
    public static RegistryObject<Item>[] LASER_LENS = new RegistryObject[DyeColor.values().length];
    public static RegistryObject<Item> SPEED_ADDON_1;
    public static RegistryObject<Item> SPEED_ADDON_2;
    public static RegistryObject<Item> EFFICIENCY_ADDON_1;
    public static RegistryObject<Item> EFFICIENCY_ADDON_2;
    public static RegistryObject<Item> PROCESSING_ADDON_1;
    public static RegistryObject<Item> PROCESSING_ADDON_2;

    public static TitaniumFluidInstance LATEX;
    public static TitaniumFluidInstance MEAT;
    public static TitaniumFluidInstance SEWAGE;
    public static TitaniumFluidInstance ESSENCE;
    public static TitaniumFluidInstance SLUDGE;
    public static TitaniumFluidInstance PINK_SLIME;
    public static TitaniumFluidInstance BIOFUEL;
    public static TitaniumFluidInstance ETHER;
    public static OreFluidInstance RAW_ORE_MEAT;
    public static OreFluidInstance FERMENTED_ORE_MEAT;

    public static RegistryObject<Item> IRON_GEAR;
    public static RegistryObject<Item> GOLD_GEAR;
    public static RegistryObject<Item> DIAMOND_GEAR;

    public static RegistryObject<RecipeSerializer<?>> DISSOLUTION_SERIALIZER;
    public static RegistryObject<RecipeType<?>> DISSOLUTION_TYPE;
    public static RegistryObject<RecipeSerializer<?>> FLUID_EXTRACTOR_SERIALIZER;
    public static RegistryObject<RecipeType<?>> FLUID_EXTRACTOR_TYPE;
    public static RegistryObject<RecipeSerializer<?>> LASER_DRILL_SERIALIZER;
    public static RegistryObject<RecipeType<?>> LASER_DRILL_TYPE;
    public static RegistryObject<RecipeSerializer<?>> LASER_DRILL_FLUID_SERIALIZER;
    public static RegistryObject<RecipeType<?>> LASER_DRILL_FLUID_TYPE;
    public static RegistryObject<RecipeSerializer<?>> STONEWORK_GENERATE_SERIALIZER;
    public static RegistryObject<RecipeType<?>> STONEWORK_GENERATE_TYPE;
    public static RegistryObject<RecipeSerializer<?>> CRUSHER_SERIALIZER;
    public static RegistryObject<RecipeType<?>> CRUSHER_TYPE;


    @Override
    public void generateFeatures(DeferredRegistryHelper helper) {
        PITY_RARITY = ClassTinkerers.getEnum(Rarity.class, "pity");
        SIMPLE_RARITY = ClassTinkerers.getEnum(Rarity.class, "simple");
        ADVANCED_RARITY = ClassTinkerers.getEnum(Rarity.class, "advanced");
        SUPREME_RARITY = ClassTinkerers.getEnum(Rarity.class, "supreme");
        PITY = helper.registerBlockWithItem("machine_frame_pity", () -> new MachineFrameBlock(PITY_RARITY, TAB_CORE), (block) -> () -> new MachineFrameBlock.MachineFrameItem(block.get(), PITY_RARITY, TAB_CORE));
        SIMPLE = helper.registerBlockWithItem("machine_frame_simple", () -> new MachineFrameBlock(SIMPLE_RARITY, TAB_CORE), (block) -> () -> new MachineFrameBlock.MachineFrameItem(block.get(), SIMPLE_RARITY, TAB_CORE));
        ADVANCED = helper.registerBlockWithItem("machine_frame_advanced", () -> new MachineFrameBlock(ADVANCED_RARITY, TAB_CORE), (block) -> () -> new MachineFrameBlock.MachineFrameItem(block.get(), ADVANCED_RARITY, TAB_CORE));
        SUPREME = helper.registerBlockWithItem("machine_frame_supreme", () -> new MachineFrameBlock(SUPREME_RARITY, TAB_CORE), (block) -> () -> new MachineFrameBlock.MachineFrameItem(block.get(), SUPREME_RARITY, TAB_CORE));
        EnvExecutor.runWhenOn(EnvType.CLIENT, () -> this::onClient);
        ServerTickEvents.END_WORLD_TICK.register(level -> {
            if (level.getGameTime() % 40 == 0 && FluidExtractorTile.EXTRACTION.containsKey(level.dimensionType()))
                FluidExtractorTile.EXTRACTION.get(level.dimensionType()).values().forEach(blockPosFluidExtractionProgressHashMap -> blockPosFluidExtractionProgressHashMap.keySet().forEach(pos -> level.destroyBlockProgress(blockPosFluidExtractionProgressHashMap.get(pos).getBreakID(), pos, blockPosFluidExtractionProgressHashMap.get(pos).getProgress())));
        });
        for (int i = 0; i < RANGE_ADDONS.length; i++) {
            int finalI = i;
            RANGE_ADDONS[i] = helper.registerGeneric(Registry.ITEM_REGISTRY, "range_addon" + i, () -> new RangeAddonItem(finalI, TAB_CORE));
        }
        TAB_CORE.addIconStack(() -> new ItemStack(PLASTIC.orElse(Items.STONE)));
        for (DyeColor value : DyeColor.values()) {
            LASER_LENS[value.getId()] = helper.registerGeneric(Registry.ITEM_REGISTRY, "laser_lens" + value.getId(), () -> new LaserLensItem(value.getId()));
        }
        TINY_DRY_RUBBER = helper.registerGeneric(Registry.ITEM_REGISTRY, "tinydryrubber", () -> new RecipelessCustomItem("tinydryrubber", TAB_CORE));
        DRY_RUBBER = helper.registerGeneric(Registry.ITEM_REGISTRY, "dryrubber", () -> new RecipelessCustomItem("dryrubber", TAB_CORE));
        PLASTIC = helper.registerGeneric(Registry.ITEM_REGISTRY, "plastic", () -> new RecipelessCustomItem("plastic", TAB_CORE));
        FERTILIZER = helper.registerGeneric(Registry.ITEM_REGISTRY, "fertilizer", () -> new FertilizerItem(TAB_CORE));
        PINK_SLIME_ITEM = helper.registerGeneric(Registry.ITEM_REGISTRY, "pink_slime", () -> new RecipelessCustomItem("pink_slime", TAB_CORE));
        PINK_SLIME_INGOT = helper.registerGeneric(Registry.ITEM_REGISTRY, "pink_slime_ingot", () -> new RecipelessCustomItem("pink_slime_ingot", TAB_CORE));
        STRAW = helper.registerGeneric(Registry.ITEM_REGISTRY, "straw", () -> new ItemStraw(TAB_CORE));
        FLUID_EXTRACTOR = helper.registerBlockWithTile("fluid_extractor", FluidExtractorBlock::new);
        LATEX_PROCESSING = helper.registerBlockWithTile("latex_processing_unit", LatexProcessingUnitBlock::new);
        DISSOLUTION_CHAMBER = helper.registerBlockWithTile("dissolution_chamber", DissolutionChamberBlock::new);
        SPEED_ADDON_1 = helper.registerGeneric(Registry.ITEM_REGISTRY, "speed_addon_1", () -> new SpeedAddonItem(1, TAB_CORE));
        SPEED_ADDON_2 = helper.registerGeneric(Registry.ITEM_REGISTRY, "speed_addon_2", () -> new SpeedAddonItem(2, TAB_CORE));
        EFFICIENCY_ADDON_1 = helper.registerGeneric(Registry.ITEM_REGISTRY, "efficiency_addon_1", () -> new EfficiencyAddonItem(1, TAB_CORE));
        EFFICIENCY_ADDON_2 = helper.registerGeneric(Registry.ITEM_REGISTRY, "efficiency_addon_2", () -> new EfficiencyAddonItem(2, TAB_CORE));
        PROCESSING_ADDON_1 = helper.registerGeneric(Registry.ITEM_REGISTRY, "processing_addon_1", () -> new ProcessingAddonItem(1, TAB_CORE));
        PROCESSING_ADDON_2 = helper.registerGeneric(Registry.ITEM_REGISTRY, "processing_addon_2", () -> new ProcessingAddonItem(2, TAB_CORE));

        LATEX = new TitaniumFluidInstance(helper, "latex", TitaniumAttributeHandler.Properties.create().density(1000), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/latex_still"), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/latex_flow"), TAB_CORE);
        MEAT = new TitaniumFluidInstance(helper, "meat", TitaniumAttributeHandler.Properties.create().density(1000), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/meat_still"), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/meat_flow"), TAB_CORE);
        SEWAGE = new TitaniumFluidInstance(helper, "sewage", TitaniumAttributeHandler.Properties.create().density(1000), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/sewage_still"), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/sewage_flow"), TAB_CORE);
        ESSENCE = new TitaniumFluidInstance(helper, "essence", TitaniumAttributeHandler.Properties.create().density(1000), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/essence_still"), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/essence_flow"), TAB_CORE);
        SLUDGE = new TitaniumFluidInstance(helper, "sludge",  TitaniumAttributeHandler.Properties.create().density(1000), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/sludge_still"), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/sludge_flow"), TAB_CORE);
        PINK_SLIME = new TitaniumFluidInstance(helper, "pink_slime",  TitaniumAttributeHandler.Properties.create().density(1000), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/pink_slime_still"), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/pink_slime_flow"), TAB_CORE);
        BIOFUEL = new TitaniumFluidInstance(helper, "biofuel",  TitaniumAttributeHandler.Properties.create().density(1000), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/biofuel_still"), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/biofuel_flow"), TAB_CORE);
        ETHER = new TitaniumFluidInstance(helper, "ether_gas",  TitaniumAttributeHandler.Properties.create().density(0), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/ether_gas_still"), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/ether_gas_flow"), TAB_CORE);
        RAW_ORE_MEAT = new OreFluidInstance(helper, "raw_ore_meat",  TitaniumAttributeHandler.Properties.create().density(1000), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/raw_ore_meat_still"), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/raw_ore_meat_flow"), TAB_CORE);
        FERMENTED_ORE_MEAT = new OreFluidInstance(helper, "fermented_ore_meat",  TitaniumAttributeHandler.Properties.create().density(1000), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/ether_gas_still"), new ResourceLocation(Reference.MOD_ID, "blocks/fluids/ether_gas_flow"), TAB_CORE);

        IRON_GEAR = helper.registerGeneric(Registry.ITEM_REGISTRY, "iron_gear", () -> new Item(new Item.Properties().tab(TAB_CORE)));
        GOLD_GEAR = helper.registerGeneric(Registry.ITEM_REGISTRY, "gold_gear", () -> new Item(new Item.Properties().tab(TAB_CORE)));
        DIAMOND_GEAR = helper.registerGeneric(Registry.ITEM_REGISTRY, "diamond_gear", () -> new Item(new Item.Properties().tab(TAB_CORE)));

        helper.registerGeneric(IFRegistries.STRAW_HANDLER_REGISTRY_KEY, "water", WaterStrawHandler::new);
        helper.registerGeneric(IFRegistries.STRAW_HANDLER_REGISTRY_KEY, "lava", LavaStrawHandler::new);
        helper.registerGeneric(IFRegistries.STRAW_HANDLER_REGISTRY_KEY, "milk", MilkStrawHandler::new);
        helper.registerGeneric(IFRegistries.STRAW_HANDLER_REGISTRY_KEY, "essence", EssenceStrawHandler::new);
        helper.registerGeneric(IFRegistries.STRAW_HANDLER_REGISTRY_KEY, "biofuel", () -> new PotionStrawHandler(ModuleCore.BIOFUEL.getSourceFluid())
                .addPotion(MobEffects.MOVEMENT_SPEED, 800, 0)
                .addPotion(MobEffects.DIG_SPEED, 800, 0));
        helper.registerGeneric(IFRegistries.STRAW_HANDLER_REGISTRY_KEY, "sludge", () -> new PotionStrawHandler(ModuleCore.SLUDGE.getSourceFluid())
                .addPotion(MobEffects.WITHER, 600, 0)
                .addPotion(MobEffects.BLINDNESS, 1000, 0)
                .addPotion(MobEffects.MOVEMENT_SLOWDOWN, 1200, 1));
        helper.registerGeneric(IFRegistries.STRAW_HANDLER_REGISTRY_KEY, "sewage",  () -> new PotionStrawHandler(ModuleCore.SEWAGE.getSourceFluid())
                .addPotion(MobEffects.CONFUSION, 1200, 0)
                .addPotion(MobEffects.MOVEMENT_SLOWDOWN, 1200, 0));
        helper.registerGeneric(IFRegistries.STRAW_HANDLER_REGISTRY_KEY, "meat", () -> new PotionStrawHandler(ModuleCore.MEAT.getSourceFluid())
                .addPotion(MobEffects.ABSORPTION, 100, 2)
                .addPotion(MobEffects.SATURATION, 300, 2));
        helper.registerGeneric(IFRegistries.STRAW_HANDLER_REGISTRY_KEY, "latex",  () -> new PotionStrawHandler(ModuleCore.LATEX.getSourceFluid())
                .addPotion(MobEffects.POISON, 1000, 2)
                .addPotion(MobEffects.MOVEMENT_SLOWDOWN, 1000, 2));

        DISSOLUTION_SERIALIZER = helper.registerGeneric(Registry.RECIPE_SERIALIZER_REGISTRY, "dissolution_chamber", () -> new GenericSerializer<>(DissolutionChamberRecipe.class, DISSOLUTION_TYPE));
        DISSOLUTION_TYPE = helper.registerGeneric(Registry.RECIPE_TYPE_REGISTRY, "dissolution_chamber", () -> new SimpleRecipeType<>(new ResourceLocation(Reference.MOD_ID, "dissolution_chamber")));
        FLUID_EXTRACTOR_SERIALIZER = helper.registerGeneric(Registry.RECIPE_SERIALIZER_REGISTRY, "fluid_extractor", () -> new GenericSerializer<>(FluidExtractorRecipe.class, FLUID_EXTRACTOR_TYPE));
        FLUID_EXTRACTOR_TYPE = helper.registerGeneric(Registry.RECIPE_TYPE_REGISTRY, "fluid_extractor", () -> new SimpleRecipeType<>(new ResourceLocation(Reference.MOD_ID, "fluid_extractor")));
        LASER_DRILL_SERIALIZER = helper.registerGeneric(Registry.RECIPE_SERIALIZER_REGISTRY, "laser_drill_ore", () -> new GenericSerializer<>(LaserDrillOreRecipe.class, LASER_DRILL_TYPE));
        LASER_DRILL_TYPE = helper.registerGeneric(Registry.RECIPE_TYPE_REGISTRY, "laser_drill_ore", () -> new SimpleRecipeType<>(new ResourceLocation(Reference.MOD_ID, "laser_drill_ore")));
        LASER_DRILL_FLUID_SERIALIZER = helper.registerGeneric(Registry.RECIPE_SERIALIZER_REGISTRY, "laser_drill_fluid", () -> new GenericSerializer<>(LaserDrillFluidRecipe.class, LASER_DRILL_FLUID_TYPE));
        LASER_DRILL_FLUID_TYPE = helper.registerGeneric(Registry.RECIPE_TYPE_REGISTRY, "laser_drill_fluid", () -> new SimpleRecipeType<>(new ResourceLocation(Reference.MOD_ID, "laser_drill_fluid")));
        STONEWORK_GENERATE_SERIALIZER = helper.registerGeneric(Registry.RECIPE_SERIALIZER_REGISTRY, "stonework_generate", () -> new GenericSerializer<>(StoneWorkGenerateRecipe.class, STONEWORK_GENERATE_TYPE));
        STONEWORK_GENERATE_TYPE = helper.registerGeneric(Registry.RECIPE_TYPE_REGISTRY, "stonework_generate", () -> new SimpleRecipeType<>(new ResourceLocation(Reference.MOD_ID, "stonework_generate")));
        CRUSHER_SERIALIZER = helper.registerGeneric(Registry.RECIPE_SERIALIZER_REGISTRY, "crusher", () -> new GenericSerializer<>(CrusherRecipe.class, CRUSHER_TYPE));
        CRUSHER_TYPE = helper.registerGeneric(Registry.RECIPE_TYPE_REGISTRY, "crusher", () -> new SimpleRecipeType<>(new ResourceLocation(Reference.MOD_ID, "crusher")));
    }

    @Environment(EnvType.CLIENT)
    public void textureStitch(TextureAtlas atlas, Consumer<ResourceLocation> adder) {
        //event.addSprite(LATEX.getSourceFluid().getAttributes().getFlowingTexture()); ??
        //event.addSprite(LATEX.getSourceFluid().getAttributes().getStillTexture()); TODO Add ether as Tags.Fluids.GASEOUS 1.19
    }

    @Environment(EnvType.CLIENT)
    public void onClient() {
        TextureStitchCallback.PRE.register(this::textureStitch);
    }
}
