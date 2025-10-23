package com.moshang.appliedcreatelogistics.mechanicalProvider;

import appeng.crafting.pattern.EncodedPatternItem;
import com.moshang.appliedcreatelogistics.AllMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class MechanicalLogisticsProviderMenu extends AbstractContainerMenu {
    private final MechanicalLogisticsProviderBlockEntity blockEntity;
    private final ItemStackHandler handler;

    public MechanicalLogisticsProviderMenu(int containerId, Inventory playerInv, MechanicalLogisticsProviderBlockEntity be) {
        super(AllMenuTypes.MECHANICAL_LOGISTICS_PROVIDER_MENU.get(), containerId);
        this.blockEntity = be;
        this.handler = be.getItemHandler();
        //TODO:修改槽位的X，Y位置，适配GUI
        int cols = 9;
        int startX = 8;
        int startY = 45;
        int slotSize = 18;

        for(int i = 0; i < handler.getSlots(); ++i) {
            int x = startX + ( i % cols) * slotSize;
            int y = startY + ( i / cols) * slotSize;

            this.addSlot(new Slot(new Inventory(playerInv.player), i, x, y) {
                @Override
                public boolean mayPlace(@Nonnull ItemStack stack) {
                    return stack.getItem() instanceof EncodedPatternItem;
                }
            });
        }

        int playerStartX = 8;
        int playerStartY = 131;
        for(int row = 0; row < 3; ++row) {
            for(int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, playerStartX + col * slotSize, playerStartY + row * slotSize));
            }
        }

        for(int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, playerStartX + col * slotSize, playerStartY + 58));
        }
    }

    public MechanicalLogisticsProviderMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, (MechanicalLogisticsProviderBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    @Override
    public @Nonnull ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if(slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            int containerSlots = handler.getSlots();
            int totalSlots = this.slots.size();

            if(index < containerSlots) {
                if(!this.moveItemStackTo(stackInSlot, containerSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if(!this.moveItemStackTo(stackInSlot, 0, containerSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if(!stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && player.distanceToSqr(blockEntity.getBlockPos().getCenter()) < 64;
    }
}
