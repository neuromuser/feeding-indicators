package com.neuromuser.feedingindicators.util;

import net.minecraft.entity.data.TrackedData;

public interface BreedingDataAccessor {
    TrackedData<Integer> feeding_indicators$getRealBreedingAgeData();
}