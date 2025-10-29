package com.moshang.appliedcreatelogistics.blocks.mechanicalProvider;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.me.GridNode;
import com.moshang.appliedcreatelogistics.AllBlockEntityTypes;
import com.moshang.appliedcreatelogistics.items.LogisticsPattern.*;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;

public class MechanicalLogisticsProviderBlockEntity extends BlockEntity implements IInWorldGridNodeHost, Clearable, MenuProvider, IActionHost {
    private GridNode node;
    private final MechanicalLogisticsProviderNode nodeHolder;
    private Direction outputDirection  = null;
    private LogisticsNetworkHandler networkHandler;
    private final List<LogisticsPatternDetails> registeredPatterns = new ArrayList<>();
    private boolean isGridConnected = false;
    private static final int SLOT_COUNT = 9;
    private static final List<MechanicalLogisticsProviderBlockEntity> ACTIVE_INSTANCES = new ArrayList<>();

    private final ItemStackHandler patternSlot = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return stack.getItem() instanceof LogisticsPatternItem;
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


    @Override
    public IGridNode getGridNode(Direction side) {
        IManagedGridNode managedNode = nodeHolder.getManagedNode(getLevel());
        return managedNode != null ? managedNode.getNode() : null;
    }

    @Override
    public void setRemoved() {
        unregisterAllPatterns();

        super.setRemoved();
        nodeHolder.destroy();
        ACTIVE_INSTANCES.remove(this);
    }

    @Override
    public void onChunkUnloaded() {
        unregisterAllPatterns();

        super.onChunkUnloaded();
        nodeHolder.destroy();
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

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if(tag.contains("PatternSlot")) {
            patternSlot.deserializeNBT(tag.getCompound("PatternSlot"));
        }

        if(tag.contains("OutputDirection")) {
            this.outputDirection = Direction.valueOf(tag.getString("OutputDirection"));
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        System.out.println("🔧 物流供应器方块实体加载: " + getBlockPos());

        // 确保节点初始化
        if (level != null && !level.isClientSide) {
            IManagedGridNode node = nodeHolder.getManagedNode(level);
            if (node != null) {
                System.out.println("✅ 节点初始化完成");
            } else {
                System.out.println("❌ 节点初始化失败");
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("PatternSlot", patternSlot.serializeNBT());

        if(this.outputDirection != null) {
            tag.putString("OutputDirection", this.outputDirection.name());
        }
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

        Direction exportDirection = this.outputDirection;
        if(exportDirection == null) {
            for (Direction direction : Direction.values()) {
                if (tryExportToAdjacentContainer(packageItem, direction)) {
                    return;
                }
            }
        } else {
            if (tryExportToAdjacentContainer(packageItem, exportDirection)) {
                return;
            }
        }

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

    public void onPatternChanged() {
        if(getLevel() != null && getLevel().isClientSide)
            return;

        unregisterAllPatterns();
        registerAllPatterns();

        IGridNode node = getActionableNode();
        if (node != null && node.isOnline() && node.getGrid() != null) {
            node.getGrid().getCraftingService().refreshNodeCraftingProvider(node);
        }
    }

    public MechanicalLogisticsProviderBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.MECHANICAL_PROVIDER_BLOCK_ENTITY.get(), pos, state);
        this.nodeHolder = new MechanicalLogisticsProviderNode(this);
    }

    public void setOutputDirection(Direction direction) {
        this.outputDirection = direction;
        setChanged();
    }

    public Direction getOutputDirection() {
        return outputDirection;
    }
/*
    public void onGridConnected(IGridNode node) {
        this.isGridConnected = true;
        this.networkHandler = new LogisticsNetworkHandler(node);

        registerAllPatterns();

        System.out.println("物流供应器连接到网格，注册了 " + registeredPatterns.size() + " 个样板");
    }
*/

    public void onGridConnected(IGridNode node) {
        System.out.println("🌐 网格连接事件触发");

        if (node == null || !node.isOnline()) {
            System.out.println("❌ 节点无效或不在线");
            return;
        }

        this.isGridConnected = true;
        this.networkHandler = new LogisticsNetworkHandler(node);

        System.out.println("✅ 网络处理器创建完成");
        registerAllPatterns();

        System.out.println("🎯 物流供应器成功连接到网格，注册了 " + registeredPatterns.size() + " 个样板");
    }
    public void onGridDisconnected() {
        this.isGridConnected = false;

        unregisterAllPatterns();
        this.networkHandler = null;

        System.out.println("物流供应器从网格断开，已取消注册所有样板");
    }

    public Set<String> getManagedAddresses() {
        Set<String> addresses = new HashSet<>();
        for(LogisticsPatternDetails pattern : registeredPatterns) {
            addresses.add(pattern.getDefaultAddress());
        }
        return addresses;
    }

    public List<LogisticsPatternDetails> getPatternsByAddress(String address) {
        List<LogisticsPatternDetails> patterns = new ArrayList<>();
        for(LogisticsPatternDetails pattern : registeredPatterns) {
            if(pattern.getDefaultAddress().equals(address)) {
                patterns.add(pattern);
            }
        }
        return patterns;
    }

    public boolean managesAddress(String address) {
        for(LogisticsPatternDetails pattern : registeredPatterns) {
            if(pattern.getDefaultAddress().equals(address)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, List<ItemStack>> getAddressPatternMap() {
        Map<String, List<ItemStack>> addressMap = new HashMap<>();
        for(LogisticsPatternDetails pattern : registeredPatterns) {
            String address = pattern.getDefaultAddress();
            addressMap.computeIfAbsent(address, k -> new ArrayList<>())
                    .add(pattern.getPatternStack());
        }
        return addressMap;
    }

    private boolean tryExportToAdjacentContainer(ItemStack packageItem, Direction direction) {
        BlockPos targetPos = worldPosition.relative(direction);
        BlockEntity targetBe = level.getBlockEntity(targetPos);

        if(targetBe == null) {
            return false;
        }

        LazyOptional<IItemHandler> capability = targetBe.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite());
        if(capability.isPresent()) {
            IItemHandler handler = capability.orElse(null);
            if(handler != null) {
                if(hasSpaceForItem(handler, packageItem)) {
                    ItemStack remainder = ItemHandlerHelper.insertItem(handler, packageItem, false);
                    if(remainder.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasSpaceForItem(IItemHandler handler, ItemStack stack) {
        for(int i = 0; i < handler.getSlots(); i++) {
            if(handler.insertItem(i, stack, true).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void registerAllPatterns() {
        if(this.networkHandler == null || !isGridConnected) return;

        registeredPatterns.clear();

        for(int i = 0; i < patternSlot.getSlots(); i++) {
            ItemStack patternStack = patternSlot.getStackInSlot(i);
            if(!patternStack.isEmpty() && patternStack.getItem() instanceof LogisticsPatternItem) {
                LogisticsPatternDetails pattern = new LogisticsPatternDetails(patternStack);
                this.networkHandler.registerPattern(pattern);
                this.registeredPatterns.add(pattern);

                String address = pattern.getDefaultAddress();
                LogisticsAddressManager.registerPatternToAddress(address, patternStack);
            }
        }
    }

    private void unregisterAllPatterns() {
        if(this.networkHandler == null) return;

        for(LogisticsPatternDetails pattern : this.registeredPatterns) {
            this.networkHandler.unregisterPattern(pattern);

            String address = pattern.getDefaultAddress();
            ItemStack patternStack = pattern.getPatternStack();
            LogisticsAddressManager.unregisterPatternFromAddress(address, patternStack);
        }

        this.registeredPatterns.clear();
    }
}
