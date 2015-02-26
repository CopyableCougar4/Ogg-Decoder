package com.digiturtle.decoder;

/**
 * Represents the overall decoder handler
 * @author Jonathan
 */
class InternalDecoder {
	
	protected PageDecoder pageDecoder = new PageDecoder();
	
	protected StreamDecoder streamDecoder = new StreamDecoder();
	
	protected InternalDecoder() {
		// Constructor
	}
	
	protected void readData() {
		Vorbis.getVorbis().wrote = false;
		Vorbis.getVorbis().pcm = new RawPCM();
		Vorbis.getVorbis().pcm.open();
		pageDecoder = new PageDecoder();
		streamDecoder = new StreamDecoder();
		streamDecoder.readStream(pageDecoder);
		Vorbis.getVorbis().pcm.close();
	}

}
