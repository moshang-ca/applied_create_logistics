package com.moshang.appliedcreatelogistics.items.LogisticsPattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.world.item.ItemStack;


public class LogisticsPatternDetails implements IPatternDetails {
    private final AEItemKey definition;
    private final ItemStack output;
    private final String defaultAddress;

    public LogisticsPatternDetails(ItemStack output, String defaultAddress) {
        this.output = output.copy();
        this.defaultAddress = defaultAddress;
        this.definition = AEItemKey.of(output);
    }

    @Override
    public AEItemKey getDefinition() {
        return definition;
    }

    @Override
    public IInput[] getInputs() {
        return new IInput[0];
    }

    @Override
    public GenericStack[] getOutputs() {
        AEItemKey key = AEItemKey.of(output);
        GenericStack gs = new GenericStack(key, output.getCount());
        return new GenericStack[]{ gs };
    }

    @Override
    public GenericStack getPrimaryOutput() {
        return getOutputs()[0];
    }

    @Override
    public boolean supportsPushInputsToExternalInventory() {
        return false;
    }

    public String getDefaultAddress() {
        return defaultAddress;
    }
}
