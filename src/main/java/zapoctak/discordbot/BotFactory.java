package zapoctak.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class BotFactory {
    public static JDA buildBot(String token) {
        JDA jda = JDABuilder.createDefault(token)
                .addEventListeners(new CommandListener())
                .setActivity(Activity.playing("Type /play to play!"))
                .build();
        jda.updateCommands().addCommands(Commands.slash("play", "Zazpivej mi Helenko")).queue();
        return jda;
    }
}
