package io.github.dschonas04.fishr.client;

import io.github.dschonas04.fishr.fish.FishSpecies;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/**
 * Artspezifische Geometrie in einem 64x64-Atlas. Der Körper besteht aus fünf Segmenten, die hinter
 * dem Kopf breiter werden und zur Schwanzwurzel hin schmaler, damit der Fisch eine echte
 * Spindelform hat; die letzten beiden Segmente und die gegabelte Schwanzflosse schwingen beim
 * Schwimmen mit. Rücken-, After-, Bauch- und Brustflossen sitzen an ihrem Platz. {@code
 * scripts/draw-textures.py} malt dieselben Segmente ({@link #SEGMENTS}, {@link #profile}) in die
 * Haut, jedes mit seinem eigenen Abschnitt des Musters.
 */
public final class FishrFishModel extends EntityModel<LivingEntityRenderState> {
    public static final int SEGMENTS = 5;
    private final ModelPart rear, tail, leftFin, rightFin;
    private final boolean flat;

    public FishrFishModel(ModelPart root, FishSpecies species) {
        super(root);
        flat = species.flat();
        rear = root.getChild("rear");
        tail = rear.getChild("tail");
        leftFin = root.getChild("left_fin");
        rightFin = root.getChild("right_fin");
    }

    /**
     * {Breitenfaktoren, Höhenfaktoren} der Segmente vom Kopf zum Schwanz. Im Gleichschritt mit
     * draw-textures.py halten.
     */
    static float[][] profile(FishSpecies species) {
        if (species.flat())
            return new float[][] {{0.8f, 1f, 1f, 0.85f, 0.55f}, {1f, 1f, 1f, 0.9f, 0.8f}};
        if (species.modelBodyLength() > species.modelHeight() * 5) {
            return new float[][] {{1f, 1f, 0.95f, 0.85f, 0.7f}, {1f, 1f, 0.95f, 0.85f, 0.7f}};
        }
        return new float[][] {{1f, 0.95f, 0.85f, 0.7f, 0.5f}, {1f, 1f, 0.88f, 0.7f, 0.5f}};
    }

    /** Wo Segment {@code index} im Atlas liegt: drei in der ersten Reihe, zwei in der zweiten. */
    static int[] texture(FishSpecies species, int index) {
        float depth = species.modelBodyLength() / SEGMENTS;
        int column = (int) Math.ceil(2 * depth + 2 * species.modelWidth()) + 1;
        return new int[] {(index % 3) * column, index < 3 ? 0 : 10};
    }

