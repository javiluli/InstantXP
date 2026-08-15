package com.javiluli.instantxp.mixin;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {

    /*
     * EN - Pickup range used to absorb XP before the orb physically touches the player.
     * The orb keeps its original Minecraft movement, preserving the visual feeling of absorption.
     *
     * ES - Rango de recogida utilizado para absorber la XP antes de que el orbe toque fisicamente al jugador.
     * El orbe mantiene el movimiento original de Minecraft, conservando la sensacion visual de absorcion.
     */
    private static final double INSTANT_PICKUP_RANGE = 1.25D;

    @Inject(method = "playerTouch", at = @At("HEAD"))
    private void instantAbsorb(Player player, CallbackInfo ci) {
        /*
         * EN - We check that we are not on the client side (XP logic is handled on the server).
         * ES - Verificamos que no estemos en el cliente (la logica de XP es del servidor).
         */
        if (!player.level().isClientSide()) {
            /*
             * EN - We reset the pickup delay to 0.
             * This allows the original Minecraft logic (including Mending) to run instantly.
             *
             * ES - Reseteamos el retraso de recogida a 0.
             * Esto permite que la logica original de Minecraft (incluyendo Mending) se ejecute al instante.
             */
            player.takeXpDelay = 0;
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void instantPickup(CallbackInfo ci) {
        ExperienceOrb orb = (ExperienceOrb) (Object) this;

        /*
         * EN - XP pickup logic must only be executed on the server.
         * We also ignore orbs that have already been removed.
         *
         * ES - La logica de recogida de XP debe ejecutarse solamente en el servidor.
         * Tambien ignoramos los orbes que ya hayan sido eliminados.
         */
        if (orb.level().isClientSide() || !orb.isAlive()) {
            return;
        }

        /*
         * EN - We search for the nearest player inside the instant pickup range.
         * The orb keeps moving normally until it gets close enough to the player.
         *
         * ES - Buscamos al jugador mas cercano dentro del rango de recogida instantanea.
         * El orbe continua moviendose normalmente hasta acercarse lo suficiente al jugador.
         */
        Player player = orb.level().getNearestPlayer(
                orb,
                INSTANT_PICKUP_RANGE
        );

        /*
         * EN - If a player is close enough, we manually trigger Minecraft's original
         * playerTouch method instead of directly giving XP.
         *
         * This preserves the original XP handling, including Mending, events,
         * sounds and any other logic executed when an experience orb is collected.
         *
         * ES - Si un jugador esta lo suficientemente cerca, ejecutamos manualmente
         * el metodo playerTouch original de Minecraft en lugar de entregar la XP directamente.
         *
         * Esto conserva la gestion original de la XP, incluyendo Mending, eventos,
         * sonidos y cualquier otra logica ejecutada al recoger un orbe de experiencia.
         */
        if (player != null) {
            orb.playerTouch(player);
        }
    }
}