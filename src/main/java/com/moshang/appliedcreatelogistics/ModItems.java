package com.moshang.appliedcreatelogistics;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.moshang.appliedcreatelogistics.AllBlocks.MECHANICAL_LOGISTICS_PROVIDER_BLOCK;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AppliedCreateLogistics.MOD_ID);

    public static final RegistryObject<BlockItem> ME_MECHANICAL_LOGISTICS_PROVIDER =
            ITEMS.register(
                    "me_mechanical_logistics_provider",
                    () -> new BlockItem(MECHANICAL_LOGISTICS_PROVIDER_BLOCK.get(),
                            new Item.Properties().stacksTo(64))
            );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
