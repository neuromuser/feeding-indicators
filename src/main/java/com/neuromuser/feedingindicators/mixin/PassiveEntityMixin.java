package com.neuromuser.feedingindicators.mixin;

import com.neuromuser.feedingindicators.util.BreedingDataAccessor;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.PassiveEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PassiveEntity.class)
public abstract class PassiveEntityMixin implements BreedingDataAccessor {

    @Unique
    private static final TrackedData<Integer> REAL_BREEDING_AGE = DataTracker.registerData(PassiveEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void onInitDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(REAL_BREEDING_AGE, 0);
    }

    @Override
    public TrackedData<Integer> feeding_indicators$getRealBreedingAgeData() {
        return REAL_BREEDING_AGE;
    }
}