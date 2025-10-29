package com.moshang.appliedcreatelogistics.blocks.mechanicalProvider;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.pattern.EncodedPatternItem;
import com.moshang.appliedcreatelogistics.api.IPackagingProviderService;
import com.moshang.appliedcreatelogistics.items.LogisticsPattern.LogisticsPatternDetails;
import com.moshang.appliedcreatelogistics.items.LogisticsPattern.LogisticsPatternItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.*;


public class MechanicalPackaging implements ICraftingProvider, IPackagingProviderService {
    private final MechanicalLogisticsProviderBlockEntity host;

    private static class PendingRequest {
        public final IPatternDetails pattern;
        public int count;
        public final String address;
        public final List<KeyCounter[]> keyCountersList;

        public PendingRequest(IPatternDetails pattern, String address,  KeyCounter[] firstKeyCounters) {
            this.pattern = pattern;
            this.count = 1;
            this.address = address;
            this.keyCountersList = new ArrayList<>();
            this.keyCountersList.add(firstKeyCounters);
        }

        public void addRequest(KeyCounter[] keyCounters) {
            this.keyCountersList.add(keyCounters);
            this.count++;
        }

        public KeyCounter[] getMergedKeyCounters() {
            if (keyCountersList.size() == 1) {
                return keyCountersList.get(0);
            }

            KeyCounter[] merged = new KeyCounter[keyCountersList.get(0).length];
            for (int i = 0; i < merged.length; i++) {
                merged[i] = new KeyCounter();

                for (KeyCounter[] keyCounters : keyCountersList) {
                    for (var entry : keyCounters[i]) {
                        merged[i].add(entry.getKey(), entry.getLongValue());
                    }
                }
            }
            return merged;
        }
    }

    public MechanicalPackaging(MechanicalLogisticsProviderBlockEntity host) {
        this.host = host;
    }


    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        List<IPatternDetails> patterns = new ArrayList<>();
        ItemStackHandler patternSlots = host.getItemHandler();

        for (int i = 0; i < patternSlots.getSlots(); i++) {
            ItemStack stack = patternSlots.getStackInSlot(i);

            if (!stack.isEmpty()) {
                IPatternDetails patternDetails = null;

                if(stack.getItem() instanceof LogisticsPatternItem logisticsPatternItem) {
                    patternDetails = logisticsPatternItem.decode(stack, host.getLevel(), false);
                }

                if(patternDetails != null && patternDetails.getOutputs() != null && patternDetails.getOutputs().length > 0) {
                    patterns.add(patternDetails);
                }
            }
        }

        return patterns;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] keyCounters) {

        String address = "default";
        if (patternDetails instanceof LogisticsPatternDetails details) {
            address = details.getDefaultAddress();
        }


        ItemStackHandler packageContents = new ItemStackHandler(9);
        int currentSlot = 0;

        for (int inputIndex = 0; inputIndex < patternDetails.getInputs().length; inputIndex++) {
            IPatternDetails.IInput input = patternDetails.getInputs()[inputIndex];


            for (var entry : keyCounters[inputIndex]) {
                AEKey actualKey = entry.getKey();
                long allocatedAmount = entry.getLongValue();

                if (allocatedAmount > 0 && input.isValid(actualKey, host.getLevel())) {
                    if (actualKey instanceof AEItemKey actualItemKey) {
                        if (currentSlot < packageContents.getSlots()) {
                            ItemStack itemStack = actualItemKey.toStack((int) allocatedAmount);
                            packageContents.setStackInSlot(currentSlot, itemStack);
                            currentSlot++;
                        }
                    }
                }
            }
        }

        host.exportToCreateSystem(packageContents, address);

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

    public void tick() {
    }
}
