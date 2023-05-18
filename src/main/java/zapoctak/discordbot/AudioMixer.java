package zapoctak.discordbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AudioMixer extends AudioEventAdapter {
    private List<AudioTrack> queue;
    private AudioPlayer player;
    boolean onLoop = false;
    public AudioMixer(AudioPlayer player){
        this.player = player;
        this.queue = new LinkedList<>();
    }
    public void play(AudioTrack track){
        if (player.getPlayingTrack() != null){
            player.stopTrack();
            if (queue.size() > 0)
                queue.remove(0);
        }
        queue.add(0, track);
        player.playTrack(queue.get(0));
    }
    public void start(){
        if (player.getPlayingTrack() == null && queue.size() > 0){
            player.playTrack(queue.get(0));
        }
    }
    public void add(AudioTrack track){
        queue.add(track);
        if (player.getPlayingTrack() == null && queue.size() > 0){
            player.playTrack(queue.get(0));
        }
    }
    public void addToBeginning(AudioTrack track){
        queue.add(0, track);
    }
    public void stop(){
        queue.remove(0);
        player.stopTrack();
    }
    public void setVolume(int volume){
        player.setVolume(volume);
    }
    public void skip(){

        player.stopTrack();
        queue.remove(0);
        if (queue.size() > 0){
            player.playTrack(queue.get(0).makeClone());
        }
    }
    public void clear(){
        player.stopTrack();
        queue.clear();
    }
    public void pause(){
        player.setPaused(true);
    }
    public void resume(){
        player.setPaused(false);
    }
    public List<AudioTrack> getQueue(){
        return queue;
    }
    public AudioTrack getPlaying(){
        return player.getPlayingTrack();
    }
    public void setLoop(boolean onLoop){
        this.onLoop = onLoop;
    }
    public void shuffle(){
        Collections.shuffle(queue);
    }
    public AudioTrack remove(int index){
        if (index < 0 || index >= queue.size()){
            return null;
        }
        return queue.remove(index);
    }
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
