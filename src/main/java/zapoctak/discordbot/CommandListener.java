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

record GuildAudio(AudioPlayer player, AudioMixer mixer) {}

/**
 * Handles all slash commands and responds to them.
 */
public class CommandListener extends ListenerAdapter {
    AudioPlayerManager playerManager;
    Map<String, GuildAudio> knownGuilds; // map of guild id to guild audio object
    public CommandListener() {
        super();
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        knownGuilds = new HashMap<>();
    }

    /**
     * Gets the guild audio object for the given event. Creates one if it doesn't exist.
     * @param event event to get the guild audio for
     * @return guild audio object for the given event
     */
    private GuildAudio getGuildAudio(SlashCommandInteractionEvent event){
        var guildId = event.getGuild().getId();
        if (!knownGuilds.containsKey(guildId)){ // create new guild audio object if it doesn't exist
            var player = playerManager.createPlayer();
            var mixer = new AudioMixer(player);
            player.addListener(mixer);
            knownGuilds.put(guildId, new GuildAudio(player, mixer)); // add to known guilds
        }
        return knownGuilds.get(guildId);
    }

    /**
     * Main entry point for the bot. Handles all slash commands.
     * @param event event that triggered the command
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        switch (event.getName()) {
            case "play" -> play(event);
            case "add" -> add(event);
            case "stop" -> stop(event);
            case "start" -> start(event);
            case "pause" -> pause(event);
            case "resume" -> resume(event);
            case "volume" -> volume(event);
            case "skip" -> skip(event);
            case "queue" -> queue(event);
            case "clear" -> clear(event);
            case "remove" -> remove(event);
            case "shuffle" -> shuffle(event);
            case "loop" -> loop(event);
            case "unloop" -> unloop(event);
            case "nowplaying" -> nowplaying(event);
            case "help" -> help(event);
            default -> event.reply("**Error:** Command unknown").queue();
        }
    }

    /**
     * Handles the play command. Loads the given url and plays it.
     * @param event event that triggered the command
     */
    private void play(SlashCommandInteractionEvent event){
        String url = event.getOption("url").getAsString();
        var guildAudio = getGuildAudio(event);
        var player = guildAudio.player();
        var mixer = guildAudio.mixer();
        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if(connectAudio(event, player)) {
                    event.reply("**Playing: **" + track.getInfo().title).queue();
                    mixer.play(track);
                }
            }
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if(connectAudio(event, player)) {
                    event.reply("**Added playlist: **" + playlist.getName()).queue();
                    var tracks = playlist.getTracks();
                    Collections.reverse(tracks);
                    for (AudioTrack track : tracks) {
                        mixer.addToBeginning(track);
                    }
                    mixer.start();
                }
            }
            @Override
            public void noMatches() {
                event.reply("**Nothing found by **" + url).queue();
            }
            @Override
            public void loadFailed(FriendlyException exception) {
                event.reply("**Could not play: **" + exception.getMessage()).queue();
            }
        });
    }

    /**
     * Handles the add command. Loads the given url and adds it to the queue.
     * @param event event that triggered the command
     */
    private void add(SlashCommandInteractionEvent event){
        String url = event.getOption("url").getAsString();
        var guildAudio = getGuildAudio(event);
        var player = guildAudio.player();
        var mixer = guildAudio.mixer();
        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if(connectAudio(event, player)) {
                    event.reply("**Added: **" + track.getInfo().title).queue();
                    mixer.add(track);
                }
            }
            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if(connectAudio(event, player)) {
                    event.reply("**Added playlist: **" + playlist.getName()).queue();
                    for (AudioTrack track : playlist.getTracks()) {
                        mixer.add(track);
                    }
                }
            }
            @Override
            public void noMatches() {
                event.reply("**Nothing found by **" + url).queue();
            }
            @Override
            public void loadFailed(FriendlyException exception) {
                event.reply("**Could not play: **" + exception.getMessage()).queue();
            }
        });
    }

    /**
     * Handles the stop command.
     * @param event event that triggered the command
     */
    private void stop(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().stop();
        event.reply("**Stopped**").queue();
    }
    /**
     * Handles the start command.
     * @param event event that triggered the command
     */
    private void start(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().start();
        event.reply("**Started**").queue();
    }
    /**
     * Handles the pause command.
     * @param event event that triggered the command
     */
    private void pause(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().pause();
        event.reply("**Paused**").queue();
    }
    /**
     * Handles the resume command.
     * @param event event that triggered the command
     */
    private void resume(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().resume();
        event.reply("**Resumed**").queue();
    }
    /**
     * Handles the volume command. If the volume is not between 0 and 100, it will be set to the closest value.
     * @param event event that triggered the command
     */
    private void volume(SlashCommandInteractionEvent event){
        int volume = Math.min(event.getOption("volume").getAsInt(), 100);
        volume = Math.max(volume, 0);
        getGuildAudio(event).mixer().setVolume(volume);
        event.reply("**Volume set to: **" + volume).queue();
    }
    /**
     * Handles the skip command.
     * @param event event that triggered the command
     */
    private void skip(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().skip();
        event.reply("**Skipped**").queue();
    }
    /**
     * Handles the queue command. Replies with a list of all tracks in the queue and their position unless the list is too long.
     * @param event event that triggered the command
     */
    private void queue(SlashCommandInteractionEvent event){
        StringBuilder response = new StringBuilder();
        String queueTitle = "**Queue:** \n";
        response.append(queueTitle);
        int counter = 1;
        int charCounter = queueTitle.length();
        for (AudioTrack track : getGuildAudio(event).mixer().getQueue()) {
            String line = "**"+ counter + ".** " + track.getInfo().title + "\n";
            if (charCounter + line.length() > 2000) { //Discord message limit is 2000 characters
                event.reply(response.toString()).queue();
                return;
            }
            response.append(line);
            charCounter += line.length();
            counter++;
        }
        event.reply(response.toString()).queue();
    }
    /**
     * Handles the clear command.
     * @param event event that triggered the command
     */
    private void clear(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().clear();
        event.reply("**Queue cleared**").queue();
    }
    private void remove(SlashCommandInteractionEvent event){
        var removed = getGuildAudio(event).mixer().remove(event.getOption("index").getAsInt() - 1);
        if (removed != null) {
            event.reply("**Removed:**" + removed.getInfo().title).queue();
        } else {
            event.reply("**Could not remove**").queue();
        }
    }
    /**
     * Handles the shuffle command.
     * @param event event that triggered the command
     */
    private void shuffle(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().shuffle();
        event.reply("**Shuffled**").queue();
    }
    /**
     * Handles the loop command.
     * @param event event that triggered the command
     */
    private void loop(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().setLoop(true);
        event.reply("**Looping**").queue();
    }
    /**
     * Handles the unloop command.
     * @param event event that triggered the command
     */
    private void unloop(SlashCommandInteractionEvent event){
        getGuildAudio(event).mixer().setLoop(true);
        event.reply("**Looping stopped**").queue();
    }
    /**
     * Handles the nowplaying command.
     * @param event event that triggered the command
     */
    private void nowplaying(SlashCommandInteractionEvent event){
        AudioTrack track = getGuildAudio(event).mixer().getPlaying();
        if (track != null) {
            event.reply("**Now playing: **" + track.getInfo().title).queue();
        } else {
            event.reply("**Nothing playing**").queue();
        }
    }

    /**
     * Sends a help message to the channel the command was used in
     * @param event The event that triggered the command
     */
    private void help(SlashCommandInteractionEvent event){
        event.reply("**Commands:**\n" +
                "`/play <url>` - Plays a song or adds it to the queue\n" +
                "`/add <url>` - Adds a song to the queue\n" +
                "`/stop` - Stops the player and removes the current song from queue\n" +
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

    /**
     * Handles the connection of the bot audio.
     * @param event The event that triggered the command
     * @param player The player that should be connected to the voice channel
     * @return True if the bot audio was connected, false if not
     */
    private boolean connectAudio(SlashCommandInteractionEvent event, AudioPlayer player) {
        var audioManager = event.getGuild().getAudioManager();
        if (!audioManager.isConnected()) {
            if (!connectToUsersVoiceChannel(audioManager, event.getUser())) {
                event.reply("**Error:** You need to be in a voice channel for this to work").queue();
                return false;
            }
            audioManager.setSendingHandler(new AudioSendControllor(player));
        }
        return true;
    }
    /**
     * Connects the bot to the voice channel of the user that used the command
     * @param audioManager The audio manager of the guild
     * @param user The user that used the command
     * @return True if the bot was connected to the voice channel, false if not
     */
    private boolean connectToUsersVoiceChannel(AudioManager audioManager, User user) {
        for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
            if (voiceChannel.getMembers().contains(audioManager.getGuild().getMember(user))) {
                audioManager.openAudioConnection(voiceChannel);
                return true;
            }
        }
        return false;
    }
}
