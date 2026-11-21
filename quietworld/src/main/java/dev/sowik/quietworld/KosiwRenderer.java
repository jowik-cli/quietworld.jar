package dev.sowik.quietworld;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class KosiwRenderer extends HumanoidMobRenderer<KosiwEntity, PlayerModel<KosiwEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Quietworld.MODID, "textures/entity/kosiw.png");

    public KosiwRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(KosiwEntity entity) {
        return TEXTURE;
    }
}
