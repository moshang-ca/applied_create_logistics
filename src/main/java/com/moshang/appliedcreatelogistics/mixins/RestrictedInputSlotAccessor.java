package com.moshang.appliedcreatelogistics.mixins;

import appeng.menu.slot.RestrictedInputSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RestrictedInputSlot.class)
public interface RestrictedInputSlotAccessor {
    @Accessor(value = "which", remap = false)
    RestrictedInputSlot.PlacableItemType appliedCreateLogistics$getWhich();
}
