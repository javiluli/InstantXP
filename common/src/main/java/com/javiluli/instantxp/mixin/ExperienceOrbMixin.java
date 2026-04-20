package com.javiluli.instantxp.mixin;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void instantAbsorb(Player player, CallbackInfo ci) {
        ExperienceOrb orb = (ExperienceOrb) (Object) this;

        /**
         * EN - We check that we are not on the client side (XP logic is handled on the server)
         * ES - Verificamos que no estemos en el cliente (la logica de XP es del servidor)
         */
        if (!player.level().isClientSide) {

            /**
             * EN - We add the XP directly to the player (skips the cooldown)
             * In 1.21.1, this method also handles Mending automatically
             *
             * ES - Anadimos la XP directamente al jugador (salta el cooldown)
             * En 1.21.1, este metodo tambien gestiona Mending automaticamente
             */
            player.giveExperiencePoints(orb.getValue());

            /**
             * EN - We remove the orb from the world immediately
             * This prevents the orb from bouncing around or waiting its turn
             *
             * ES - Eliminamos el orbe del mundo inmediatamente
             * Esto evita que el orbe se quede rebotando o esperando turno
             */
            orb.discard();

            /**
             * EN - We cancel the original method
             * This prevents Minecraft's base code from trying to process it again
             *
             * ES - Cancelamos el metodo original
             * Asi evitamos que el codigo base de Minecraft intente procesarlo de nuevo
             */
            ci.cancel();
        }
    }
}