package com.moshang.appliedcreatelogistics.items.LogisticsPattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.EncodedPatternItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LogisticsPatternItem extends EncodedPatternItem {
    public LogisticsPatternItem(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable IPatternDetails decode(ItemStack itemStack, Level level, boolean autoRecovery) {
        return new LogisticsPatternDetails(itemStack);
    }

    @Override
    public @Nullable IPatternDetails decode(AEItemKey aeItemKey, Level level) {
        return new LogisticsPatternDetails(aeItemKey.toStack());
    }

    public static void setAddress(ItemStack patternStack, String address) {
        if(!(patternStack.getItem() instanceof LogisticsPatternItem))
            return;
        patternStack.getOrCreateTag().putString("LogisticsAddress", address);
    }

    public static String getAddress(ItemStack patternStack) {
        if(!(patternStack.getItem() instanceof LogisticsPatternItem))
            return "default";

        var tag = patternStack.getTag();
        return tag != null && tag.contains("LogisticsAddress") ? tag.getString("LogisticsAddress") : "default";
    }

    /*
    public ItemStack getOutput(ItemStack patternStack) {
        Level level = net.minecraft.client.Minecraft.getInstance().level;
        if(level == null)
            return ItemStack.EMPTY;

        IPatternDetails details = decode(patternStack, level, true);
        if(details != null) {
            GenericStack output = details.getPrimaryOutput();
            if(output != null && output.what() instanceof AEItemKey key) {
                return key.toStack();
            }
        }

        return ItemStack.EMPTY;
    }

     */

    public ItemStack getOutput(ItemStack patternStack) {
        if (patternStack.isEmpty() || !(patternStack.getItem() instanceof LogisticsPatternItem)) {
            return ItemStack.EMPTY;
        }

        LogisticsPatternDetails details = new LogisticsPatternDetails(patternStack);
        GenericStack output = details.getPrimaryOutput();
        if (output != null && output.what() instanceof AEItemKey key) {
            return key.toStack((int) output.amount());
        }

        return ItemStack.EMPTY;
    }

}
