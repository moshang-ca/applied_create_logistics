package com.moshang.appliedcreatelogistics;

import appeng.api.ids.AECreativeTabIds;
import com.moshang.appliedcreatelogistics.items.ModItems;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AppliedCreateLogistics.MOD_ID)
public class AppliedCreateLogistics {
    public static final String MOD_ID = "applied_create_logistics";

    public AppliedCreateLogistics(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        onRegistry(modEventBus);

        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == AECreativeTabIds.MAIN) {
            event.accept(ModItems.ME_MECHANICAL_LOGISTICS_PROVIDER);
        }
    }

    private void onRegistry(IEventBus bus) {
        AllBlocks.register(bus);
        AllBlockEntityTypes.register(bus);
        ModItems.register(bus);
        AllMenuTypes.register(bus);
    }
}
