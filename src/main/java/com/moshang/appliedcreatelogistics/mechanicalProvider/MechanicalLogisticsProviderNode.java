package com.moshang.appliedcreatelogistics.mechanicalProvider;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
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

    public MechanicalLogisticsProviderNode(MechanicalLogisticsProviderBlockEntity host) {
        this.host = host;
        this.source = new MachineSource((IActionHost) host);
    }

    @Override
    public void onSaveChanges(MechanicalLogisticsProviderBlockEntity mechanicalLogisticsProviderBlockEntity, IGridNode iGridNode) {

    }

    @Override
    public void onGridChanged(MechanicalLogisticsProviderBlockEntity nodeOwner, IGridNode node) {
        if(node.isOnline()) {
            ensurePackagingProvider((GridNode) node); //TODO:注册provider服务
        }else {
            packagingProvider = null;
        }
    }

    public void onReady(GridNode node, ServerLevel level) {
        ensurePackagingProvider(node);
    }

    private void ensurePackagingProvider(GridNode node) {
        if(packagingProvider == null) {
            packagingProvider = new MechanicalPackaging(host);
            node.addService(IPackagingProviderService.class, packagingProvider);
        }
    }

    public IManagedGridNode getManagedNode(Level level) {
        if(managedNode == null && level != null && !level.isClientSide) {
            managedNode = appeng.api.networking.GridHelper.createManagedNode(
                    host, this
            );
            managedNode.setIdlePowerUsage(10.f);
            managedNode.setExposedOnSides(EnumSet.allOf(Direction.class));
            managedNode.setVisualRepresentation(host.getBlockState().getBlock());
        }
        return managedNode;
    }

    public IActionSource getActionSource() {
        return source;
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
