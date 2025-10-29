package com.moshang.appliedcreatelogistics;

import appeng.api.ids.AECreativeTabIds;
import com.moshang.appliedcreatelogistics.debug.AEDebug;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.launch.MixinBootstrap;

@Mod(AppliedCreateLogistics.MOD_ID)
public class AppliedCreateLogistics {
    public static final String MOD_ID = "applied_create_logistics";
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath("applied_create_logistics", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public AppliedCreateLogistics(FMLJavaModLoadingContext context) throws NoSuchMethodException {
        MixinBootstrap.init();
        IEventBus modEventBus = context.getModEventBus();
        onRegistry(modEventBus);

        AEDebug.printPatternClasses();

        CHANNEL.registerMessage(0, SetLogisticsModePacket.class,
                SetLogisticsModePacket::encode, SetLogisticsModePacket::new,
                SetLogisticsModePacket::handle);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        AllEvents.eventRegister(event);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == AECreativeTabIds.MAIN) {
            event.accept(AllItems.ME_MECHANICAL_LOGISTICS_PROVIDER);
            event.accept(AllItems.BLANK_LOGISTICS_PATTERN);
        }
    }

    private void onRegistry(IEventBus bus) {
        AllBlocks.register(bus);
        AllBlockEntityTypes.register(bus);
        AllItems.register(bus);
        AllMenuTypes.register(bus);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {

    }
}
