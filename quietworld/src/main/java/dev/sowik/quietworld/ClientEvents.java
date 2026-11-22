package dev.sowik.quietworld;

import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
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
        Screen next = event.getNewScreen();
        boolean custom = Config.CUSTOM_MENUS.get();

        if (next instanceof TitleScreen) {
            Screen target = custom ? new QuietTitleScreen() : next;
            if (!shown && Config.SHOW_WARNING.get()) {
                shown = true;
                event.setNewScreen(new WarningScreen(target));
            } else {
                shown = true;
                if (custom) {
                    event.setNewScreen(target);
                }
            }
        } else if (next instanceof PauseScreen && custom) {
            event.setNewScreen(new QuietPauseScreen());
        }
    }
}
