package io.github.dschonas04.fishr.block;

import com.mojang.serialization.MapCodec;
import io.github.dschonas04.fishr.fish.FishCatch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Fish plaque (after Fish of Thieves): a board for the wall that shows off a caught fish. Right-click with a fish to
 * hang it up, with an empty hand to read its card, sneaking with an empty hand to take it down again.
 */
public final class FishPlaqueBlock extends BaseEntityBlock {
    public static final MapCodec<FishPlaqueBlock> CODEC = simpleCodec(FishPlaqueBlock::new);
    /** The way the board faces; it hangs on the block behind. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SOUTH = Block.box(1, 3, 0, 15, 13, 1.5), NORTH = Block.box(1, 3, 14.5, 15, 13, 16),
            EAST = Block.box(0, 3, 1, 1.5, 13, 15), WEST = Block.box(14.5, 3, 1, 16, 13, 15);

    public FishPlaqueBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (face.getAxis().isVertical()) face = context.getHorizontalDirection().getOpposite();
        return defaultBlockState().setValue(FACING, face);
    }

    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }

    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case WEST -> WEST;
            default -> SOUTH;
        };
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FishPlaqueBlockEntity(pos, state); }

    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                                    InteractionHand hand, BlockHitResult hit) {
        if (FishCatch.read(stack) == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!(level.getBlockEntity(pos) instanceof FishPlaqueBlockEntity plaque)) return InteractionResult.PASS;
        if (player instanceof ServerPlayer serverPlayer) plaque.hang(serverPlayer, stack);
        return InteractionResult.SUCCESS;
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FishPlaqueBlockEntity plaque)) return InteractionResult.PASS;
        if (player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) plaque.takeDown(serverPlayer);
            else plaque.describe(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }
}
