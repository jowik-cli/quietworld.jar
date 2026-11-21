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
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(12)
                    .build(Quietworld.MODID + ":watcher"));

    public static final RegistryObject<EntityType<KosiwEntity>> KOSIW = ENTITIES.register("kosiw",
            () -> EntityType.Builder.of(KosiwEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(12)
                    .build(Quietworld.MODID + ":kosiw"));

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(WATCHER.get(), WatcherEntity.createAttributes().build());
        event.put(KOSIW.get(), KosiwEntity.createAttributes().build());
    }
}
