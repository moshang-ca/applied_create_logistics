package com.moshang.appliedcreatelogistics.mechanicalProvider;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.me.GridNode;
import com.moshang.appliedcreatelogistics.AllBlockEntityTypes;
import com.moshang.appliedcreatelogistics.api.IPackagingProviderService;
import com.moshang.appliedcreatelogistics.items.LogisticsPattern.LogisticsPatternItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class MechanicalLogisticsProviderBlockEntity extends BlockEntity implements IInWorldGridNodeHost, Clearable, MenuProvider, IActionHost {
    private GridNode node;
    private final MechanicalLogisticsProviderNode nodeHolder;
    private static final int SLOT_COUNT = 9;
    private static final List<MechanicalLogisticsProviderBlockEntity> ACTIVE_INSTANCES = new ArrayList<>();

    private final ItemStackHandler patternSlot = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() instanceof EncodedPatternItem || stack.getItem() instanceof LogisticsPatternItem;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();

            if(level != null && !level.isClientSide) {
                onPatternChanged();
            }
        }
    };

    public void onPatternChanged() {
        if(getLevel() != null && getLevel().isClientSide)
            return;

        IGridNode node = getActionableNode();

        if (node != null && node.isOnline() && node.getGrid() != null) {
            node.getGrid().getCraftingService().refreshNodeCraftingProvider(node);
        }
    }

    public MechanicalLogisticsProviderBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.MECHANICAL_PROVIDER_BLOCK_ENTITY.get(), pos, state);
        this.nodeHolder = new MechanicalLogisticsProviderNode(this);
    }

    @Override
    public IGridNode getGridNode(Direction side) {
        IManagedGridNode managedNode = nodeHolder.getManagedNode(getLevel());
        return managedNode != null ? managedNode.getNode() : null;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        nodeHolder.destroy();
        ACTIVE_INSTANCES.remove(this);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        nodeHolder.destroy();
    }

    public static void onChunkLoaded(Level level, BlockPos pos, BlockState state, MechanicalLogisticsProviderBlockEntity be) {
        if (!level.isClientSide) {
            be.nodeHolder.onReady((ServerLevel) level);
        }
    }

    public void exportToCreateSystem(ItemStackHandler contents, String address) {
        if (contents == null || level == null || level.isClientSide) return;

        int itemCount = 0;
        for (int i = 0; i < contents.getSlots(); i++) {
            if (!contents.getStackInSlot(i).isEmpty()) {
                itemCount++;
            }
        }

        ItemStack packageItem = createPackageItem(contents, address, itemCount);
        if (!packageItem.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.0,
                    worldPosition.getZ() + 0.5,
                    packageItem));

        }
    }

    private ItemStack createPackageItem(ItemStackHandler contents, String address, int itemCount) {
        String packageName = "cardboard_package";
        if(itemCount <= 3) {
            packageName = "cardboard_package_10x8";
        } else if(itemCount <= 6) {
            packageName = "cardboard_package_10x12";
        } else {
            packageName = "cardboard_package_12x12";
        }

        ItemStack packageItem = new ItemStack(
                ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("create", packageName))
        );

        if (packageItem.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // 构建机械动力包裹的NBT结构
        CompoundTag compound = new CompoundTag();

        // 1. 存储所有物品内容
        compound.put("Items", contents.serializeNBT());

        // 2. 设置地址
        compound.putString("Address", address);

        // 3. 设置到包裹物品
        packageItem.setTag(compound);

        // 调试信息
        for (int i = 0; i < contents.getSlots(); i++) {
            ItemStack stack = contents.getStackInSlot(i);
        }

        return packageItem;
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

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide && !ACTIVE_INSTANCES.contains(this)) {
            ACTIVE_INSTANCES.add(this);
        }
    }

    public ItemStackHandler getItemHandler() {
        return patternSlot;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MechanicalLogisticsProviderBlockEntity be) {
        if (!level.isClientSide) {
            if (be.nodeHolder != null && be.nodeHolder.getPackagingProvider() != null) {
                be.nodeHolder.getPackagingProvider().tick();
            }
        }
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            for (MechanicalLogisticsProviderBlockEntity provider : ACTIVE_INSTANCES) {
                if (provider.getLevel() != null && !provider.getLevel().isClientSide) {
                    tick(provider.getLevel(), provider.getBlockPos(), provider.getBlockState(), provider);
                }
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if(tag.contains("PatternSlot")) {
            patternSlot.deserializeNBT(tag.getCompound("PatternSlot"));
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
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
    public IGridNode getActionableNode() {
        IManagedGridNode managedNode = nodeHolder.getManagedNode(getLevel());
        return managedNode != null ? managedNode.getNode() : null;
    }
}
