package net.instaslate.mixin;

import net.instaslate.InstaSlate;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void instaslate$doubleDeepslateDestroySpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        Player player = (Player) (Object) this;
        if (!InstaSlate.shouldBoostDeepslateMining(player, state)) {
            return;
        }

        cir.setReturnValue(cir.getReturnValue() * 2.0F);
    }
}