    public static LayerDefinition layer(FishSpecies species) {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        float width = species.modelWidth();
        float height = species.modelHeight();
        float length = species.modelBodyLength();
        float depth = length / SEGMENTS;
        float[][] profile = profile(species);
        boolean flat = species.flat();
        PartDefinition rear =
                root.addOrReplaceChild(
                        "rear", CubeListBuilder.create(), PartPose.offset(0, 22, 3 * depth));
        for (int k = 0; k < SEGMENTS; k++) {
            float w = width * profile[0][k], h = height * profile[1][k];
            int[] uv = texture(species, k);
            var cube = CubeListBuilder.create().texOffs(uv[0], uv[1]);
            if (k < 3)
                root.addOrReplaceChild(
                        "body_" + k,
                        cube.addBox(-w / 2, -h / 2, k * depth, w, h, depth),
                        PartPose.offset(0, 22, 0));
            else
                rear.addOrReplaceChild(
                        "body_" + k,
                        cube.addBox(-w / 2, -h / 2, (k - 3) * depth, w, h, depth),
                        PartPose.ZERO);
        }
        // der Kopf wird zum Maul hin etwas schmaler
        float headW = width * (flat ? 0.75f : 0.9f), headH = height * (flat ? 1f : 0.88f);
        root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 20)
                        .addBox(-headW / 2, -headH / 2, -3, headW, headH, 3),
                PartPose.offset(0, 22, 0));
        float nose = species.modelNoseLength();
        if (nose > 4) {
            // das Schwert des Schwertfisches: lang und dünn
            root.addOrReplaceChild(
                    "snout",
                    CubeListBuilder.create()
                            .texOffs(0, 32)
                            .addBox(-0.35f, -0.35f, -nose, 0.7f, 0.7f, nose),
                    PartPose.offset(0, 22, -3));
        } else {
            float snoutW = headW * 0.7f, snoutH = headH * 0.55f;
            root.addOrReplaceChild(
                    "snout",
                    CubeListBuilder.create()
                            .texOffs(0, 32)
                            .addBox(-snoutW / 2, -snoutH / 2, -nose, snoutW, snoutH, nose),
                    PartPose.offset(0, 22.2f, -3));
        }
        // der Schwanz: eine gegabelte Flosse aus zwei Lappen an der Schwanzwurzel; Plattfische
        // liegen auf
        // der Seite, ihrer steht deshalb waagrecht
        PartDefinition tail =
                rear.addOrReplaceChild(
                        "tail", CubeListBuilder.create(), PartPose.offset(0, 0, 2 * depth));
        float tailLength = species.modelTailLength();
        float spread = Math.max(1.2f, height * 0.35f);
        for (int side : new int[] {-1, 1}) {
            var lobe = CubeListBuilder.create().texOffs(0, 49);
            if (flat) {
                tail.addOrReplaceChild(
                        "lobe_" + side,
                        lobe.addBox(side < 0 ? -spread : 0, 0, 0, spread, 0, tailLength),
                        PartPose.rotation(0, side * 0.35f, 0));
            } else {
                tail.addOrReplaceChild(
                        "lobe_" + side,
                        lobe.addBox(0, side < 0 ? -spread : 0, 0, 0, spread, tailLength),
                        PartPose.rotation(side * -0.35f, 0, 0));
            }
        }
        // Brustflossen hinter den Kiemen
        root.addOrReplaceChild(
                "left_fin",
                CubeListBuilder.create().texOffs(16, 50).addBox(0, 0, -1, 2.5f, 0, 2.5f),
                PartPose.offsetAndRotation(width / 2, 22 + height * 0.2f, 0.8f, 0, 0.3f, 0.5f));
        root.addOrReplaceChild(
                "right_fin",
                CubeListBuilder.create().texOffs(16, 50).addBox(-2.5f, 0, -1, 2.5f, 0, 2.5f),
                PartPose.offsetAndRotation(-width / 2, 22 + height * 0.2f, 0.8f, 0, -0.3f, -0.5f));
        if (flat) {
            // ein Flossensaum an beiden Kanten statt einer Rückenflosse
            for (int side : new int[] {-1, 1}) {
                root.addOrReplaceChild(
                        "fringe_" + side,
                        CubeListBuilder.create()
                                .texOffs(28, 50)
                                .addBox(side < 0 ? -1 : 0, 0, 0, 1, 0, length * 0.85f),
                        PartPose.offset(side * width / 2, 22, length * 0.08f));
            }
            // beide Augen auf der Oberseite
            for (int side : new int[] {-1, 1}) {
                root.addOrReplaceChild(
                        "eye_" + side,
                        CubeListBuilder.create()
                                .texOffs(55, 60)
                                .addBox(-0.4f, -0.4f, -0.4f, 0.8f, 0.8f, 0.8f),
                        PartPose.offset(side * 0.6f, 22 - headH / 2 - 0.3f, -1.6f));
                root.addOrReplaceChild(
                        "pupil_" + side,
                        CubeListBuilder.create()
                                .texOffs(61, 60)
                                .addBox(-0.23f, -0.23f, -0.23f, 0.46f, 0.46f, 0.46f),
                        PartPose.offset(side * 0.6f, 22 - headH / 2 - 0.6f, -1.65f));
            }
        } else {
            // Rückenflosse auf dem höchsten Punkt des Rückens, nach hinten geneigt; After- und
            // Bauchflossen
            // darunter
            float dorsal = Math.max(1.2f, height * 0.45f);
            root.addOrReplaceChild(
                    "dorsal",
                    CubeListBuilder.create()
                            .texOffs(28, 50)
                            .addBox(0, -dorsal, 0, 0, dorsal, Math.max(2, length * 0.3f)),
                    PartPose.offsetAndRotation(
                            0, 22 - height / 2 + 0.1f, length * 0.2f, -0.12f, 0, 0));
            rear.addOrReplaceChild(
                    "anal",
                    CubeListBuilder.create()
                            .texOffs(28, 56)
                            .addBox(
                                    0,
                                    0,
                                    0,
                                    0,
                                    Math.max(0.8f, height * 0.25f),
                                    Math.max(1.5f, depth)),
                    PartPose.offsetAndRotation(
                            0, height * profile[1][3] / 2 - 0.1f, 0.2f, 0.25f, 0, 0));
            for (int side : new int[] {-1, 1}) {
                root.addOrReplaceChild(
                        "pelvic_" + side,
                        CubeListBuilder.create().texOffs(40, 50).addBox(0, 0, 0, 0, 1.2f, 1.8f),
                        PartPose.offsetAndRotation(
                                side * width * 0.25f,
                                22 + height / 2 - 0.1f,
                                length * 0.35f,
                                0.5f,
                                0,
                                side * 0.3f));
                root.addOrReplaceChild(
                        "eye_" + side,
                        CubeListBuilder.create()
                                .texOffs(55, 60)
                                .addBox(-0.4f, -0.4f, -0.4f, 0.8f, 0.8f, 0.8f),
                        PartPose.offset(side * headW / 2, 22 - headH * 0.15f, -2));
                root.addOrReplaceChild(
                        "pupil_" + side,
                        CubeListBuilder.create()
                                .texOffs(61, 60)
                                .addBox(-0.23f, -0.23f, -0.23f, 0.46f, 0.46f, 0.46f),
                        PartPose.offset(side * (headW / 2 + 0.3f), 22 - headH * 0.15f, -2.05f));
            }
        }
        if (species.hasBarbels()) {
            for (int side : new int[] {-1, 1}) {
                root.addOrReplaceChild(
                        "barbel_" + side,
                        CubeListBuilder.create()
                                .texOffs(32, 50)
                                .addBox(-0.15f, 0, -3.5f, 0.3f, 0.3f, 3.5f),
                        PartPose.offsetAndRotation(
                                side * headW / 2, 22.5f, -2.5f, 0.2f, side * -0.6f, 0));
            }
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float speed = state.isInWater ? 0.6f : 1.0f;
        float beat = Mth.sin(state.ageInTicks * speed);
        // die Welle läuft den Körper entlang: das Hinterteil schwingt ein wenig, der Schwanz folgt
        // einen
        // Augenblick später und weiter
        float late = Mth.sin(state.ageInTicks * speed - 0.9f);
        float amount = state.isInWater ? 1f : 1.6f;
        if (flat) {
            rear.xRot = beat * 0.12f * amount;
            tail.xRot = late * 0.3f * amount;
        } else {
            rear.yRot = beat * 0.15f * amount;
            tail.yRot = late * 0.4f * amount;
        }
        leftFin.yRot = 0.3f + beat * 0.25f;
        rightFin.yRot = -0.3f - beat * 0.25f;
    }
}
