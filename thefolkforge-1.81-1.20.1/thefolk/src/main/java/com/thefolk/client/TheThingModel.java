package com.thefolk.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thefolk.TheFolkMod;
import com.thefolk.entity.FolkMonster;
import com.thefolk.entity.TheThingEntity;
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
 * Criatura ancha y encorvada: mandíbula enorme que se abre y se cierra, tres ojos, púas en la espalda,
 * garras y dos brazos extra que le salen del pecho. Todo cuelga del torso, que se inclina al correr.
 */
public class TheThingModel extends EntityModel<TheThingEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(TheFolkMod.MOD_ID, "the_thing"), "main");

    private static final float RAD = (float) Math.PI / 180.0F;

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart extraLeftArm;
    private final ModelPart extraRightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public TheThingModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.extraLeftArm = this.body.getChild("extra_left_arm");
        this.extraRightArm = this.body.getChild("extra_right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    private static CubeListBuilder armWithClaws() {
        return CubeListBuilder.create().texOffs(32, 16)
                .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 22.0F, 3.0F)
                .texOffs(44, 34)
                .addBox(-1.5F, 22.0F, -0.5F, 1.0F, 4.0F, 1.0F)
                .addBox(-0.5F, 22.0F, -0.5F, 1.0F, 4.0F, 1.0F)
                .addBox(0.5F, 22.0F, -0.5F, 1.0F, 4.0F, 1.0F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();

        // Torso con tres púas en la espalda (pivote en la cadera)
        PartDefinition body = r.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-4.0F, -14.0F, -2.5F, 8.0F, 14.0F, 5.0F)
                        .texOffs(32, 0)
                        .addBox(-1.0F, -13.0F, 2.5F, 2.0F, 4.0F, 2.0F)
                        .addBox(-1.0F, -9.0F, 2.5F, 2.0F, 4.0F, 2.0F)
                        .addBox(-1.0F, -5.0F, 2.5F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, 10.0F, 0.0F));

        // Cabeza, con la mandíbula articulada en la parte de atrás
        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, -14.0F, 0.0F));
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(0, 40).addBox(-3.0F, 0.0F, -7.0F, 6.0F, 4.0F, 7.0F),
                PartPose.offset(0.0F, 0.0F, 3.0F));

        body.addOrReplaceChild("right_arm", armWithClaws(), PartPose.offset(-5.5F, -12.0F, 0.0F));
        body.addOrReplaceChild("left_arm", armWithClaws(), PartPose.offset(5.5F, -12.0F, 0.0F));

        // Dos brazos extra que le salen del pecho
        body.addOrReplaceChild("extra_right_arm",
                CubeListBuilder.create().texOffs(40, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 14.0F, 2.0F),
                PartPose.offset(-2.5F, -10.0F, -3.0F));
        body.addOrReplaceChild("extra_left_arm",
                CubeListBuilder.create().texOffs(40, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 14.0F, 2.0F),
                PartPose.offset(2.5F, -10.0F, -3.0F));

        r.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(46, 16).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F),
                PartPose.offset(-2.0F, 10.0F, 0.0F));
        r.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(46, 16).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 14.0F, 3.0F),
                PartPose.offset(2.0F, 10.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(TheThingEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        int status = entity.getStatus();
        float walk = limbSwingAmount;
        float run = Mth.clamp(limbSwingAmount * 2.0F, 0.0F, 1.0F);
        float breathe = Mth.sin(ageInTicks * 0.12F);
        float cycle = limbSwing * 0.8F;

        // Piernas
        float legSwing = Mth.cos(cycle) * 1.6F * walk;
        this.rightLeg.xRot = legSwing;
        this.leftLeg.xRot = -legSwing;

        // Torso encorvado que respira y se lanza hacia delante al correr
        this.body.xRot = 0.25F + 0.3F * run + breathe * 0.03F;
        this.body.yRot = Mth.cos(cycle) * 0.3F * walk;
        this.body.zRot = Mth.sin(cycle * 0.5F) * 0.08F * walk;

        // Cabeza que sigue al jugador y temblor
        this.head.yRot = netHeadYaw * RAD - this.body.yRot;
        this.head.xRot = headPitch * RAD - this.body.xRot + 0.45F;
        this.head.zRot = Mth.sin(ageInTicks * 0.3F) * 0.05F;

        // Mandíbula: entreabierta en reposo, abriéndose y cerrándose a mordiscos al correr
        this.jaw.xRot = 0.3F + breathe * 0.05F + run * (0.5F + Mth.sin(ageInTicks * 1.2F) * 0.35F);

        // Brazos largos hacia delante, temblando
        float shake = Mth.cos(ageInTicks * 1.1F) * 0.25F;
        this.rightArm.xRot = -1.2F + shake;
        this.leftArm.xRot = -1.2F - shake;
        this.rightArm.zRot = 0.15F;
        this.leftArm.zRot = -0.15F;

        // Brazos extra agitándose sin parar
        float flail = Mth.sin(ageInTicks * 0.9F) * 0.6F;
        this.extraRightArm.xRot = -0.4F + flail;
        this.extraLeftArm.xRot = -0.4F - flail;
        this.extraRightArm.zRot = 0.2F;
        this.extraLeftArm.zRot = -0.2F;

        // Efectos de las armas
        if (status == FolkMonster.STATUS_PARALYZED) {
            this.body.zRot += Mth.sin(ageInTicks * 2.5F) * 0.08F;
            this.head.zRot += Mth.sin(ageInTicks * 3.1F) * 0.15F;
            this.jaw.xRot = 0.8F + Mth.sin(ageInTicks * 3.0F) * 0.1F;      // mandíbula abierta y temblando
            this.extraRightArm.xRot = -0.4F + Mth.sin(ageInTicks * 3.5F) * 0.15F;
            this.extraLeftArm.xRot = -0.4F - Mth.sin(ageInTicks * 3.5F) * 0.15F;
        } else if (status == FolkMonster.STATUS_CONFUSED) {
            this.head.xRot += Mth.sin(ageInTicks * 0.35F) * 0.4F;
            this.head.yRot += Mth.cos(ageInTicks * 0.3F) * 0.5F;
            this.rightArm.xRot += Mth.sin(ageInTicks * 0.4F) * 0.5F;
            this.leftArm.xRot -= Mth.sin(ageInTicks * 0.4F) * 0.5F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
