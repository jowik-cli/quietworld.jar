package dev.sowik.quietworld;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WatcherRenderer extends HumanoidMobRenderer<WatcherEntity, HumanoidModel<WatcherEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Quietworld.MODID, "textures/entity/watcher.png");

    public WatcherRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(WatcherEntity entity) {
        return TEXTURE;
    }
}
