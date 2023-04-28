package zapoctak.discordbot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;

public class CommandListener extends ListenerAdapter {
    AudioPlayerManager playerManager;
    AudioPlayer player;
    public CommandListener() {
        super();
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        player = playerManager.createPlayer();
    }
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        if (event.getName().equals("play")) {
            event.reply("Zpivam!").queue();
            AudioManager audioManager = event.getGuild().getAudioManager();
            if (!audioManager.isConnected()) {
                connectToFirstVoiceChannel(audioManager);
            }
            playerManager.loadItem("https://youtu.be/EJlY1M65Xao", new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack audioTrack) {
                    player.setVolume(30);
                    player.playTrack(audioTrack);
                }

                @Override
                public void playlistLoaded(AudioPlaylist audioPlaylist) {
                    player.playTrack(audioPlaylist.getTracks().get(0));
                }

                @Override
                public void noMatches() {

                }

                @Override
                public void loadFailed(FriendlyException e) {

                }
                public void loadFailed(java.lang.Throwable throwable) {

                }
            });
            audioManager.setSendingHandler(new AudioSendControllor(player));
        }
    }
    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
        for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
            audioManager.openAudioConnection(voiceChannel);
            break;
        }
    }
}
