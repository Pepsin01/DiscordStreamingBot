package zapoctak.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class BotFactory {
    public static JDA buildBot(String token) {
        JDA jda = JDABuilder.createDefault(token).build();
        return jda;
    }
}
