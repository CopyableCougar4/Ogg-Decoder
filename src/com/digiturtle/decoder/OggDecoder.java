package com.digiturtle.decoder;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents the decoding interface
 * @author Jonathan
 */
public class OggDecoder {
	
	/**
	 * Construct the Ogg decoder and start decoding
	 * @param stream Access to the Ogg file
	 * @throws IOException When decoding fails
	 */
	public OggDecoder(InputStream stream) throws IOException {
		// Initialize Vorbis
		Vorbis.initialize();
		Vorbis vorbis = Vorbis.getVorbis();
		vorbis.inputStream = stream;
		vorbis.available = vorbis.inputStream.available();
		// Start the decoding
		InternalDecoder decoder = new InternalDecoder();
		decoder.readData();
	}
	
	/**
	 * Get the bit stream rate
	 * @return Bit stream rate
	 */
	public int getRate() {
		Vorbis vorbis = Vorbis.getVorbis();
		return vorbis.oggInfo.rate;
	}
	
	/**
	 * Get the number of channels in this bit stream
	 * @return Bit stream channels
	 */
	public int getChannels() {
		Vorbis vorbis = Vorbis.getVorbis();
		return vorbis.oggInfo.channels;
	}
	
	/**
	 * Get the Raw PCM data read. Use .getData() to retrieve the buffer
	 * @return Raw PCM data
	 */
	public RawPCM getPCM() {
		Vorbis vorbis = Vorbis.getVorbis();
		return vorbis.pcm;
	}

}
