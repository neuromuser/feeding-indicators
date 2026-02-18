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
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Box;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedingIndicatorsClient implements ClientModInitializer {

    private ModConfig config;
    private AnimalFoodCache foodCache;
    private FoodIndicatorRenderer renderer;
    private Entity lastLookedAtAnimal = null;

    private final Map<Integer, Long> recentlyFedAnimals = new HashMap<>();
    private static final long HIDE_DURATION_TICKS = 600; //love mode duration!

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        foodCache = new AnimalFoodCache();
        renderer = new FoodIndicatorRenderer();

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient && entity instanceof AnimalEntity animal && canBeFed(animal, player.getStackInHand(hand))) {
                recentlyFedAnimals.put(entity.getId(), world.getTime());
            }
            return ActionResult.PASS;
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;
            if (player == null || client.world == null) return;

            if (config.enableHeldItemIndicators) renderHeldItemIndicators(context, player);
            if (config.enableLookingIndicators) renderLookingIndicators(context, player);
        });
    }

    private boolean isRecentlyFed(AnimalEntity animal, long worldTime) {
        Long fedTime = recentlyFedAnimals.get(animal.getId());
        if (fedTime == null) return false;
        if (worldTime - fedTime > HIDE_DURATION_TICKS) {
            recentlyFedAnimals.remove(animal.getId());
            return false;
        }
        return true;
    }

    private void renderHeldItemIndicators(WorldRenderContext context, ClientPlayerEntity player) {
        ItemStack heldItem = getRelevantHeldItem(player);
        if (heldItem.isEmpty()) return;

        long worldTime = player.getWorld().getTime();
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
            if (isRecentlyFed(animal, worldTime)) continue;
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
                player, 4,
                entity -> entity instanceof AnimalEntity);

        if (!(targetEntity instanceof AnimalEntity animal)) {
            lastLookedAtAnimal = null;
            return;
        }

        lastLookedAtAnimal = animal;

        long worldTime = player.getWorld().getTime();
        if (isRecentlyFed(animal, worldTime)) return;

        List<ItemStack> foodTypes;

        if (animal instanceof TameableEntity tameable && !tameable.isTamed()) {
            foodTypes = foodCache.getTamingFoodsFor(animal);
        } else {
            foodTypes = foodCache.getFoodsFor(animal);
        }

        if (foodTypes.isEmpty()) return;

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