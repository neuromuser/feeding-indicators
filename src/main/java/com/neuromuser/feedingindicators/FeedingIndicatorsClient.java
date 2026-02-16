package com.neuromuser.feedingindicators;

import com.neuromuser.feedingindicators.config.ModConfig;
import com.neuromuser.feedingindicators.renderer.FoodIndicatorRenderer;
import com.neuromuser.feedingindicators.util.AnimalFoodCache;
import com.neuromuser.feedingindicators.util.RaycastHelper;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;

import java.util.List;

public class FeedingIndicatorsClient implements ClientModInitializer {

    private ModConfig config;
    private AnimalFoodCache foodCache;
    private FoodIndicatorRenderer renderer;
    private Entity lastLookedAtAnimal = null;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        foodCache = new AnimalFoodCache();
        renderer = new FoodIndicatorRenderer();

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;
            if (player == null || client.world == null) return;

            if (config.enableHeldItemIndicators) renderHeldItemIndicators(context, player);
            if (config.enableLookingIndicators) renderLookingIndicators(context, player);
        });
    }

    private void renderHeldItemIndicators(WorldRenderContext context, ClientPlayerEntity player) {
        ItemStack heldItem = getRelevantHeldItem(player);
        if (heldItem.isEmpty()) return;

        double radiusSquared = (double) config.detectionRadius * config.detectionRadius;
        Box searchBox = Box.of(player.getPos(),
                config.detectionRadius * 2,
                config.detectionRadius * 2,
                config.detectionRadius * 2);

        List<AnimalEntity> nearbyAnimals = player.getWorld().getEntitiesByClass(
                AnimalEntity.class, searchBox,
                animal -> player.squaredDistanceTo(animal) <= radiusSquared);

        for (AnimalEntity animal : nearbyAnimals) {
            if (animal == lastLookedAtAnimal) continue;
            if (canBeFed(animal, heldItem)) {
                renderer.renderFoodIndicator(context, animal, heldItem, true, false);
            }
        }
    }

    private void renderLookingIndicators(WorldRenderContext context, ClientPlayerEntity player) {
        if (!player.isSneaking()) {
            lastLookedAtAnimal = null;
            return;
        }

        Entity targetEntity = RaycastHelper.getTargetedEntity(
                player, config.detectionRadius,
                entity -> entity instanceof AnimalEntity);

        if (!(targetEntity instanceof AnimalEntity animal)) {
            lastLookedAtAnimal = null;
            return;
        }

        lastLookedAtAnimal = animal;

        List<ItemStack> foodTypes;

        if (animal instanceof TameableEntity tameable && !tameable.isTamed()) {
            foodTypes = foodCache.getTamingFoodsFor(animal);
        } else {
            foodTypes = foodCache.getFoodsFor(animal);
        }

        if (foodTypes.isEmpty()) return;

        long worldTime = player.getWorld().getTime();
        int cycleIndex = (int) ((worldTime / config.cycleDurationTicks) % foodTypes.size());
        renderer.renderFoodIndicator(context, animal, foodTypes.get(cycleIndex), true, true);
    }

    private boolean canBeFed(AnimalEntity animal, ItemStack item) {
        if (animal.isBaby()) return false;
        if (!animal.isBreedingItem(item)) return false;
        return animal.canEat();
    }

    private ItemStack getRelevantHeldItem(ClientPlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        if (!mainHand.isEmpty()) return mainHand;

        ItemStack offHand = player.getOffHandStack();
        if (!offHand.isEmpty()) return offHand;

        return ItemStack.EMPTY;
    }
}