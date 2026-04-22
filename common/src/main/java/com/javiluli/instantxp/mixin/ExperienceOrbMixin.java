package com.javiluli.instantxp.mixin;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {

    @Inject(method = "playerTouch", at = @At("HEAD"))
    private void instantAbsorb(Player player, CallbackInfo ci) {
        /*
         * EN - We check that we are not on the client side (XP logic is handled on the server)
         * ES - Verificamos que no estemos en el cliente (la logica de XP es del servidor)
         */
        if (!player.level().isClientSide) {
            /*
             * EN - We reset the pickup delay to 0.
             * This allows the original Minecraft logic (including Mending) to run instantly.
             *
             *  ES - Reseteamos el retraso de recogida a 0.
             * Esto permite que la logica original de Minecraft (incluyendo Mending) se ejecute al instante.
             */
            player.takeXpDelay = 0;
        }
    }

    @Inject(method = "playerTouch", at = @At("RETURN"))
    private void discardAfterTouch(Player player, CallbackInfo ci) {
        ExperienceOrb orb = (ExperienceOrb) (Object) this;

        /*
         * EN - Once the original method has finished processing XP and Mending, we remove the orb.
         * ES - Una vez el metodo original ha terminado de procesar XP y Mending, eliminamos el orbe.
         */
        if (!player.level().isClientSide) {
            orb.discard();
        }
    }
}