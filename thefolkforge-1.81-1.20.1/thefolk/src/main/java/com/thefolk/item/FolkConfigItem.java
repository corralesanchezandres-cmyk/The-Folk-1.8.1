package com.thefolk.item;

import com.thefolk.client.FolkClientHooks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/** Engranaje: clic derecho abre el menú con los 3 modos. */
public class FolkConfigItem extends Item {
    public FolkConfigItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> FolkClientHooks::openConfigScreen);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
