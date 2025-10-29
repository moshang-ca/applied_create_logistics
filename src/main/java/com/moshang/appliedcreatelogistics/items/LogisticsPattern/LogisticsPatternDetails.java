package com.moshang.appliedcreatelogistics.items.LogisticsPattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.pattern.EncodedPatternItem;
import com.moshang.appliedcreatelogistics.AllItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;


public class LogisticsPatternDetails implements IPatternDetails {
    private final ItemStack patternStack;
    private final AEItemKey definition;
    private final IPatternDetails decodePattern;
    private final String defaultAddress;

    public LogisticsPatternDetails(ItemStack patternStack) {
        this.patternStack = patternStack;

        CompoundTag tag = patternStack.getTag();
        if(tag != null && tag.contains("LogisticsAddress")) {
            this.defaultAddress = tag.getString("LogisticsAddress");
        } else {
            this.defaultAddress = "default_address";
        }

        this.definition = AEItemKey.of(patternStack.getItem());

        this.decodePattern = null;
    }

    public LogisticsPatternDetails(ItemStack patternStack, String defaultAddress) {
        this.patternStack = patternStack;
        this.definition = AEItemKey.of(AllItems.BLANK_LOGISTICS_PATTERN.get(), null);
        this.defaultAddress = defaultAddress;
        this.decodePattern = null;
    }

    @Override
    public AEItemKey getDefinition() {
        return this.definition;
    }

    @Override
    public IInput[] getInputs() {
        return new IInput[0];
    }

    @Override
    public GenericStack[] getOutputs() {
        return new GenericStack[0];
    }

    @Override
    public GenericStack getPrimaryOutput() {
        return null;
    }

    @Override
    public boolean supportsPushInputsToExternalInventory() {
        if(this.decodePattern != null) {
            return this.decodePattern.supportsPushInputsToExternalInventory();
        }
        return true;
    }

    @Override
    public void pushInputsToExternalInventory(KeyCounter[] inputHolder, PatternInputSink inputSink) {
        if(this.decodePattern != null) {
            this.decodePattern.pushInputsToExternalInventory(inputHolder, inputSink);
        }  else {
            IPatternDetails.super.pushInputsToExternalInventory(inputHolder, inputSink);
        }
    }

    public String getDefaultAddress() {
        return defaultAddress;
    }

    public ItemStack getPatternStack() {
        return patternStack;
    }
}
