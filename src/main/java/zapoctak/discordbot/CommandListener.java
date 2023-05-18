package zapoctak.discordbot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class GuildAudio {
    public GuildAudio(AudioPlayer player, AudioMixer mixer) {
        this.player = player;
        this.mixer = mixer;
    }

    private AudioPlayer player;
    private AudioMixer mixer;
    public AudioPlayer player() {
        return player;
    }
    public AudioMixer mixer() {
        return mixer;
    }
}

public class CommandListener extends ListenerAdapter {
    AudioPlayerManager playerManager;
    Map<String, GuildAudio> knownGuilds;
    public CommandListener() {
        super();
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        knownGuilds = new HashMap<>();
    }
    private GuildAudio getGuildAudio(SlashCommandInteractionEvent event){
        var guildId = event.getGuild().getId();
        if (!knownGuilds.containsKey(guildId)){
            var player = playerManager.createPlayer();
            var mixer = new AudioMixer(player);
            player.addListener(mixer);
            knownGuilds.put(guildId, new GuildAudio(player, mixer));
        }
        return knownGuilds.get(guildId);
    }
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        switch (event.getName()) {
            case "play":
                play(event);
                break;
            case "add":
                add(event);
                break;
            case "stop":
                stop(event);
                break;
            case "start":
                start(event);
                break;
            case "pause":
                pause(event);
                break;
            case "resume":
                resume(event);
                break;
            case "volume":
                volume(event);
                break;
            case "skip":
                skip(event);
                break;
            case "queue":
                queue(event);
                break;
            case "clear":
                clear(event);
                break;
            case "remove":
                remove(event);
                break;
            case "shuffle":
                shuffle(event);
                break;
            case "loop":
                loop(event);
                break;
            case "unloop":
                unloop(event);
                break;
            case "nowplaying":
                nowplaying(event);
                break;
            case "help":
                help(event);
                break;
            default:
                event.reply("Command unknown").queue();
        }
    }
    private void play(SlashCommandInteractionEvent event){
        String url = event.getOption("url").getAsString();
        var guildAudio = getGuildAudio(event);
        var player = guildAudio.player();
        var mixer = guildAudio.mixer();
        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                event.reply("Playing: " + track.getInfo().title).queue();
                mixer.play(track);
            }
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                event.reply("Added playlist: " + playlist.getName()).queue();
                var tracks = playlist.getTracks();
                Collections.reverse(tracks);
                for (AudioTrack track : tracks) {
                    mixer.addToBeginning(track);
                }
                mixer.start();
            }
            @Override
            public void noMatches() {
                event.reply("Nothing found by " + url).queue();
            }
            @Override
            public void loadFailed(FriendlyException exception) {
                event.reply("Could not play: " + exception.getMessage()).queue();
            }
        });

        var audioManager = event.getGuild().getAudioManager();
        if (!audioManager.isConnected()) {
            connectToUsersVoiceChannel(audioManager, event.getUser());
            audioManager.setSendingHandler(new AudioSendControllor(player));
        }
    }
    private void add(SlashCommandInteractionEvent event){
        String url = event.getOption("url").getAsString();
        var guildAudio = getGuildAudio(event);
        var player = guildAudio.player();
        var mixer = guildAudio.mixer();
        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                event.reply("Added: " + track.getInfo().title).queue();
                mixer.add(track);
            }
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                event.reply("Added playlist: " + playlist.getName()).queue();
                for (AudioTrack track : playlist.getTracks()) {
                    mixer.add(track);
                }
            }
            @Override
            public void noMatches() {
                event.reply("Nothing found by " + url).queue();
            }
            @Override
            public void loadFailed(FriendlyException exception) {
                event.reply("Could not play: " + exception.getMessage()).queue();
            }
        });

        var audioManager = event.getGuild().getAudioManager();
        if (!audioManager.isConnected()) {
            connectToUsersVoiceChannel(audioManager, event.getUser());
            audioManager.setSendingHandler(new AudioSendControllor(player));
        }
    }
    private void stop(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().stop();
        event.reply("Stopped").queue();
    }
    private void start(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().start();
        event.reply("Started").queue();
    }
    private void pause(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().pause();
        event.reply("Paused").queue();
    }
    private void resume(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().resume();
        event.reply("Resumed").queue();
    }
    private void volume(SlashCommandInteractionEvent event){
        int volume = Math.min(event.getOption("volume").getAsInt(), 100);
        volume = Math.max(volume, 0);
        getGuildAudio(event).mixer().setVolume(volume);
        event.reply("Volume set to: " + volume).queue();
    }
    private void skip(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().skip();
        event.reply("Skipped").queue();
    }
    private void queue(SlashCommandInteractionEvent event){
        StringBuilder response = new StringBuilder();
        String queueTitle = "Queue: \n";
        response.append(queueTitle);
        int counter = 1;
        int charCounter = queueTitle.length();
        for (AudioTrack track : getGuildAudio(event).mixer().getQueue()) {
            String line = "**"+ counter + ".** " + track.getInfo().title + "\n";
            if (charCounter + line.length() > 2000) {
                event.reply(response.toString()).queue();
                return;
            }
            response.append(line);
            charCounter += line.length();
            counter++;
        }
        event.reply(response.toString()).queue();
    }
    private void clear(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().clear();
        event.reply("Queue cleared").queue();
    }
    private void remove(SlashCommandInteractionEvent event){
        var removed = getGuildAudio(event).mixer().remove(event.getOption("index").getAsInt() - 1);
        if (removed != null) {
            event.reply("Removed:" + removed.getInfo().title).queue();
        } else {
            event.reply("Could not remove").queue();
        }
    }
    private void shuffle(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().shuffle();
        event.reply("Shuffled").queue();
    }
    private void loop(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().setLoop(true);
        event.reply("Looping").queue();
    }
    private void unloop(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().setLoop(true);
        event.reply("Looping stopped").queue();
    }
    private void nowplaying(SlashCommandInteractionEvent event){
        AudioTrack track = getGuildAudio(event).mixer().getPlaying();
        if (track != null) {
            event.reply("Now playing: " + track.getInfo().title).queue();
        } else {
            event.reply("Nothing playing").queue();
        }
    }
    private void help(SlashCommandInteractionEvent event){
        event.reply("**Commands:**\n" +
                "`/play <url>` - Plays a song or adds it to the queue\n" +
                "`/add <url>` - Adds a song to the queue\n" +
                "`/stop` - Stops the player and clears the queue\n" +
                "`/start` - Starts the player\n" +
                "`/pause` - Pauses the player\n" +
                "`/resume` - Resumes the player\n" +
                "`/volume <volume>` - Sets the volume of the player\n" +
                "`/skip` - Skips the current song\n" +
                "`/queue` - Shows the current queue\n" +
                "`/clear` - Clears the queue\n" +
                "`/remove <index>` - Removes a song from the queue\n" +
                "`/shuffle` - Shuffles the queue\n" +
                "`/loop` - Loops the current song\n" +
                "`/loopqueue` - Loops the queue\n" +
                "`/nowplaying` - Shows the current song\n" +
                "`/help` - Shows this message"
        ).queue();
    }

    private static void connectToUsersVoiceChannel(AudioManager audioManager, User user) {
        for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
            if (voiceChannel.getMembers().contains(audioManager.getGuild().getMember(user))) {
                audioManager.openAudioConnection(voiceChannel);
                return;
            }
        }
        System.out.println("User not in voice channel");
    }
}
