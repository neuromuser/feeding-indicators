package com.neuromuser.feedingindicators.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends PassiveEntity {

    @Shadow public abstract boolean isInLove();

    @Unique
    private static final TrackedData<Integer> BREEDING_AGE_SYNCED = DataTracker.registerData(AnimalEntityMixin.class, TrackedDataHandlerRegistry.INTEGER);

    protected AnimalEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstructor(EntityType<? extends AnimalEntity> type, World world, CallbackInfo ci) {
        // Use the instance method directly since we extend PassiveEntity (which extends Entity)
        this.getDataTracker().startTracking(BREEDING_AGE_SYNCED, 0);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void syncBreedingAge(CallbackInfo ci) {
        if (!this.getWorld().isClient) {
            this.getDataTracker().set(BREEDING_AGE_SYNCED, this.getBreedingAge());
        }
    }

    @Inject(method = "canEat", at = @At("HEAD"), cancellable = true)
    private void checkCanEat(CallbackInfoReturnable<Boolean> cir) {
        if (this.getWorld().isClient) {
            int breedingAge = this.getDataTracker().get(BREEDING_AGE_SYNCED);
            boolean inCooldown = breedingAge > 0;
            boolean inLove = this.isInLove();

            // This is the core logic that updates the client-side feeding status
            cir.setReturnValue(!inCooldown && !inLove);
        }
    }
}