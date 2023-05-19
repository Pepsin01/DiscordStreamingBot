package zapoctak.discordbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Class that manages the queue of songs and plays them
 */
public class AudioMixer extends AudioEventAdapter {
    private List<AudioTrack> queue;
    private AudioPlayer player;
    boolean onLoop = false;
    public AudioMixer(AudioPlayer player){
        this.player = player;
        this.queue = new LinkedList<>();
    }
    /**
     * Plays the given track and stops the current one if it is playing
     * @param track track to play
     */
    public void play(AudioTrack track){
        if (player.getPlayingTrack() != null){
            player.stopTrack();
            if (queue.size() > 0)
                queue.remove(0);
        }
        queue.add(0, track);
        player.playTrack(queue.get(0));
    }
    /**
     * Adds the given track to the end of the queue
     * @param track track to add
     */
    public void add(AudioTrack track){
        queue.add(track);
        if (player.getPlayingTrack() == null && queue.size() > 0){
            player.playTrack(queue.get(0));
        }
    }
    /**
     * Adds the given track to the beginning of the queue
     * @param track track to add
     */
    public void addToBeginning(AudioTrack track){
        queue.add(0, track);
    }
    /**
     * Starts playing the first track in the queue if there is no track playing
     */
    public void start(){
        if (player.getPlayingTrack() == null && queue.size() > 0){
            player.playTrack(queue.get(0));
        }
    }

    /**
     * Stops the current track and removes it from the queue
     */
    public void stop(){
        queue.remove(0);
        player.stopTrack();
    }

    /**
     * Sets the volume of the player
     * @param volume volume to set
     */
    public void setVolume(int volume){
        player.setVolume(volume);
    }

    /**
     * Skips the current track and plays the next one in the queue
     */
    public void skip(){

        player.stopTrack();
        queue.remove(0);
        if (queue.size() > 0){
            player.playTrack(queue.get(0).makeClone());
        }
    }
    /**
     * Clears the queue and stops the current track
     */
    public void clear(){
        player.stopTrack();
        queue.clear();
    }
    /**
     * Pauses the current track
     */
    public void pause(){
        player.setPaused(true);
    }
    /**
     * Resumes the current track
     */
    public void resume(){
        player.setPaused(false);
    }
    /**
     * Returns the queue
     * @return queue
     */
    public List<AudioTrack> getQueue(){
        return queue;
    }
    /**
     * Returns the current track
     * @return current track
     */
    public AudioTrack getPlaying(){
        return player.getPlayingTrack();
    }

    /**
     * Sets the loop
     * @param onLoop whether to loop or not
     */
    public void setLoop(boolean onLoop){
        this.onLoop = onLoop;
    }

    /**
     * Shuffles the queue except for the current track
     */
    public void shuffle(){
        var track = player.getPlayingTrack();
        if (track != null){
            queue.remove(0);
        }
        Collections.shuffle(queue);
        if (track != null){
            queue.add(0, track);
        }
    }
    /**
     * Removes the track at the given index
     * @param index index of the track to remove
     * @return removed track
     */
    public AudioTrack remove(int index){
        if (index < 0 || index >= queue.size()){
            return null;
        }
        return queue.remove(index);
    }

    /**
     * Decides what to do when the track ends
     * @param player player that played the track
     * @param track track that ended
     * @param endReason reason why the track ended
     */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext){
            if (onLoop)
                queue.add(track.makeClone());

            if (queue.size() > 0)
                queue.remove(0);

            if (queue.size() > 0)
                player.playTrack(queue.get(0));
        }
    }
}
