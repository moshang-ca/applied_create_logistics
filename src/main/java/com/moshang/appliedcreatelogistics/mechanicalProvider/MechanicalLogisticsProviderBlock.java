package com.moshang.appliedcreatelogistics.mechanicalProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class MechanicalLogisticsProviderBlock extends Block implements EntityBlock {

    public MechanicalLogisticsProviderBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(4.f)
        );
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MechanicalLogisticsProviderBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MechanicalLogisticsProviderBlockEntity provider) {
                NetworkHooks.openScreen((ServerPlayer) player, provider, pos);
            } else {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.SUCCESS;
    }
}
