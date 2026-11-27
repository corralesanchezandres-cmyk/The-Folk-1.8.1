package com.thefolk.client;

import com.thefolk.entity.FolkMonster;
import com.thefolk.entity.TheManEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Modelo de jugador de vanilla con poses propias:
 * quieto y rígido mientras te espera, y corriendo inclinado con los brazos hacia atrás.
 */
public class TheManModel extends HumanoidModel<TheManEntity> {

    public TheManModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(TheManEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        int state = entity.getManState();
        int status = entity.getStatus();

        if (state != TheManEntity.STATE_RUN) {
            // Rígido como un maniquí, con la cabeza clavada en el jugador
            this.rightArm.xRot = 0.0F;
            this.leftArm.xRot = 0.0F;
            this.rightArm.zRot = 0.04F;
            this.leftArm.zRot = -0.04F;
            this.rightLeg.xRot = 0.0F;
            this.leftLeg.xRot = 0.0F;
        } else {
            // Corriendo: brazos hacia atrás y la cabeza adelantada
            float cycle = limbSwing * 0.9F;
            float amount = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
            this.rightArm.xRot = 1.0F + Mth.cos(cycle) * 0.8F * amount;
            this.leftArm.xRot = 1.0F - Mth.cos(cycle) * 0.8F * amount;
            this.rightArm.zRot = 0.1F;
            this.leftArm.zRot = -0.1F;
            this.rightLeg.xRot = Mth.cos(cycle) * 1.6F * amount;
            this.leftLeg.xRot = -Mth.cos(cycle) * 1.6F * amount;
            this.head.xRot += 0.35F;
        }

        if (status == FolkMonster.STATUS_PARALYZED) {
            this.head.zRot = Mth.sin(ageInTicks * 3.1F) * 0.15F;
            this.rightArm.zRot += Mth.sin(ageInTicks * 2.8F) * 0.1F;
            this.leftArm.zRot -= Mth.sin(ageInTicks * 2.8F) * 0.1F;
        } else if (status == FolkMonster.STATUS_CONFUSED) {
            this.head.xRot += Mth.sin(ageInTicks * 0.35F) * 0.4F;
            this.head.yRot += Mth.cos(ageInTicks * 0.3F) * 0.5F;
            this.rightArm.xRot += Mth.sin(ageInTicks * 0.4F) * 0.5F;
            this.leftArm.xRot -= Mth.sin(ageInTicks * 0.4F) * 0.5F;
        }

        this.hat.copyFrom(this.head);
    }
}
