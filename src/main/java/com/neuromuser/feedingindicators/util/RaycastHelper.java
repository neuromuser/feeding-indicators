package com.neuromuser.feedingindicators.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;

public class RaycastHelper {

    public static Entity getTargetedEntity(PlayerEntity player, double maxDistance, Predicate<Entity> filter) {
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d direction = player.getRotationVec(1.0f);
        Vec3d end = start.add(direction.multiply(maxDistance));

        Box box = player.getBoundingBox()
                .stretch(direction.multiply(maxDistance))
                .expand(1.0);

        EntityHitResult result = ProjectileUtil.raycast(
                player, start, end, box,
                entity -> !entity.isSpectator() && entity.canHit() && filter.test(entity),
                maxDistance * maxDistance);

        return result != null ? result.getEntity() : null;
    }
}