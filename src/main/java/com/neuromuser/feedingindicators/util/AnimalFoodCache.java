package com.neuromuser.feedingindicators.util;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimalFoodCache {

    private final Map<Class<? extends AnimalEntity>, List<ItemStack>> breedingCache = new HashMap<>();
    private final Map<Class<? extends AnimalEntity>, List<ItemStack>> tamingCache = new HashMap<>();

    public List<ItemStack> getFoodsFor(AnimalEntity animal) {
        return breedingCache.computeIfAbsent(animal.getClass(), k -> discoverFoods(animal));
    }

    public List<ItemStack> getTamingFoodsFor(AnimalEntity animal) {
        return tamingCache.computeIfAbsent(animal.getClass(), k -> discoverTamingFoods(animal));
    }

    private List<ItemStack> discoverFoods(AnimalEntity animal) {
        List<ItemStack> foods = new ArrayList<>();
        for (Item item : Registries.ITEM) {
            ItemStack stack = new ItemStack(item);
            if (animal.isBreedingItem(stack)) {
                foods.add(stack);
            }
        }
        return foods;
    }

    private List<ItemStack> discoverTamingFoods(AnimalEntity animal) {
        List<ItemStack> foods = new ArrayList<>();
        if (animal instanceof WolfEntity) {
            foods.add(new ItemStack(Items.BONE));
        } else if (animal instanceof CatEntity) {
            foods.add(new ItemStack(Items.COD));
            foods.add(new ItemStack(Items.SALMON));
        } else if (animal instanceof ParrotEntity) {
            foods.add(new ItemStack(Items.WHEAT_SEEDS));
            foods.add(new ItemStack(Items.MELON_SEEDS));
            foods.add(new ItemStack(Items.PUMPKIN_SEEDS));
            foods.add(new ItemStack(Items.BEETROOT_SEEDS));
            foods.add(new ItemStack(Items.TORCHFLOWER_SEEDS));
        }
        return foods;
    }
}