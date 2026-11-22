package dev.sowik.quietworld;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Quietworld.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.WATCHER.get(), WatcherRenderer::new);
        event.registerEntityRenderer(ModEntities.KOSIW.get(), KosiwRenderer::new);
        event.registerEntityRenderer(ModEntities.HOLLOW.get(), ModRenderers.Hollow::new);
        event.registerEntityRenderer(ModEntities.GLITCH.get(), ModRenderers.Glitch::new);
        event.registerEntityRenderer(ModEntities.ECHO.get(), ModRenderers.Echo::new);
        event.registerEntityRenderer(ModEntities.TALL_ONE.get(), ModRenderers.TallOne::new);
        event.registerEntityRenderer(ModEntities.TENANT.get(), ModRenderers.Tenant::new);
        event.registerEntityRenderer(ModEntities.CRAWLER.get(), ModRenderers.Crawler::new);
        event.registerEntityRenderer(ModEntities.GAZER.get(), ModRenderers.Gazer::new);
        event.registerEntityRenderer(ModEntities.STATIC_MOTH.get(), ModRenderers.Moth::new);
        event.registerEntityRenderer(ModEntities.LAMPLIGHTER.get(), ModRenderers.Lamplighter::new);
        event.registerEntityRenderer(ModEntities.ARCHIVIST.get(), ModRenderers.Archivist::new);
        event.registerEntityRenderer(ModEntities.WRETCH.get(), ModRenderers.Wretch::new);
        event.registerEntityRenderer(ModEntities.LOST_CHILD.get(), ModRenderers.Child::new);
    }
}
