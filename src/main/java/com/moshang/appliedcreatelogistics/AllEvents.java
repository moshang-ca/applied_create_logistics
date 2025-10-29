package com.moshang.appliedcreatelogistics;

import com.moshang.appliedcreatelogistics.blocks.mechanicalProvider.MechanicalLogisticsProviderBlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class AllEvents {
    public static void eventRegister(FMLCommonSetupEvent event) {
        System.out.println("[DEBUG] eventRegister 被调用");
        event.enqueueWork(() -> {
            System.out.println("[DEBUG] 开始注册tick事件");
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, TickEvent.ServerTickEvent.class,
                    MechanicalLogisticsProviderBlockEntity::onServerTick);
            System.out.println("[DEBUG] tick事件注册完成");
        });
    }
}
