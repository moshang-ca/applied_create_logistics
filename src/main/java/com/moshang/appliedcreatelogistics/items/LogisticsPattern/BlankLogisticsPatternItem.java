package com.moshang.appliedcreatelogistics.items.LogisticsPattern;

import appeng.core.definitions.AEItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class BlankLogisticsPatternItem extends Item {
    public BlankLogisticsPatternItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("在样板编码终端中编码后变为物流样板"));
        tooltip.add(Component.literal("可以设置物流地址"));
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return AEItems.BLANK_PATTERN.getEnglishName();
    }
}
