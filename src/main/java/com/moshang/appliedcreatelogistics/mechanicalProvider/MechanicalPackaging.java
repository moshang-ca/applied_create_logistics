package com.moshang.appliedcreatelogistics.mechanicalProvider;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import com.moshang.appliedcreatelogistics.api.IPackagingProviderService;
import com.moshang.appliedcreatelogistics.items.LogisticsPattern.LogisticsPatternDetails;
import net.minecraft.world.item.ItemStack;

import java.util.List;


public class MechanicalPackaging implements ICraftingProvider, IPackagingProviderService {
    private final MechanicalLogisticsProviderBlockEntity host;

    public MechanicalPackaging(MechanicalLogisticsProviderBlockEntity host) {
        this.host = host;
    }


    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        //TODO:实现动态读入样板的功能
        return List.of(
                new LogisticsPatternDetails(new ItemStack(net.minecraft.world.item.Items.DIAMOND), "central_storage"),
                new LogisticsPatternDetails(new ItemStack(net.minecraft.world.item.Items.IRON_INGOT), "factory_storage")
        );
    }

    @Override
    public boolean pushPattern(IPatternDetails iPatternDetails, KeyCounter[] keyCounters) {
        if(!(iPatternDetails instanceof LogisticsPatternDetails details))
            return false;

        GenericStack[] outputs = details.getOutputs();
        String address = details.getDefaultAddress();

        host.exportToCreateSystem(outputs, address);

        return true;
    }

    @Override
    public boolean requestPackaging(String jobId, String destination) {
        return true;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public String getDefaultAddress() {
        return "Default";
    }
}
