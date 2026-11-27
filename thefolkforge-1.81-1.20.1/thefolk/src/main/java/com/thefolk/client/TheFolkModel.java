package com.thefolk.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thefolk.TheFolkMod;
import com.thefolk.entity.FolkMonster;
import com.thefolk.entity.TheFolkEntity;
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
 * Humanoide altísimo (3 bloques): cuello largo, brazos con codos y dedos de garra.
 * La cabeza y los brazos cuelgan del torso, así que al inclinarse el torso se mueve todo junto.
 */
public class TheFolkModel extends EntityModel<TheFolkEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(TheFolkMod.MOD_ID, "the_folk"), "main");

    private static final float RAD = (float) Math.PI / 180.0F;

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightForearm;
    private final ModelPart leftForearm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public TheFolkModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.rightArm = this.body.getChild("right_arm");
        this.leftArm = this.body.getChild("left_arm");
        this.rightForearm = this.rightArm.getChild("right_forearm");
        this.leftForearm = this.leftArm.getChild("left_forearm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    private static CubeListBuilder forearmWithClaws() {
        return CubeListBuilder.create().texOffs(32, 16)
                .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 14.0F, 2.0F)
                .texOffs(44, 0)
                .addBox(-2.0F, 14.0F, -0.5F, 1.0F, 6.0F, 1.0F)
                .addBox(-0.5F, 14.0F, -0.5F, 1.0F, 6.0F, 1.0F)
                .addBox(1.0F, 14.0F, -0.5F, 1.0F, 6.0F, 1.0F);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();

        // Torso flaco (pivote en la cadera). De él cuelgan el cuello, la cabeza y los brazos.
        PartDefinition body = r.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 16).addBox(-3.0F, -16.0F, -1.5F, 6.0F, 16.0F, 3.0F),
                PartPose.offset(0.0F, 4.0F, 0.0F));

        body.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(52, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, -16.0F, 0.0F));

        body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 8.0F, 6.0F),
                PartPose.offset(0.0F, -20.0F, 0.0F));

        // Brazos en dos tramos (brazo y antebrazo con garras)
        PartDefinition rightArm = body.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 14.0F, 2.0F),
                PartPose.offset(-4.0F, -14.0F, 0.0F));
        rightArm.addOrReplaceChild("right_forearm", forearmWithClaws(), PartPose.offset(0.0F, 14.0F, 0.0F));

        PartDefinition leftArm = body.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 14.0F, 2.0F),
                PartPose.offset(4.0F, -14.0F, 0.0F));
        leftArm.addOrReplaceChild("left_forearm", forearmWithClaws(), PartPose.offset(0.0F, 14.0F, 0.0F));

        r.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(32, 40).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 20.0F, 2.0F),
                PartPose.offset(-1.5F, 4.0F, 0.0F));

        r.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(32, 40).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 20.0F, 2.0F),
                PartPose.offset(1.5F, 4.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(TheFolkEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        int status = entity.getStatus();
        boolean chasing = entity.getPhase() >= TheFolkEntity.PHASE_CHASE;
        float walk = limbSwingAmount;
        float run = Mth.clamp(limbSwingAmount * 2.0F, 0.0F, 1.0F);
        float breathe = Mth.sin(ageInTicks * 0.1F);
        float cycle = limbSwing * 0.6662F;

        // Piernas
        float legSwing = Mth.cos(cycle) * 1.3F * walk;
        this.rightLeg.xRot = legSwing;
        this.leftLeg.xRot = -legSwing;

        // Torso: respira, se inclina hacia delante al correr y se tuerce al caminar
        float lean = 0.06F + breathe * 0.02F;
        if (chasing) {
            lean += 0.15F + 0.35F * run;
        }
        this.body.xRot = lean;
        this.body.yRot = Mth.cos(cycle) * 0.25F * walk;
        this.body.zRot = Mth.sin(cycle * 0.5F) * 0.06F * walk;

        // Cabeza: sigue mirando al jugador aunque se incline el torso, ladeada, con tics bruscos
        float twitch = Mth.sin(ageInTicks * 1.7F) > 0.97F ? 0.3F : 0.0F;
        this.head.yRot = netHeadYaw * RAD - this.body.yRot + twitch;
        this.head.xRot = headPitch * RAD - lean + (chasing ? 0.3F : 0.0F);
        this.head.zRot = 0.2F + Mth.sin(ageInTicks * 0.08F) * 0.05F;

        // Brazos
        if (chasing) {
            float shake = Mth.sin(ageInTicks * 0.8F) * 0.08F;
            this.rightArm.xRot = -1.1F + shake;
            this.leftArm.xRot = -1.1F - shake;
            this.rightForearm.xRot = -0.5F + Mth.sin(ageInTicks * 0.6F) * 0.1F;
            this.leftForearm.xRot = -0.5F - Mth.sin(ageInTicks * 0.6F) * 0.1F;
            this.rightArm.zRot = 0.12F;
            this.leftArm.zRot = -0.12F;
        } else {
            float sway = Mth.sin(ageInTicks * 0.05F) * 0.03F;
            this.rightArm.xRot = -legSwing * 0.5F + sway;
            this.leftArm.xRot = legSwing * 0.5F - sway;
            this.rightForearm.xRot = -0.25F + breathe * 0.05F - legSwing * 0.2F;
            this.leftForearm.xRot = -0.25F - breathe * 0.05F + legSwing * 0.2F;
            this.rightArm.zRot = 0.05F;
            this.leftArm.zRot = -0.05F;
        }

        // Efectos de las armas
        if (status == FolkMonster.STATUS_PARALYZED) {
            // Rígido y temblando
            this.body.zRot += Mth.sin(ageInTicks * 2.5F) * 0.08F;
            this.head.zRot += Mth.sin(ageInTicks * 3.1F) * 0.15F;
            this.rightArm.zRot += Mth.sin(ageInTicks * 2.8F) * 0.1F;
            this.leftArm.zRot -= Mth.sin(ageInTicks * 2.8F) * 0.1F;
        } else if (status == FolkMonster.STATUS_CONFUSED) {
            // Cabeza bamboleándose y brazos sin fuerza
            this.head.xRot += Mth.sin(ageInTicks * 0.35F) * 0.4F;
            this.head.yRot += Mth.cos(ageInTicks * 0.3F) * 0.5F;
            this.rightArm.xRot += Mth.sin(ageInTicks * 0.4F) * 0.4F;
            this.leftArm.xRot -= Mth.sin(ageInTicks * 0.4F) * 0.4F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
