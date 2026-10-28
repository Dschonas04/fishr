package io.github.dschonas04.fishr.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.dschonas04.fishr.Fishr;
import io.github.dschonas04.fishr.fish.FishrFishEntity;
import io.github.dschonas04.fishr.fish.FishrFishRegistry;
import io.github.dschonas04.fishr.fish.FishSpecies;
import io.github.dschonas04.fishr.fish.FishVariant;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class FishrFishRenderer extends MobRenderer<FishrFishEntity, FishrFishRenderer.State, FishrFishModel> {
    /** The fish's state for drawing, with its colouring. */
    public static final class State extends LivingEntityRenderState {
        FishVariant variant = FishVariant.NORMAL;
    }

    private final Identifier texture, paleTexture;
    private final boolean flat;

    private FishrFishRenderer(EntityRendererProvider.Context context, FishSpecies species, ModelLayerLocation layer) {
        super(context, new FishrFishModel(context.bakeLayer(layer), species), 0.2f);
        texture = Fishr.id("textures/entity/fish/" + species.id() + ".png");
        paleTexture = Fishr.id("textures/entity/fish/" + species.id() + "_pale.png");
        flat = species.flat();
    }

    public static void register() {
        for (FishSpecies species : FishSpecies.values()) {
            var layer = new ModelLayerLocation(Fishr.id(species.id()), "main");
            ModelLayerRegistry.registerModelLayer(layer, () -> FishrFishModel.layer(species));
            EntityRendererRegistry.register(FishrFishRegistry.entity(species), context -> new FishrFishRenderer(context, species, layer));
        }
    }

    @Override public Identifier getTextureLocation(State state) { return state.variant.pale() ? paleTexture : texture; }
    @Override public State createRenderState() { return new State(); }

    @Override public void extractRenderState(FishrFishEntity fish, State state, float partialTicks) {
        super.extractRenderState(fish, state, partialTicks);
        state.variant = fish.variant();
    }

    /** Rare colourings: the skin (the pale one for the bright colourings) tinted. */
    @Override protected int getModelTint(State state) { return state.variant.tint(); }

    @Override protected void setupRotations(State state, PoseStack poses, float rotation, float scale) {
        super.setupRotations(state, poses, rotation, scale);
        poses.mulPose(Axis.YP.rotationDegrees(4.3f * Mth.sin(0.6f * state.ageInTicks)));
        // stranded fish lie on their side; a flatfish already does
        if (!state.isInWater && !flat) {
            poses.translate(0.1f, 0.1f, -0.1f);
            poses.mulPose(Axis.ZP.rotationDegrees(90));
        }
    }
}
