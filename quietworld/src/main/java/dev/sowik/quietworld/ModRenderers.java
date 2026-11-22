package dev.sowik.quietworld;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ambient.Bat;

/** Renderers for every custom mob. They use vanilla models with generated textures. */
public final class ModRenderers {
    private ModRenderers() {}

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(Quietworld.MODID, "textures/entity/" + name + ".png");
    }

    public static class Hollow extends HumanoidMobRenderer<HollowEntity, HumanoidModel<HollowEntity>> {
        private static final ResourceLocation TEXTURE = tex("hollow");

        public Hollow(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(HollowEntity entity) {
            return TEXTURE;
        }
    }

    public static class Glitch extends HumanoidMobRenderer<GlitchEntity, HumanoidModel<GlitchEntity>> {
        private static final ResourceLocation TEXTURE = tex("glitch");

        public Glitch(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.3F);
        }

        @Override
        protected void scale(GlitchEntity entity, PoseStack poseStack, float partialTick) {
            poseStack.scale(0.7F, 0.7F, 0.7F);
        }

        @Override
        public ResourceLocation getTextureLocation(GlitchEntity entity) {
            return TEXTURE;
        }
    }

    public static class Echo extends HumanoidMobRenderer<EchoEntity, PlayerModel<EchoEntity>> {
        private static final ResourceLocation TEXTURE = tex("echo");

        public Echo(EntityRendererProvider.Context context) {
            super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(EchoEntity entity) {
            return TEXTURE;
        }
    }

    public static class TallOne extends HumanoidMobRenderer<TallOneEntity, HumanoidModel<TallOneEntity>> {
        private static final ResourceLocation TEXTURE = tex("tall_one");

        public TallOne(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        }

        @Override
        protected void scale(TallOneEntity entity, PoseStack poseStack, float partialTick) {
            poseStack.scale(0.9F, 1.5F, 0.9F);
        }

        @Override
        public ResourceLocation getTextureLocation(TallOneEntity entity) {
            return TEXTURE;
        }
    }

    public static class Tenant extends MobRenderer<TenantEntity, VillagerModel<TenantEntity>> {
        private static final ResourceLocation TEXTURE = tex("tenant");

        public Tenant(EntityRendererProvider.Context context) {
            super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(TenantEntity entity) {
            return TEXTURE;
        }
    }

    public static class Crawler extends MobRenderer<CrawlerEntity, SpiderModel<CrawlerEntity>> {
        private static final ResourceLocation TEXTURE = tex("crawler");

        public Crawler(EntityRendererProvider.Context context) {
            super(context, new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER)), 1.0F);
        }

        @Override
        public ResourceLocation getTextureLocation(CrawlerEntity entity) {
            return TEXTURE;
        }
    }

    public static class Gazer extends HumanoidMobRenderer<GazerEntity, HumanoidModel<GazerEntity>> {
        private static final ResourceLocation TEXTURE = tex("gazer");

        public Gazer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(GazerEntity entity) {
            return TEXTURE;
        }
    }

    public static class Moth extends BatRenderer {
        private static final ResourceLocation TEXTURE = tex("static_moth");

        public Moth(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public ResourceLocation getTextureLocation(Bat entity) {
            return TEXTURE;
        }
    }

    public static class Lamplighter extends HumanoidMobRenderer<LamplighterEntity, HumanoidModel<LamplighterEntity>> {
        private static final ResourceLocation TEXTURE = tex("lamplighter");

        public Lamplighter(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(LamplighterEntity entity) {
            return TEXTURE;
        }
    }

    public static class Archivist extends MobRenderer<ArchivistEntity, VillagerModel<ArchivistEntity>> {
        private static final ResourceLocation TEXTURE = tex("archivist");

        public Archivist(EntityRendererProvider.Context context) {
            super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(ArchivistEntity entity) {
            return TEXTURE;
        }
    }

    public static class Wretch extends HumanoidMobRenderer<WretchEntity, HumanoidModel<WretchEntity>> {
        private static final ResourceLocation TEXTURE = tex("wretch");

        public Wretch(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(WretchEntity entity) {
            return TEXTURE;
        }
    }

    public static class Child extends HumanoidMobRenderer<LostChildEntity, HumanoidModel<LostChildEntity>> {
        private static final ResourceLocation TEXTURE = tex("child");

        public Child(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.3F);
        }

        @Override
        protected void scale(LostChildEntity entity, PoseStack poseStack, float partialTick) {
            poseStack.scale(0.55F, 0.55F, 0.55F);
        }

        @Override
        public ResourceLocation getTextureLocation(LostChildEntity entity) {
            return TEXTURE;
        }
    }
}
