package com.moshang.appliedcreatelogistics.mixins;

import appeng.menu.slot.RestrictedInputSlot;
import com.moshang.appliedcreatelogistics.items.LogisticsPattern.BlankLogisticsPatternItem;
import com.moshang.appliedcreatelogistics.mixins.RestrictedInputSlotAccessor;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RestrictedInputSlot.class)
public class RestrictedInputSlotMixin {

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true, remap = false)
    private void appliedCreateLogistics$mayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        System.out.println("🔍 RestrictedInputSlot.mayPlace - 物品: " + stack.getItem());

        RestrictedInputSlot self = (RestrictedInputSlot) (Object) this;
        RestrictedInputSlotAccessor accessor = (RestrictedInputSlotAccessor) self;

        RestrictedInputSlot.PlacableItemType which = accessor.appliedCreateLogistics$getWhich();

        // 检查是否为空白样板槽位
        if (which == RestrictedInputSlot.PlacableItemType.BLANK_PATTERN) {
            System.out.println("🔍 这是空白样板槽位");

            if (stack.getItem() instanceof BlankLogisticsPatternItem) {
                System.out.println("✅ RestrictedInputSlot: 允许空白物流样板");
                cir.setReturnValue(true);
            }
        }
    }
}