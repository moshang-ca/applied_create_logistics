package com.moshang.appliedcreatelogistics;

import com.moshang.appliedcreatelogistics.blocks.mechanicalProvider.MechanicalLogisticsProviderBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = AppliedCreateLogistics.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AllBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, AppliedCreateLogistics.MOD_ID);

    public static final RegistryObject<MechanicalLogisticsProviderBlock> MECHANICAL_LOGISTICS_PROVIDER_BLOCK =
            BLOCKS.register("me_mechanical_logistics_provider", MechanicalLogisticsProviderBlock::new);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
