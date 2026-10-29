package io.github.dschonas04.fishr.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import io.github.dschonas04.fishr.block.FishPlaqueBlock;
import io.github.dschonas04.fishr.block.FishPlaqueBlockEntity;
import io.github.dschonas04.fishr.fish.FishCatch;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Zeichnet den Fisch auf eine Fischtafel: flach auf dem Brett wie im Rahmen, je größer der Fisch,
 * desto größer.
 */
public final class FishPlaqueRenderer
        implements BlockEntityRenderer<FishPlaqueBlockEntity, FishPlaqueRenderer.State> {
    public static final class State extends BlockEntityRenderState {
        final ItemStackRenderState item = new ItemStackRenderState();
        boolean empty = true;
        Direction facing = Direction.SOUTH;
        float scale = 1.2f;
    }

    private final ItemModelResolver items;

    public FishPlaqueRenderer(BlockEntityRendererProvider.Context context) {
        items = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
            FishPlaqueBlockEntity plaque,
            State state,
            float partialTicks,
            Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(plaque, state, partialTicks, camera, breaking);
        state.facing = plaque.getBlockState().getValue(FishPlaqueBlock.FACING);
        ItemStack fish = plaque.fish();
        state.empty = fish.isEmpty();
        if (state.empty) return;
        items.updateForTopItem(
                state.item,
                fish,
                ItemDisplayContext.FIXED,
                plaque.getLevel(),
                null,
                (int) plaque.getBlockPos().asLong());
        FishCatch.Catch caught = FishCatch.read(fish);
        state.scale =
                caught == null ? 1.2f : (float) (1.1 + 0.6 * caught.specimen().sizeFraction());
    }

    @Override
    public void submit(
            State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.empty) return;
        poses.pushPose();
        // vor dem Brett, das an der Seite gegenüber der Blickrichtung hängt
        poses.translate(
                0.5 - state.facing.getStepX() * 0.36, 0.5, 0.5 - state.facing.getStepZ() * 0.36);
        poses.rotate(Axis.YP.rotationDegrees(180 - state.facing.toYRot()));
        poses.scale(state.scale, state.scale, state.scale);
        state.item.submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poses.popPose();
    }
}
