package com.moshang.appliedcreatelogistics;

import appeng.core.sync.BasePacket;
import com.moshang.appliedcreatelogistics.api.ILogisticsPatternMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetLogisticsModePacket extends BasePacket {
    private final int containerId;
    private final boolean logisticsMode;
    private final String address;

    public SetLogisticsModePacket(int containerId, boolean logisticsMode, String address) {
        this.containerId = containerId;
        this.logisticsMode = logisticsMode;
        this.address = address;
    }

    public SetLogisticsModePacket(FriendlyByteBuf buf) {
        this.containerId = buf.readInt();
        this.logisticsMode = buf.readBoolean();
        this.address = buf.readUtf(16);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeBoolean(logisticsMode);
        buf.writeUtf(address, 16);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null && player.containerMenu.containerId == containerId) {
                if (player.containerMenu instanceof ILogisticsPatternMenu menu) {
                    menu.appliedCreateLogistics$setLogisticsMode(logisticsMode, address);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}

