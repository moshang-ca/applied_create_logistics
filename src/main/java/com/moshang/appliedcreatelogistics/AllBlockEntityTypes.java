package com.moshang.appliedcreatelogistics;

import com.moshang.appliedcreatelogistics.mechanicalProvider.MechanicalLogisticsProviderBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.moshang.appliedcreatelogistics.AllBlocks.MECHANICAL_LOGISTICS_PROVIDER_BLOCK;

@Mod.EventBusSubscriber(modid = AppliedCreateLogistics.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AllBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AppliedCreateLogistics.MOD_ID);

    public static final RegistryObject<BlockEntityType<MechanicalLogisticsProviderBlockEntity>> MECHANICAL_PROVIDER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("me_mechanical_provider", () -> BlockEntityType.Builder.of(
                    MechanicalLogisticsProviderBlockEntity::new,
                    MECHANICAL_LOGISTICS_PROVIDER_BLOCK.get()
            ).build(null));

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
