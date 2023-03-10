package com.buuz135.industrial;

import com.buuz135.industrial.recipe.provider.IndustrialRecipeProvider;
import com.buuz135.industrial.recipe.provider.IndustrialSerializableProvider;
import com.buuz135.industrial.recipe.provider.IndustrialTagsProvider;
import com.buuz135.industrial.utils.Reference;
import com.buuz135.industrial.utils.data.IndustrialBlockstateProvider;
import com.buuz135.industrial.utils.data.IndustrialModelProvider;
import com.hrznstudio.titanium.TitaniumData;
import com.hrznstudio.titanium.datagenerator.loot.TitaniumLootTableProvider;
import com.hrznstudio.titanium.datagenerator.model.BlockItemModelGeneratorProvider;
import com.hrznstudio.titanium.fabric.NonNullLazy;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IndustrialForegoingData implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        var existingData = System.getProperty("twilightforest.data.existingData").split(";");
        ExistingFileHelper helper = new ExistingFileHelper(Arrays.stream(existingData).map(Paths::get).toList(), Collections.emptySet(),
                true, null, null);
//        TitaniumData.addDataProvider(event);
        NonNullLazy<List<Block>> blocksToProcess = NonNullLazy.of(() ->
                Registry.BLOCK
                        .stream()
                        .filter(block -> !block.getClass().equals(LiquidBlock.class))
                        .filter(basicBlock -> Optional.of(Registry.BLOCK.getKey(basicBlock))
                                .map(ResourceLocation::getNamespace)
                                .filter(Reference.MOD_ID::equalsIgnoreCase)
                                .isPresent())
                        .collect(Collectors.toList())
        );
        generator.addProvider(IndustrialTagsProvider.Blocks::new);
        generator.addProvider(IndustrialTagsProvider.Items::new);
        generator.addProvider(true, new IndustrialRecipeProvider(generator, blocksToProcess));
        generator.addProvider(true, new IndustrialSerializableProvider(generator, Reference.MOD_ID));
        generator.addProvider(true, new TitaniumLootTableProvider(generator, blocksToProcess));
        generator.addProvider(true, new BlockItemModelGeneratorProvider(generator, Reference.MOD_ID, blocksToProcess));
        generator.addProvider(true, new IndustrialBlockstateProvider(generator, helper, blocksToProcess));
        generator.addProvider(true, new IndustrialModelProvider(generator, helper));
    }
}
