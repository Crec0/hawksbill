package club.mindtech.mindbot;

import club.mindtech.mindbot.commands.Commands;
import club.mindtech.mindbot.database.PostgresDatabase;
import club.mindtech.mindbot.events.AnnotatedListener;
import club.mindtech.mindbot.events.MessageEventListener;
import club.mindtech.mindbot.events.ReadyEventListener;
import club.mindtech.mindbot.events.SlashCommandEventListener;
import club.mindtech.mindbot.util.TypedList;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;
import java.util.List;

public class MindBot {
    public static final Logger LOGGER = LoggerFactory.getLogger(MindBot.class);
    private static JDA API;

    public static void main(String[] args) {
        if (args.length == 0) {
            LOGGER.error("No token passed. Please pass token as an argument");
            System.exit(1);
        }
        String token = args[0];
        createJDA(token);
//        registerCommands();
        try {
            PostgresDatabase.connectToDatabase();
        } catch (SQLException e) {
            LOGGER.error("Could not connect to database");
            e.printStackTrace();
        }
    }

    private static void createJDA(String token) {
        try {

            API = JDABuilder
                    .createDefault(token)
                    .setEventManager(new AnnotatedEventManager())
                    .addEventListeners(getListeners())
                    .build();

        } catch (LoginException e) {
            LOGGER.error("Invalid Token. Please check your token");
            System.exit(1);
        }

        LOGGER.info("JDA created");
    }

    private static Object[] getListeners() {
        List<? extends AnnotatedListener> listeners = TypedList.of(
            new MessageEventListener(),
            new ReadyEventListener(),
            new SlashCommandEventListener()
        );
        return listeners.toArray();
    }

    private static void registerCommands() {
        getAPI().updateCommands()
                .addCommands(Commands.getSlashCommandData())
                .queue();
    }

    public static JDA getAPI() {
        return API;
    }
}
