package com.moshang.appliedcreatelogistics.mixins;

import appeng.crafting.pattern.EncodedPatternItem;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.moshang.appliedcreatelogistics.AllItems;
import com.moshang.appliedcreatelogistics.api.ILogisticsPatternMenu;
import com.moshang.appliedcreatelogistics.items.LogisticsPattern.BlankLogisticsPatternItem;
import com.moshang.appliedcreatelogistics.items.LogisticsPattern.LogisticsPatternItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PatternEncodingTermMenu.class)
public class PatternEncodingTermMenuMixin implements ILogisticsPatternMenu {
    @Unique
    private boolean appliedCreateLogistics$logisticsMode = false;
    @Unique
    private String appliedCreateLogistics$logisticsAddress = "default";
    @Unique
    private boolean appliedCreateLogistics$isEncoding = false;

    @Unique
    public void appliedCreateLogistics$setLogisticsMode(boolean logisticsMode, String address) {
        this.appliedCreateLogistics$logisticsMode = logisticsMode;
        this.appliedCreateLogistics$logisticsAddress = address;

        if (logisticsMode) {
            System.out.println("[ACL DEBUG] 物流模式启用，地址: " + address);
        } else {
            System.out.println("[ACL DEBUG] 物流模式禁用");
        }
    }

    @Inject(method = "encode", at = @At("HEAD"), cancellable = true, remap = false)
    private void onEncode(CallbackInfo ci) {
        if(this.appliedCreateLogistics$isEncoding) return;

        if(this.appliedCreateLogistics$logisticsMode && appliedCreateLogistics$hasBlankLogisticsPattern()) {
            appliedCreateLogistics$debugEncodingState();

            if(appliedCreateLogistics$isOutputSlotOccupied()) {
                System.out.println("[ACL DEBUG] 输出槽位已满");
                return;
            }

            this.appliedCreateLogistics$isEncoding = true;
            try {
                appliedCreateLogistics$encodeLogisticsPattern();
                ci.cancel();
            } finally {
                this.appliedCreateLogistics$isEncoding = false;
            }
        }
    }

