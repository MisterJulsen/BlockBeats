package de.mrjulsen.blockbeats.block;

import org.jetbrains.annotations.Nullable;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.block.entity.SoundPlayerBlockEntity;
import de.mrjulsen.blockbeats.block.entity.TickableBlockEntity;
import de.mrjulsen.blockbeats.client.ClientWrapper;
import de.mrjulsen.blockbeats.core.data.ERedstoneMode;
import de.mrjulsen.blockbeats.registry.ModBlockEntities;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SoundPlayerBlock extends BaseEntityBlock {

    private static final MutableComponent textInaccessible = TextUtils.translate("block." + BlockBeats.MOD_ID + ".sound_player.locked").withStyle(ChatFormatting.RED);

    public SoundPlayerBlock() {
        super(Properties.copy(Blocks.JUKEBOX));
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.getBlockEntity(blockPos) instanceof SoundPlayerBlockEntity be) {            
            if (level.isClientSide && !player.isShiftKeyDown()) {
                if (be.isAccessible(player)) {
                    ClientWrapper.openPlaylistScreen(be);
                } else {
                    player.displayClientMessage(textInaccessible, true);
                }
            } else if (!level.isClientSide && ((!be.isRunning() && be.getRedstone() == ERedstoneMode.NO_REDSTONE && player.isShiftKeyDown()) || (be.isRunning() && be.getRedstone() != ERedstoneMode.REDSTONE && player.isShiftKeyDown()))) {
                be.setRunning(!be.isRunning());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (level.getBlockEntity(blockPos) instanceof SoundPlayerBlockEntity be) {            
            if (!level.isClientSide) {
                be.stop();
            }
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        if (level.getBlockEntity(blockPos) instanceof SoundPlayerBlockEntity be) {            
            if (!level.isClientSide) {
                be.setOwner(livingEntity.getUUID());
                be.notifyUpdate();
            }
        }
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
    }
    
    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (level.isClientSide) {
            return;
        }

        if (level.getBlockEntity(blockPos) instanceof SoundPlayerBlockEntity be) {
            if (level.hasNeighborSignal(blockPos)) {
                if (!be.isPowered()) {
                    be.setPowered(true);
                }
            } else {
                if (be.isPowered()) {
                    be.setPowered(false);
                }
            }
            be.notifyUpdate();
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SoundPlayerBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, ModBlockEntities.SOUND_PLAYER_BLOCK_ENTITY.get(), TickableBlockEntity::globalTick);
    }
    
}
