package com.moshang.appliedcreatelogistics.mixins;

import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.util.inv.filter.AEItemDefinitionFilter;
import com.moshang.appliedcreatelogistics.items.LogisticsPattern.BlankLogisticsPatternItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AEItemDefinitionFilter.class)
public class AEItemDefinitionFilterMixin {

    @Inject(method = "allowInsert", at = @At("HEAD"), cancellable = true, remap = false)
    private void appliedcreatelogistics$allowInsert(appeng.api.inventories.InternalInventory inv, int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        System.out.println("🔍 AEItemDefinitionFilter.allowInsert - 物品: " + stack.getItem());

        // 简单直接地检查是否为空白物流样板
        if (stack.getItem() instanceof BlankLogisticsPatternItem) {
            System.out.println("✅ 检测到空白物流样板，允许放入");
            System.out.println("🔍 调用栈:");
            Thread.dumpStack();
            cir.setReturnValue(true);
        }
    }
}