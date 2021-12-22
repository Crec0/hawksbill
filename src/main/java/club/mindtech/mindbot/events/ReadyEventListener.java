package club.mindtech.mindbot.events;

import club.mindtech.mindbot.MindBot;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class ReadyEventListener {

    @SubscribeEvent
    public void onReady(@NotNull ReadyEvent event) {
        MindBot.LOGGER.info("Bot is ready!");
    }
}
