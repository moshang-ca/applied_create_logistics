package com.moshang.appliedcreatelogistics.mechanicalProvider;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.me.GridNode;
import appeng.me.helpers.MachineSource;
import com.moshang.appliedcreatelogistics.api.IPackagingProviderService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

public class MechanicalLogisticsProviderNode implements IGridNodeListener<MechanicalLogisticsProviderBlockEntity> {
    private final MechanicalLogisticsProviderBlockEntity host;
    private final IActionSource source;
    private IManagedGridNode managedNode;
    private MechanicalPackaging packagingProvider;//TODO:实现打包逻辑

    private void ensureServicesRegistered(IGridNode node) {
        if (packagingProvider == null) {
            packagingProvider = new MechanicalPackaging(host);
        }
        GridNode gridNode = (GridNode)node;

        boolean bCraftingProviderRegistered = gridNode.getService(ICraftingProvider.class) != null;
        boolean bPackagingProviderRegistered = gridNode.getService(IPackagingProviderService.class) != null;

        if(!bCraftingProviderRegistered) {
            gridNode.addService(ICraftingProvider.class, packagingProvider);
        }
        if(!bPackagingProviderRegistered) {
            gridNode.addService(IPackagingProviderService.class, packagingProvider);
        }
    }

    private void ensurePackagingProvider(IGridNode node) {
        if(packagingProvider == null) {
            packagingProvider = new MechanicalPackaging(host);
            GridNode gridNode = (GridNode)node;
            gridNode.addService(IPackagingProviderService.class, packagingProvider);
            gridNode.addService(ICraftingProvider.class, packagingProvider);
        }
    }

    public MechanicalLogisticsProviderNode(MechanicalLogisticsProviderBlockEntity host) {
        this.host = host;
        this.source = new MachineSource((IActionHost) host);
    }

    @Override
    public void onSaveChanges(MechanicalLogisticsProviderBlockEntity blockEntity, IGridNode node) {
        blockEntity.setChanged();
    }

    @Override
    public void onGridChanged(MechanicalLogisticsProviderBlockEntity nodeOwner, IGridNode node) {
        if(node.isOnline()) {
            ensureServicesRegistered(node);
            ensurePackagingProvider(node); //TODO:注册provider服务
        }else {
            packagingProvider = null;
        }
    }

    public void onReady(ServerLevel level) {
        IManagedGridNode node = getManagedNode(level);
        if (node != null) {
            node.create(level, host.getBlockPos());
            ensurePackagingProvider((GridNode) node.getNode());
        }
    }

    private void ensurePackagingProvider(GridNode node) {
        if(packagingProvider == null) {
            packagingProvider = new MechanicalPackaging(host);

        }
    }

    public IManagedGridNode getManagedNode(Level level) {
        if(managedNode == null && level != null && !level.isClientSide) {
            managedNode = appeng.api.networking.GridHelper.createManagedNode(
                    host, this
            );
            managedNode.setIdlePowerUsage(10.0);
            managedNode.setExposedOnSides(EnumSet.allOf(Direction.class));
            managedNode.setVisualRepresentation(host.getBlockState().getBlock());
            managedNode.setInWorldNode(true);
            managedNode.setGridColor(appeng.api.util.AEColor.TRANSPARENT);
            managedNode.setFlags(GridFlags.REQUIRE_CHANNEL);

            //packagingProvider = new MechanicalPackaging(host);
            if (level instanceof ServerLevel serverLevel) {
                managedNode.create(serverLevel, host.getBlockPos());

                ensureServicesRegistered(managedNode.getNode());
            }
        }
        return managedNode;
    }

    public IActionSource getActionSource() {
        return source;
    }

    public MechanicalPackaging getPackagingProvider() {
        return packagingProvider;
    }

    public boolean isActive() {
        return managedNode != null && managedNode.getNode().isOnline();
    }

    public void destroy() {
        if (managedNode != null) {
            managedNode.destroy();
            managedNode = null;
        }
    }

    public AECableType getCableConnectionType(Direction side) {
        return AECableType.SMART;
    }

    public DimensionalBlockPos getLocation() {
        Level level = host.getLevel();
        BlockPos pos = host.getBlockPos();
        return new DimensionalBlockPos(level, pos);
    }
}
