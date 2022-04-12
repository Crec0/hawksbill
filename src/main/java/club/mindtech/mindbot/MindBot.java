package club.mindtech.mindbot;

import club.mindtech.mindbot.commands.Commands;
import club.mindtech.mindbot.events.AnnotatedEventListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class MindBot {
    public static final Logger LOGGER = LoggerFactory.getLogger(MindBot.class);
    private static JDA API;

    public static void main(String[] args) {
        if (args.length == 0) {
            LOGGER.error("No token passed. Please pass token as an argument");
            System.exit(1);
        }
        final String token = args[0];
        createJDA(token);
        registerCommands();
    }

    private static void createJDA(String token) {
        try {
            API = JDABuilder
                .createDefault(token)
                .setEventManager(new AnnotatedEventManager())
                .addEventListeners(new AnnotatedEventListener())
                .build()
                .awaitReady();
        } catch (LoginException e) {
            LOGGER.error("Invalid Token. Please check your token");
            System.exit(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LOGGER.info("JDA created");
    }

    public static JDA getAPI() {
        return API;
    }

    private static void registerCommands() {
        getAPI().getGuilds().forEach(MindBot::registerGuildCommands);
    }

    private static void registerGuildCommands(Guild guild) {
        guild.updateCommands()
            .addCommands(Commands.getSlashCommandData())
            .queue();
    }

    public static MongoDatabase getDatabase() {
        return database;
    }
}
