package club.mindtech.mindbot;

import club.mindtech.mindbot.events.ReadyEventListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

public class MindBot {
    public static final Logger LOGGER = LoggerFactory.getLogger(MindBot.class);
    private static JDA JDA_INSTANCE;

    public static void main(String[] args) {
        if (args.length == 0) {
            LOGGER.error("No token passed. Please pass token as an argument");
            System.exit(1);
        }
        String token = args[0];
        createJDA(token);
    }

    private static void createJDA(String token) {
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.addEventListeners(registerListeners());

        try {
            JDA_INSTANCE = builder.build();
        } catch (LoginException e) {
            LOGGER.error("Failed to create JDA", e);
            System.exit(1);
        }

        LOGGER.info("JDA created");
    }

    private static Object[] registerListeners() {
        List<Object> listeners = new ArrayList<>(10);

        listeners.add(new ReadyEventListener());

        return listeners.toArray();
    }

    public static JDA getJDA() {
        return JDA_INSTANCE;
    }
}
