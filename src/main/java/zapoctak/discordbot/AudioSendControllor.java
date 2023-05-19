package zapoctak.discordbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

/**
 * Class that handles sending audio to discord
 */
public class AudioSendControllor implements AudioSendHandler {
    private final AudioPlayer player;
    private final ByteBuffer buffer;
    private final MutableAudioFrame frame;

    public AudioSendControllor(AudioPlayer player) {
        this.player = player;
        this.buffer = ByteBuffer.allocate(1024);
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    /**
     * Checks if the player can provide audio
     * @return true if the player can provide audio
     */
    @Override
    public boolean canProvide() {
        return player.provide(frame);
    }

    /**
     * Provides audio to discord
     * @return ByteBuffer with audio
     */
    @Override
    public ByteBuffer provide20MsAudio() {
        return buffer.flip();
    }
    @Override
    public boolean isOpus() {
        return true;
    }
}
