package com.moshang.appliedcreatelogistics.mechanicalProvider;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.me.GridNode;
import com.moshang.appliedcreatelogistics.AllBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Clearable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class MechanicalLogisticsProviderBlockEntity extends BlockEntity implements IInWorldGridNodeHost, Clearable, MenuProvider, IActionHost {
    private GridNode node;
    private IManagedGridNode managedNode;
    private static final float powerUsage = 15.f;
    private static final int SLOT_COUNT = 9;

    private final ItemStackHandler patternSlot = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public void setStackInSlot(int slot, @NonNull ItemStack stack) {
            if(!stack.isEmpty() && !(stack.getItem() instanceof EncodedPatternItem)) {
                return;
            }

            super.setStackInSlot(slot, stack);
            setChanged();
            return;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    };

    public MechanicalLogisticsProviderBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.MECHANICAL_PROVIDER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public GridNode getGridNode(Direction side) {
        if(node == null) {
            managedNode = GridHelper.createManagedNode(this, new MechanicalLogisticsProviderNode(this));
            managedNode.setIdlePowerUsage(powerUsage);
            managedNode.setExposedOnSides(EnumSet.allOf(Direction.class));
            node = (GridNode) managedNode.getNode();
        }
        return node;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (managedNode != null) {
            managedNode.destroy();
            managedNode = null;
        }
    }

    public void exportToCreateSystem(GenericStack[] outputs, String address) {
        if (outputs == null) return;

        for (GenericStack gs : outputs) {
            if (!(gs.what() instanceof AEItemKey itemKey)) continue;

            ItemStack output = new ItemStack(itemKey.getItem(), (int) gs.amount());
            if (output.isEmpty()) continue;

            ItemStack packageItem = new ItemStack(
                    ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("create", "package"))
            );

            if (packageItem.isEmpty()) {
                System.err.println("[MechanicalAE] 未找到 create:package 物品！");
                return;
            }

            var pkg = new net.minecraft.nbt.CompoundTag();
            var content = new net.minecraft.nbt.CompoundTag();
            content.putString("id", ForgeRegistries.ITEMS.getKey(output.getItem()).toString());
            content.putByte("Count", (byte) output.getCount());
            pkg.put("Content", content);
            pkg.putString("Address", address);
            packageItem.getOrCreateTag().put("Package", pkg);

            level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                    packageItem));
        }
    }


    @Override
    public Component getDisplayName() {
        return Component.translatable("block.applied_create_logistics.me_mechanical_logistics_provider");
    }

    @Nullable
    @Override
    public  AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new MechanicalLogisticsProviderMenu(containerId, playerInv, this);
    }

    public ItemStackHandler getItemHandler() {
        return patternSlot;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MechanicalLogisticsProviderBlockEntity be) {

    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if(tag.contains("PatternSlot")) {
            patternSlot.deserializeNBT(tag.getCompound("PatternSlot"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("PatternSlot", patternSlot.serializeNBT());
    }

    @Override
    public void clearContent() {
        for(int slot = 0; slot < patternSlot.getSlots(); slot++) {
            patternSlot.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public @Nullable IGridNode getActionableNode() {
        return managedNode != null ? managedNode.getNode() : null;
    }
}