    private boolean appliedCreateLogistics$isOutputSlotOccupied() {
        PatternEncodingTermMenu self = (PatternEncodingTermMenu)(Object)this;

        for(int i = 0; i < self.slots.size(); i++) {
            var slot = self.getSlot(i);
            if (slot != null) {
                if (self.getSlotSemantic(slot) == SlotSemantics.ENCODED_PATTERN) {
                    if (!slot.getItem().isEmpty()) {
                        System.out.println("️[ACL DEBUG] 输出槽位已有物品: " + slot.getItem().getItem());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Unique
    private boolean appliedCreateLogistics$hasBlankLogisticsPattern() {
        PatternEncodingTermMenu self = (PatternEncodingTermMenu)(Object)this;

        for(int i = 0; i < self.slots.size(); i++) {
            var slot = self.slots.get(i);
            if(slot != null && !slot.getItem().isEmpty()) {
                SlotSemantic semantic = self.getSlotSemantic(slot);
                if(semantic == SlotSemantics.BLANK_PATTERN) {
                    ItemStack stack = slot.getItem();
                    if (stack.getItem() instanceof BlankLogisticsPatternItem) {
                        System.out.println("[ACL DEBUG] 是空白样板");
                        return true;
                    }
                }
            }
        }
        System.out.println("[ACL DEBUG] 不是空白样板");
        return false;
    }
    
    @Unique
    private void appliedCreateLogistics$encodeLogisticsPattern() {
        PatternEncodingTermMenu self = (PatternEncodingTermMenu)(Object)this;

        try {
            ItemStack blankPattern = appliedCreateLogistics$findBlankLogisticsPattern();
            if(blankPattern.isEmpty()) {
                System.out.println("[ACL DEBUG] 无空白样板");
                return;
            }

            System.out.println("[ACL DEBUG] 调用原版ae2逻辑");
            appliedCreateLogistics$callOriginalEncode(self);

            ItemStack encodedPattern = appliedCreateLogistics$getOutputPattern();
            System.out.println("[ACL DEBUG] 输出样板：" + encodedPattern);
            if(!encodedPattern.isEmpty()) {
                ItemStack logisticsPattern = appliedCreateLogistics$createLogisticsPattern(encodedPattern);
                boolean success =  appliedCreateLogistics$setOutputPattern(logisticsPattern);

                if(success) {
                    blankPattern.shrink(1);
                    System.out.println("[ACL DEBUG] 已创建新样板, 地址" + appliedCreateLogistics$logisticsAddress);

                    appliedCreateLogistics$clearOriginalOutput();
                }
            }
        } catch (Exception e) {
            System.err.println("[ACL DEBUG] 创建新样板失败" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Unique
    private void appliedCreateLogistics$callOriginalEncode(PatternEncodingTermMenu menu) {
        try {
            java.lang.reflect.Method encodeMethod = PatternEncodingTermMenu.class.getDeclaredMethod("encode");
            encodeMethod.setAccessible(true);
            encodeMethod.invoke(menu);
        } catch (Exception e) {
            System.out.println("[ACL DEBUG] 调用失败");
        }
    }

    @Unique
    private boolean appliedCreateLogistics$setOutputPattern(ItemStack pattern) {
        PatternEncodingTermMenu self = (PatternEncodingTermMenu)(Object)this;

        for(int i = 0; i < self.slots.size(); i++) {
            var slot = self.getSlot(i);
            if(slot != null) {
                SlotSemantic semantic = self.getSlotSemantic(slot);
                if (semantic == SlotSemantics.ENCODED_PATTERN) {
                    slot.set(pattern);
                    System.out.println("✅ 设置到输出槽位: " + i);
                    return true;
                }
            }
        }

        return false;
    }

    @Unique
    private ItemStack appliedCreateLogistics$createLogisticsPattern(ItemStack originalPattern) {
        ItemStack logisticsPattern = new ItemStack(AllItems.LOGISTICS_PATTERN.get());

        if(originalPattern.hasTag()) {
            logisticsPattern.setTag(originalPattern.getTag().copy());
        }

        LogisticsPatternItem.setAddress(logisticsPattern, appliedCreateLogistics$logisticsAddress);

        return logisticsPattern;
    }

    @Unique
    private ItemStack appliedCreateLogistics$getOutputPattern() {
        PatternEncodingTermMenu self = (PatternEncodingTermMenu)(Object)this;

        for(int i = 0; i < self.slots.size(); i++) {
            var slot = self.getSlot(i);
            if(slot != null && slot.mayPickup(appliedCreateLogistics$getPlayer())) {
                ItemStack stack = slot.getItem();
                if(stack.getItem() instanceof EncodedPatternItem) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Unique
    private Player appliedCreateLogistics$getPlayer() {
        PatternEncodingTermMenu self = (PatternEncodingTermMenu)(Object)this;
        return self.getPlayer();
    }

    @Unique
    private ItemStack appliedCreateLogistics$findBlankLogisticsPattern() {
        PatternEncodingTermMenu self = (PatternEncodingTermMenu)(Object)this;

        for(int i = 0; i < self.slots.size(); i++) {
            var slot = self.getSlot(i);
            if(slot != null && !slot.getItem().isEmpty()) {
                // 只查找终端内部的BLANK_PATTERN槽位
                SlotSemantic semantic = self.getSlotSemantic(slot);
                if (semantic == SlotSemantics.BLANK_PATTERN) {
                    ItemStack stack = slot.getItem();
                    if(stack.getItem() instanceof BlankLogisticsPatternItem) {
                        System.out.println("🔍 找到终端空白样板: 槽位 " + i);
                        return stack;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Unique
    private void appliedCreateLogistics$clearOriginalOutput() {
        PatternEncodingTermMenu self = (PatternEncodingTermMenu)(Object)this;

        // 清空原版编码的输出
        for(int i = 0; i < self.slots.size(); i++) {
            var slot = self.getSlot(i);
            if(slot != null) {
                SlotSemantic semantic = self.getSlotSemantic(slot);
                if (semantic == SlotSemantics.ENCODED_PATTERN) {
                    // 检查是否是原版编码的输出
                    ItemStack stack = slot.getItem();
                    if (!stack.isEmpty() && stack.getItem() instanceof EncodedPatternItem &&
                            !(stack.getItem() instanceof LogisticsPatternItem)) {
                        slot.set(ItemStack.EMPTY);
                        System.out.println("🧹 清空原版编码输出");
                    }
                }
            }
        }
    }

    //this function is used to show debug state
    @Unique
    private void appliedCreateLogistics$debugEncodingState() {
        PatternEncodingTermMenu self = (PatternEncodingTermMenu)(Object)this;

        System.out.println("=== ACL 编码状态调试 ===");
        System.out.println("[ACL DEBUG] 物流模式: " + appliedCreateLogistics$logisticsMode);
        System.out.println("[ACL DEBUG] 有空白物流样板: " + appliedCreateLogistics$hasBlankLogisticsPattern());
        System.out.println("[ACL DEBUG] 输出槽位占用: " + appliedCreateLogistics$isOutputSlotOccupied());

        // 检查输入槽位
        for(int i = 0; i < self.slots.size(); i++) {
            var slot = self.getSlot(i);
            if (slot != null) {
                var semantic = self.getSlotSemantic(slot);
                if (semantic == SlotSemantics.CRAFTING_GRID ||
                        semantic == SlotSemantics.PROCESSING_INPUTS) {
                    ItemStack stack = slot.getItem();
                    if (!stack.isEmpty()) {
                        System.out.println("[ACL DEBUG] 输入槽位 " + i + " (" + semantic + "): " + stack);
                    }
                }
            }
        }
        System.out.println("======================");
    }
}
