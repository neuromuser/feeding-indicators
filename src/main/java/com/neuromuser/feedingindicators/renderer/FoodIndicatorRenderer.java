package com.neuromuser.feedingindicators.renderer;

import com.neuromuser.feedingindicators.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

public class FoodIndicatorRenderer {

    private final MinecraftClient client;
    private final ModConfig config;

    public FoodIndicatorRenderer() {
        this.client = MinecraftClient.getInstance();
        this.config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public void renderFoodIndicator(WorldRenderContext context, AnimalEntity animal, ItemStack foodItem, boolean breedable, boolean highlighted) {
        if (foodItem.isEmpty()) return;

        MatrixStack matrices = context.matrixStack();
        Vec3d cameraPos = context.camera().getPos();
        float tickDelta = context.tickDelta();

        double renderX = (animal.prevX + (animal.getX() - animal.prevX) * tickDelta) - cameraPos.x;
        double renderY = (animal.prevY + (animal.getY() - animal.prevY) * tickDelta) - cameraPos.y + animal.getHeight() + config.indicatorHeight;
        double renderZ = (animal.prevZ + (animal.getZ() - animal.prevZ) * tickDelta) - cameraPos.z;

        matrices.push();
        matrices.translate(renderX, renderY, renderZ);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-context.camera().getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(context.camera().getPitch()));

        float scale = config.indicatorScale;
        if (!highlighted && !breedable) scale *= 0.7f;
        if (highlighted) {
            assert client.world != null;
            float pulse = 1.0f + (float) Math.sin(client.world.getTime() * 0.1) * 0.1f;
            scale *= pulse * 1.2f;
        }

        matrices.scale(scale, scale, scale);
        renderItem(matrices, context.consumers(), animal, foodItem, highlighted);
        matrices.pop();
    }

    private void renderItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                            AnimalEntity animal, ItemStack stack, boolean highlighted) {
        ItemRenderer itemRenderer = client.getItemRenderer();
        int light = highlighted ? LightmapTextureManager.MAX_LIGHT_COORDINATE : getPackedLight(animal);
        itemRenderer.renderItem(stack, ModelTransformationMode.GUI, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, client.world, 0);
    }

    private int getPackedLight(AnimalEntity animal) {
        BlockPos pos = animal.getBlockPos();
        assert client.world != null;
        int blockLight = client.world.getLightLevel(LightType.BLOCK, pos);
        int skyLight = client.world.getLightLevel(LightType.SKY, pos);
        return LightmapTextureManager.pack(blockLight, skyLight);
    }
}