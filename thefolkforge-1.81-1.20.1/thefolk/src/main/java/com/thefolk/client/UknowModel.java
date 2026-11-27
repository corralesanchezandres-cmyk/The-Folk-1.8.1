package com.thefolk.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thefolk.TheFolkMod;
import com.thefolk.entity.FolkMonster;
import com.thefolk.entity.UknowEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Criatura escuálida a cuatro patas: brazos larguísimos con los codos por encima del lomo,
 * patas traseras dobladas, cabeza en cuña con una boca enorme llena de dientes y dos cuernos
 * largos y finos. Todo cuelga del torso.
 */
public class UknowModel extends EntityModel<UknowEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(TheFolkMod.MOD_ID, "uknow"), "main");

    private static final float RAD = (float) Math.PI / 180.0F;

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart rightHorn;
    private final ModelPart leftHorn;
    private final ModelPart frontRightArm;
    private final ModelPart frontLeftArm;
    private final ModelPart frontRightForearm;
    private final ModelPart frontLeftForearm;
    private final ModelPart backRightThigh;
    private final ModelPart backLeftThigh;
    private final ModelPart backRightShin;
    private final ModelPart backLeftShin;

    public UknowModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.rightHorn = this.head.getChild("right_horn");
        this.leftHorn = this.head.getChild("left_horn");
        this.frontRightArm = this.body.getChild("front_right_arm");
        this.frontLeftArm = this.body.getChild("front_left_arm");
        this.frontRightForearm = this.frontRightArm.getChild("front_right_forearm");
        this.frontLeftForearm = this.frontLeftArm.getChild("front_left_forearm");
        this.backRightThigh = this.body.getChild("back_right_thigh");
        this.backLeftThigh = this.body.getChild("back_left_thigh");
        this.backRightShin = this.backRightThigh.getChild("back_right_shin");
        this.backLeftShin = this.backLeftThigh.getChild("back_left_shin");
    }

    private static CubeListBuilder arm() {
        return CubeListBuilder.create().texOffs(64, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F);
    }

    /** Antebrazo largo que termina en tres dedos de garra. */
    private static CubeListBuilder forearm() {
        return CubeListBuilder.create().texOffs(72, 0)
                .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 18.0F, 2.0F)
                .texOffs(80, 0)
                .addBox(-1.5F, 18.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .addBox(-0.5F, 18.0F, -0.5F, 1.0F, 9.0F, 1.0F)
                .addBox(0.5F, 18.0F, -0.5F, 1.0F, 9.0F, 1.0F);
    }

    private static CubeListBuilder thigh() {
        return CubeListBuilder.create().texOffs(84, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 9.0F, 3.0F);
    }

    private static CubeListBuilder shin() {
        return CubeListBuilder.create().texOffs(96, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();

        // Torso flaco con tres púas en el lomo (pivote en el centro del torso)
        PartDefinition body = r.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3.5F, -3.5F, -8.0F, 7.0F, 7.0F, 16.0F)
                        .texOffs(0, 54)
                        .addBox(-1.0F, -6.0F, -6.0F, 2.0F, 3.0F, 3.0F)
                        .addBox(-1.0F, -6.0F, -1.5F, 2.0F, 3.0F, 3.0F)
                        .addBox(-1.0F, -6.0F, 3.0F, 2.0F, 3.0F, 3.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));

        // Cabeza en cuña con dientes colgando del paladar
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 24)
                        .addBox(-4.0F, -3.0F, -10.0F, 8.0F, 6.0F, 10.0F)
                        .texOffs(120, 0)
                        .addBox(-3.0F, 3.0F, -9.0F, 1.0F, 2.0F, 1.0F)
                        .addBox(2.0F, 3.0F, -9.0F, 1.0F, 2.0F, 1.0F)
                        .addBox(-3.0F, 3.0F, -6.0F, 1.0F, 2.0F, 1.0F)
                        .addBox(2.0F, 3.0F, -6.0F, 1.0F, 2.0F, 1.0F)
                        .addBox(-3.0F, 3.0F, -3.0F, 1.0F, 2.0F, 1.0F)
                        .addBox(2.0F, 3.0F, -3.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -1.0F, -8.0F));

        // Mandíbula articulada con dientes hacia arriba y el interior rojo
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(0, 40)
                        .addBox(-3.5F, 0.0F, -9.0F, 7.0F, 3.0F, 10.0F)
                        .texOffs(120, 0)
                        .addBox(-3.0F, -2.0F, -8.0F, 1.0F, 2.0F, 1.0F)
                        .addBox(2.0F, -2.0F, -8.0F, 1.0F, 2.0F, 1.0F)
                        .addBox(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F)
                        .addBox(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F)
                        .addBox(-3.0F, -2.0F, -2.0F, 1.0F, 2.0F, 1.0F)
                        .addBox(2.0F, -2.0F, -2.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 3.0F, -1.0F));

        // Dos cuernos largos y finos: uno casi recto y otro echado hacia atrás
        PartDefinition rightHorn = head.addOrReplaceChild("right_horn",
                CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                PartPose.offsetAndRotation(-3.0F, -3.0F, -2.0F, -0.6F, 0.0F, -0.5F));
        rightHorn.addOrReplaceChild("right_horn_tip",
                CubeListBuilder.create().texOffs(56, 0).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.3F, 0.0F, -0.3F));

        PartDefinition leftHorn = head.addOrReplaceChild("left_horn",
                CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                PartPose.offsetAndRotation(3.0F, -3.0F, -2.0F, -0.15F, 0.0F, 0.15F));
        leftHorn.addOrReplaceChild("left_horn_tip",
                CubeListBuilder.create().texOffs(56, 0).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, -0.2F, 0.0F, 0.1F));

        // Brazos: el codo sube por encima del lomo y el antebrazo baja hacia delante
        PartDefinition rightArm = body.addOrReplaceChild("front_right_arm", arm(),
                PartPose.offsetAndRotation(-5.0F, 1.0F, -5.0F, 2.0F, 0.0F, -0.15F));
        rightArm.addOrReplaceChild("front_right_forearm", forearm(),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, -2.8F, 0.0F, 0.0F));

        PartDefinition leftArm = body.addOrReplaceChild("front_left_arm", arm(),
                PartPose.offsetAndRotation(5.0F, 1.0F, -5.0F, 2.0F, 0.0F, 0.15F));
        leftArm.addOrReplaceChild("front_left_forearm", forearm(),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, -2.8F, 0.0F, 0.0F));

        // Patas traseras dobladas hacia atrás
        PartDefinition rightThigh = body.addOrReplaceChild("back_right_thigh", thigh(),
                PartPose.offsetAndRotation(-4.5F, 1.0F, 6.0F, 0.7F, 0.0F, 0.0F));
        rightThigh.addOrReplaceChild("back_right_shin", shin(),
                PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, -1.4F, 0.0F, 0.0F));

        PartDefinition leftThigh = body.addOrReplaceChild("back_left_thigh", thigh(),
                PartPose.offsetAndRotation(4.5F, 1.0F, 6.0F, 0.7F, 0.0F, 0.0F));
        leftThigh.addOrReplaceChild("back_left_shin", shin(),
                PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, -1.4F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(UknowEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        int state = entity.getUknowState();
        int status = entity.getStatus();
        float cycle = limbSwing * 0.6662F;
        float gait = Mth.cos(cycle) * limbSwingAmount;     // de -1 a 1 mientras camina
        float breathe = Mth.sin(ageInTicks * 0.1F);
        float hornSway = Mth.sin(ageInTicks * 0.07F) * 0.04F;

        // --- Postura base: encorvado, con la boca colgando abierta ---
        this.body.xRot = 0.1F + breathe * 0.02F + Mth.sin(cycle * 2.0F) * 0.04F * limbSwingAmount;
        this.body.yRot = 0.0F;
        this.body.zRot = Mth.sin(cycle) * 0.05F * limbSwingAmount;

        this.head.xRot = headPitch * RAD + 0.35F + breathe * 0.03F;
        this.head.yRot = netHeadYaw * RAD;
        this.head.zRot = 0.0F;
        this.jaw.xRot = 0.9F + breathe * 0.05F + Mth.sin(ageInTicks * 0.3F) * 0.04F;

        this.rightHorn.xRot = -0.6F;
        this.rightHorn.zRot = -0.5F + hornSway;
        this.leftHorn.xRot = -0.15F;
        this.leftHorn.zRot = 0.15F - hornSway;

        // Marcha: brazo delantero y pata trasera opuesta se mueven juntos
        this.frontRightArm.xRot = 2.0F + gait * 0.6F;
        this.frontLeftArm.xRot = 2.0F - gait * 0.6F;
        this.frontRightArm.zRot = -0.15F;
        this.frontLeftArm.zRot = 0.15F;
        this.frontRightForearm.xRot = -2.8F + gait * 0.4F;
        this.frontLeftForearm.xRot = -2.8F - gait * 0.4F;

        this.backLeftThigh.xRot = 0.7F + gait * 0.5F;
        this.backRightThigh.xRot = 0.7F - gait * 0.5F;
        this.backLeftShin.xRot = -1.4F + gait * 0.3F;
        this.backRightShin.xRot = -1.4F - gait * 0.3F;

        // --- Poses según lo que esté haciendo ---
        if (state == UknowEntity.STATE_POUNCE) {
            // Salto: brazos estirados hacia delante, patas traseras empujando y la boca muy abierta
            this.frontRightArm.xRot = -1.2F;
            this.frontLeftArm.xRot = -1.2F;
            this.frontRightForearm.xRot = 0.3F;
            this.frontLeftForearm.xRot = 0.3F;
            this.backRightThigh.xRot = 1.5F;
            this.backLeftThigh.xRot = 1.5F;
            this.backRightShin.xRot = -0.3F;
            this.backLeftShin.xRot = -0.3F;
            this.body.xRot = -0.25F;
            this.head.xRot = headPitch * RAD + 0.15F;
            this.jaw.xRot = 1.25F;
        } else if (state == UknowEntity.STATE_HOLD) {
            // Sujetando a su presa: brazos encogidos, cabeza sacudiéndose y mordiscos
            float clutch = Mth.sin(ageInTicks * 0.8F) * 0.2F;
            this.frontRightArm.xRot = 2.5F + clutch;
            this.frontLeftArm.xRot = 2.5F - clutch;
            this.frontRightForearm.xRot = -3.3F;
            this.frontLeftForearm.xRot = -3.3F;
            this.body.xRot = 0.2F;
            this.head.xRot = 0.7F + Mth.cos(ageInTicks * 1.8F) * 0.3F;
            this.head.yRot = Mth.sin(ageInTicks * 1.5F) * 0.6F;
            this.jaw.xRot = 0.9F + (Mth.sin(ageInTicks * 2.2F) * 0.5F + 0.5F) * 0.5F;
        }

        // --- Efectos de las armas ---
        if (status == FolkMonster.STATUS_PARALYZED) {
            this.body.zRot = Mth.sin(ageInTicks * 2.5F) * 0.05F;
            this.head.zRot = Mth.sin(ageInTicks * 3.1F) * 0.15F;
            this.jaw.xRot = 1.1F + Mth.sin(ageInTicks * 3.0F) * 0.08F;
        } else if (status == FolkMonster.STATUS_CONFUSED) {
            this.head.xRot += Mth.sin(ageInTicks * 0.35F) * 0.4F;
            this.head.yRot += Mth.cos(ageInTicks * 0.3F) * 0.5F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
