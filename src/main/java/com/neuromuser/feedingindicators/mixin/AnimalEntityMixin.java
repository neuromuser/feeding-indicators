package com.neuromuser.feedingindicators.mixin;

import com.neuromuser.feedingindicators.util.BreedingDataAccessor;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends PassiveEntity {

    protected AnimalEntityMixin(net.minecraft.entity.EntityType<? extends PassiveEntity> entityType, net.minecraft.world.World world) {
        super(entityType, world);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void syncData(CallbackInfo ci) {
        if (!this.getWorld().isClient()) {
            var key = ((BreedingDataAccessor)this).feeding_indicators$getRealBreedingAgeData();
            this.getDataTracker().set(key, this.breedingAge);
        }
    }

    @Inject(method = "canEat", at = @At("HEAD"), cancellable = true)
    private void checkCanEat(CallbackInfoReturnable<Boolean> cir) {
        if (this.getWorld().isClient()) {
            var key = ((BreedingDataAccessor)this).feeding_indicators$getRealBreedingAgeData();
            int age = this.getDataTracker().get(key);

            if (age > 0) {
                cir.setReturnValue(false);
            }
        }
    }
}