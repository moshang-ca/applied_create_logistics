package com.moshang.appliedcreatelogistics;

import com.google.common.eventbus.Subscribe;
import com.moshang.appliedcreatelogistics.mechanicalProvider.MechanicalLogisticsProviderScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = AppliedCreateLogistics.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(AllMenuTypes.MECHANICAL_LOGISTICS_PROVIDER_MENU.get(), MechanicalLogisticsProviderScreen::new);
        });
    }
}
