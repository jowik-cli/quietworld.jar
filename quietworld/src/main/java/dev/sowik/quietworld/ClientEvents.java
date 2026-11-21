package dev.sowik.quietworld;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Quietworld.MODID, value = Dist.CLIENT)
public class ClientEvents {
    private static boolean shown = false;

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (shown || !Config.SHOW_WARNING.get()) return;
        if (event.getNewScreen() instanceof TitleScreen) {
            shown = true;
            event.setNewScreen(new WarningScreen(event.getNewScreen()));
        }
    }
}
