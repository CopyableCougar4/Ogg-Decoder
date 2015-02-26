package com.digiturtle.decoder;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Encapsulates raw PCM that can be fed into the sound system of your choice 
 * @author Jonathan
 */
public class RawPCM {
	
	// holds the final data
	private ByteBuffer buffer;
	
	// holds 8-bit signed bytes
	private ByteArrayOutputStream stream;
	
	// holds 16-bit unsigned bytes
	private ByteArrayOutputStream output;
	
	protected void write(byte[] data) {
		stream.write(data, 0, data.length);
	}
	
	protected void open() {
		stream = new ByteArrayOutputStream();
	}
	
	protected void close() {
		output = new ByteArrayOutputStream();
		byte[] streamed = stream.toByteArray();
		byte[] adjusted;
		for (int index = 0; index < streamed.length; index++) {
			int streamedByte = streamed[index];
			if (streamedByte < 0) {
				streamedByte = streamedByte + 256;
			}
			output.write(streamedByte);
		}
		adjusted = output.toByteArray();
		buffer = ByteBuffer.allocateDirect(adjusted.length);
		buffer.put(adjusted);
		buffer.rewind();
	}
	
	/**
	 * Retrieve the raw PCM data
	 * @return Raw PCM data in a bytebuffer
	 */
	public ByteBuffer getData() {
		return buffer;
	}

}
