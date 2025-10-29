package com.moshang.appliedcreatelogistics;

import com.moshang.appliedcreatelogistics.blocks.mechanicalProvider.MechanicalLogisticsProviderMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AllMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, "appliedcreatelogistics");

    public static final RegistryObject<MenuType<MechanicalLogisticsProviderMenu>> MECHANICAL_LOGISTICS_PROVIDER_MENU =
            MENU_TYPES.register(
                    "mechanical_logistics_provider_menu",
                    () -> IForgeMenuType.create((windowId, inv, data) -> {
                        return new MechanicalLogisticsProviderMenu(windowId, inv, data);
                    })
            );

    public static void register(IEventBus bus) {
        MENU_TYPES.register(bus);
    }
}
