package zapoctak.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.OptionType;
/**
 * Class that builds the bot
 */
public class BotFactory {
    public static JDA buildBot(String token) {
        JDA jda = JDABuilder.createDefault(token)
                .addEventListeners(new CommandListener())
                .setActivity(Activity.playing("Type /play to play!"))
                .build();
        jda.updateCommands().addCommands(
                Commands.slash("play", "Play a song from youtube")
                        .addOption(OptionType.STRING, "url", "Youtube url of the song", true),
                Commands.slash("stop", "Stop playing the song"),
                Commands.slash("start", "Start playing the song"),
                Commands.slash("pause", "Pause the song"),
                Commands.slash("resume", "Resume the song"),
                Commands.slash("add", "Add a song to the queue")
                        .addOption(OptionType.STRING, "url", "Youtube url of the song", true),
                Commands.slash("volume", "Change the volume of the song")
                        .addOption(OptionType.INTEGER, "volume", "Volume of the song", true),
                Commands.slash("skip", "Skip the song"),
                Commands.slash("queue", "Show the queue of songs"),
                Commands.slash("clear", "Clear the queue of songs"),
                Commands.slash("shuffle", "Shuffle the queue of songs"),
                Commands.slash("remove", "Remove a song from the queue")
                        .addOption(OptionType.INTEGER, "index", "Index of the song in the queue", true),
                Commands.slash("loop", "Loop the queue of songs"),
                Commands.slash("unloop", "Unloop the queue of songs"),
                Commands.slash("nowplaying", "Show the song that is currently playing"),
                Commands.slash("help", "Show this help message")
        ).queue();
        return jda;
    }
}
