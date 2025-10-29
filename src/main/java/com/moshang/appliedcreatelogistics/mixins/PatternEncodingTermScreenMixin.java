package com.moshang.appliedcreatelogistics.mixins;

import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.sync.network.NetworkHandler;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.moshang.appliedcreatelogistics.AppliedCreateLogistics;
import com.moshang.appliedcreatelogistics.SetLogisticsModePacket;
import com.moshang.appliedcreatelogistics.items.LogisticsPattern.BlankLogisticsPatternItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PatternEncodingTermScreen.class)
public class PatternEncodingTermScreenMixin<C extends PatternEncodingTermMenu> {
    @Unique
    private boolean appliedCreateLogistics$logisticsMode = false;
    @Unique
    private EditBox appliedCreateLogistics$addressTextField;
    @Unique
    private boolean appliedCreateLogistics$widgetsInitialized = false;

    @Unique
    private void appliedcreatelogistics$addWidgetViaAccessor(PatternEncodingTermScreen<C> screen, EditBox widget) {
        ScreenAccessor accessor = (ScreenAccessor) screen;
        accessor.getChildren().add(widget);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void appliedCreateLogistics$onInit(CallbackInfo ci) {
        PatternEncodingTermScreen<C> self = (PatternEncodingTermScreen<C>) (Object)this;

        if (!appliedCreateLogistics$widgetsInitialized) {
            try {
                // 使用Accessor获取font
                ScreenAccessor accessor = (ScreenAccessor) self;
                Font font = accessor.appliedCreateLogistics$getFont();

                if (font == null) {
                    System.err.println("❌ 通过Accessor获取的font为null");
                    return;
                }

                this.appliedCreateLogistics$addressTextField = new EditBox(
                        font, // 使用Accessor获取的font
                        0, 0, 80, 16,
                        Component.literal("Address")
                );
                this.appliedCreateLogistics$addressTextField.setMaxLength(16);
                this.appliedCreateLogistics$addressTextField.setVisible(false);
                this.appliedCreateLogistics$addressTextField.setHint(Component.literal("Address"));
                this.appliedCreateLogistics$addressTextField.setResponder(text -> appliedCreateLogistics$onAddressChanged());

                // 安全地添加控件
                appliedCreateLogistics$addWidgetSafely(self, this.appliedCreateLogistics$addressTextField);

                appliedCreateLogistics$widgetsInitialized = true;
                System.out.println("✅ 物流地址输入框初始化完成");

            } catch (Exception e) {
                System.err.println("❌ 初始化物流地址输入框失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"),remap = false)
    private void onUpdateBeforeRender(CallbackInfo ci) {
        PatternEncodingTermScreen<C> self = (PatternEncodingTermScreen<C>) (Object)this;
        appliedCreateLogistics$checkBlankPattern();
        appliedCreateLogistics$updateLayout();
    }

    @Inject(method = "renderTooltip", at = @At("TAIL"))
    private void renderTooltip(GuiGraphics graphics, int x, int y, CallbackInfo ci) {
        if(appliedCreateLogistics$logisticsMode && appliedCreateLogistics$widgetsInitialized) {
            try {
                PatternEncodingTermScreen<C> self = (PatternEncodingTermScreen<C>) (Object)this;
                ScreenAccessor accessor = (ScreenAccessor) self;
                Font font = accessor.appliedCreateLogistics$getFont();

                if (font != null) {
                    graphics.drawString(font, "address",
                            self.getGuiLeft() + 10, self.getGuiTop() + 120,
                            0x404040, false);
                }
            } catch (Exception e) {
                System.err.println("❌ 渲染地址标签失败: " + e.getMessage());
            }
        }
    }

    @Unique
    private void appliedCreateLogistics$checkBlankPattern() {
        PatternEncodingTermScreen<C> self = (PatternEncodingTermScreen<C>) (Object)this;

        Object menuObj = self.getMenu();
        if (!(menuObj instanceof PatternEncodingTermMenu menu)) {
            return; // 如果不是PatternEncodingTermMenu，直接返回
        }

        boolean hasBlankLogisticsPattern = appliedCreateLogistics$hasBlankLogisticsPattern(menu);

        if (hasBlankLogisticsPattern && !this.appliedCreateLogistics$logisticsMode) {
            appliedCreateLogistics$setLogisticsMode(true, "default");
        } else if(!hasBlankLogisticsPattern && this.appliedCreateLogistics$logisticsMode) {
            appliedCreateLogistics$setLogisticsMode(false, "");
        }
    }

    @Unique
    private void appliedCreateLogistics$setLogisticsMode(boolean enabled, String address) {
        this.appliedCreateLogistics$logisticsMode = enabled;
        this.appliedCreateLogistics$addressTextField.setVisible(enabled);

        if (enabled) {
            this.appliedCreateLogistics$addressTextField.setValue(address);
            PatternEncodingTermScreen<C> self = (PatternEncodingTermScreen<C>) (Object) this;

            // 使用Forge网络系统发送数据包
            AppliedCreateLogistics.CHANNEL.sendToServer(
                    new SetLogisticsModePacket(self.getMenu().containerId, true, address)
            );
        }
    }

    @Unique
    private boolean appliedCreateLogistics$hasBlankLogisticsPattern(PatternEncodingTermMenu menu) {
        for(int i = 0; i < menu.slots.size(); ++i) {
            var slot = menu.slots.get(i);
            if(slot != null && !slot.getItem().isEmpty()) {
                ItemStack stack = slot.getItem();
                if(stack.getItem() instanceof BlankLogisticsPatternItem) {
                    return true;
                }
            }
        }
        return false;
    }

    @Unique
    private void appliedCreateLogistics$onAddressChanged() {
        if(this.appliedCreateLogistics$logisticsMode) {
            PatternEncodingTermScreen<C> self = (PatternEncodingTermScreen<C>) (Object)this;
            AppliedCreateLogistics.CHANNEL.sendToServer(
                    new SetLogisticsModePacket(self.getMenu().containerId, true, this.appliedCreateLogistics$addressTextField.getValue())
            );
        }
    }

    @Unique
    private void appliedCreateLogistics$updateLayout() {
        PatternEncodingTermScreen<C> self = (PatternEncodingTermScreen<C>) (Object)this;

        int x = self.getGuiLeft() + 10;
        int y = self.getGuiTop() + 132;
        this.appliedCreateLogistics$addressTextField.setX(x);
        this.appliedCreateLogistics$addressTextField.setY(y);
    }

    @Unique
    private void appliedCreateLogistics$addWidgetSafely(PatternEncodingTermScreen<C> screen, EditBox widget) {
        try {
            // 使用ScreenAccessor添加控件
            if (screen instanceof ScreenAccessor) {
                ((ScreenAccessor) screen).getChildren().add(widget);
            } else {
                // 备用方案...
            }
        } catch (Exception e) {
            System.err.println("❌ 添加控件失败: " + e.getMessage());
        }
    }
}
