package com.javiluli.instantxp.mixin;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {

    @Shadow
    private Player followingPlayer;

    @Shadow
    private int count;

    /*
     * EN - Pickup range used to absorb XP before the orb physically touches the player.
     * The orb keeps its original Minecraft movement, preserving the visual feeling of absorption.
     *
     * ES - Rango de recogida utilizado para absorber la XP antes de que el orbe toque fisicamente al jugador.
     * El orbe mantiene el movimiento original de Minecraft, conservando la sensacion visual de absorcion.
     */
    @Unique
    private static final double INSTANTXP_PICKUP_RANGE = 1.25D;

    @Unique
    private static final int INSTANTXP_MAX_PICKUPS_PER_TICK = 4096;

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
             * We do not give XP, change the orb value/count, create orbs, or discard orbs here.
             *
             * ES - Reseteamos el retraso de recogida a 0.
             * Esto permite que la logica original de Minecraft (incluyendo Mending) se ejecute al instante.
             * Aqui no damos XP, no cambiamos el valor/cantidad del orbe, no creamos orbes ni descartamos orbes.
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
         * EN - We use the player already selected by vanilla attraction and only
         * trigger vanilla pickup when the orb is close enough.
         *
         * ES - Usamos el jugador ya seleccionado por la atraccion vanilla y solo
         * activamos la recogida vanilla cuando el orbe esta suficientemente cerca.
         */
        Player player = this.followingPlayer;

        /*
         * EN - If a player is close enough, we manually trigger Minecraft's original
         * playerTouch method instead of directly giving XP.
         *
         * This preserves the original XP handling, including Mending, events,
         * sounds and any other logic executed when an experience orb is collected.
         * Repeating the vanilla method consumes merged orb stacks in one tick.
         * The original method is still responsible for decreasing the orb count
         * and removing the orb when it is fully consumed.
         *
         * ES - Si un jugador esta lo suficientemente cerca, ejecutamos manualmente
         * el metodo playerTouch original de Minecraft en lugar de entregar la XP directamente.
         *
         * Esto conserva la gestion original de la XP, incluyendo Mending, eventos,
         * sonidos y cualquier otra logica ejecutada al recoger un orbe de experiencia.
         * Repetir el metodo vanilla consume pilas de orbes fusionados en un tick.
         * El metodo original sigue siendo responsable de reducir la cantidad del orbe
         * y eliminarlo cuando se consume por completo.
         */
        if (player != null
                && !player.isSpectator()
                && !player.isDeadOrDying()
                && player.distanceToSqr(orb) < INSTANTXP_PICKUP_RANGE * INSTANTXP_PICKUP_RANGE) {
            int pickups = Math.min(this.count, INSTANTXP_MAX_PICKUPS_PER_TICK);
            while (pickups > 0 && this.count > 0 && orb.isAlive()) {
                int countBeforePickup = this.count;
                orb.playerTouch(player);
                if (orb.isAlive() && this.count != countBeforePickup - 1) {
                    return;
                }

                pickups--;
            }
        }
    }
}
