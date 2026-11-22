package dev.sowik.quietworld;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Quietworld.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Quietworld.MODID);

    public static final RegistryObject<EntityType<WatcherEntity>> WATCHER = ENTITIES.register("watcher",
            () -> EntityType.Builder.of(WatcherEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F).clientTrackingRange(12).build(Quietworld.MODID + ":watcher"));

    public static final RegistryObject<EntityType<KosiwEntity>> KOSIW = ENTITIES.register("kosiw",
            () -> EntityType.Builder.of(KosiwEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F).clientTrackingRange(12).build(Quietworld.MODID + ":kosiw"));

    public static final RegistryObject<EntityType<HollowEntity>> HOLLOW = ENTITIES.register("hollow",
            () -> EntityType.Builder.of(HollowEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F).clientTrackingRange(12).build(Quietworld.MODID + ":hollow"));

    public static final RegistryObject<EntityType<GlitchEntity>> GLITCH = ENTITIES.register("glitch",
            () -> EntityType.Builder.of(GlitchEntity::new, MobCategory.MISC)
                    .sized(0.5F, 1.3F).clientTrackingRange(10).build(Quietworld.MODID + ":glitch"));

    public static final RegistryObject<EntityType<EchoEntity>> ECHO = ENTITIES.register("echo",
            () -> EntityType.Builder.of(EchoEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F).clientTrackingRange(12).build(Quietworld.MODID + ":echo"));

    public static final RegistryObject<EntityType<TallOneEntity>> TALL_ONE = ENTITIES.register("tall_one",
            () -> EntityType.Builder.of(TallOneEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.9F).clientTrackingRange(12).build(Quietworld.MODID + ":tall_one"));

    public static final RegistryObject<EntityType<TenantEntity>> TENANT = ENTITIES.register("tenant",
            () -> EntityType.Builder.of(TenantEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(Quietworld.MODID + ":tenant"));

    public static final RegistryObject<EntityType<CrawlerEntity>> CRAWLER = ENTITIES.register("crawler",
            () -> EntityType.Builder.of(CrawlerEntity::new, MobCategory.MONSTER)
                    .sized(1.4F, 0.9F).clientTrackingRange(10).build(Quietworld.MODID + ":crawler"));

    public static final RegistryObject<EntityType<GazerEntity>> GAZER = ENTITIES.register("gazer",
            () -> EntityType.Builder.of(GazerEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F).clientTrackingRange(12).build(Quietworld.MODID + ":gazer"));

    public static final RegistryObject<EntityType<StaticMothEntity>> STATIC_MOTH = ENTITIES.register("static_moth",
            () -> EntityType.Builder.of(StaticMothEntity::new, MobCategory.AMBIENT)
                    .sized(0.5F, 0.9F).clientTrackingRange(8).build(Quietworld.MODID + ":static_moth"));

    public static final RegistryObject<EntityType<LamplighterEntity>> LAMPLIGHTER = ENTITIES.register("lamplighter",
            () -> EntityType.Builder.of(LamplighterEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F).clientTrackingRange(12).build(Quietworld.MODID + ":lamplighter"));

    public static final RegistryObject<EntityType<ArchivistEntity>> ARCHIVIST = ENTITIES.register("archivist",
            () -> EntityType.Builder.of(ArchivistEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(Quietworld.MODID + ":archivist"));

    public static final RegistryObject<EntityType<WretchEntity>> WRETCH = ENTITIES.register("wretch",
            () -> EntityType.Builder.of(WretchEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build(Quietworld.MODID + ":wretch"));

    public static final RegistryObject<EntityType<LostChildEntity>> LOST_CHILD = ENTITIES.register("lost_child",
            () -> EntityType.Builder.of(LostChildEntity::new, MobCategory.MISC)
                    .sized(0.4F, 1.1F).clientTrackingRange(10).build(Quietworld.MODID + ":lost_child"));

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(WATCHER.get(), WatcherEntity.createAttributes().build());
        event.put(KOSIW.get(), KosiwEntity.createAttributes().build());
        event.put(HOLLOW.get(), HollowEntity.createAttributes().build());
        event.put(GLITCH.get(), GlitchEntity.createAttributes().build());
        event.put(ECHO.get(), EchoEntity.createAttributes().build());
        event.put(TALL_ONE.get(), TallOneEntity.createAttributes().build());
        event.put(TENANT.get(), TenantEntity.createAttributes().build());
        event.put(CRAWLER.get(), CrawlerEntity.createAttributes().build());
        event.put(GAZER.get(), GazerEntity.createAttributes().build());
        event.put(STATIC_MOTH.get(), StaticMothEntity.createAttributes().build());
        event.put(LAMPLIGHTER.get(), LamplighterEntity.createAttributes().build());
        event.put(ARCHIVIST.get(), ArchivistEntity.createAttributes().build());
        event.put(WRETCH.get(), WretchEntity.createAttributes().build());
        event.put(LOST_CHILD.get(), LostChildEntity.createAttributes().build());
    }
}
