package com.digiturtle.decoder;

import com.jcraft.jogg.*;
import com.jcraft.jorbis.*;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Holds the JOrbis objects
 * @author Jonathan
 */
class Vorbis {
	
	private static Vorbis instance = new Vorbis();
	
	private Vorbis() {
		// Protect instantiation
	}
	
	protected static Vorbis getVorbis() {
		return instance;
	}
	
	protected static void initialize() {
		instance = new Vorbis();
		instance.syncState.init();
	}
	
	protected InputStream inputStream; // the input stream to decode
	
	protected int available; // the number of readable bytes in the input stream
	
	protected SyncState syncState = new SyncState(); // handles the syncing of the input stream
	
	protected boolean endOfBitStream = true; // is the bit stream completely read
	
	protected boolean inited = false; // is the OGG info block initialized
	
	protected Info oggInfo = new Info(); // bitstream settings
	
	protected Page page = new Page(); // one Ogg bitstream page
	
	protected StreamState streamState = new StreamState(); // weld the pages together
	
	protected Packet packet = new Packet(); // one raw packet of data for decode
	
	protected DspState dspState = new DspState(); // central working state for the packet->PCM decoder
	
	protected Block vorbisBlock = new Block(dspState); // local working space for packet->PCM decode
	
	protected int convsize = 4096 * 4; // size of the data segments
	
	protected byte[] convbuffer = new byte[convsize]; // take 8k out of the data segment, not the stack
	
	protected boolean bigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN); // byte ordering for data
	
	protected ByteBuffer pcmBuffer = ByteBuffer.allocateDirect(4096 * 500).order(ByteOrder.nativeOrder()); // buffer for decoded PCM data
	
	protected int bytes = 0; // number of bytes read
	
	protected boolean endOfStream; // whether there is more available data
	
	protected byte[] buffer; // scratch buffer
	
	protected Comment comment = new Comment(); // bitstream user comments
	
	protected int readIndex; // read data like a normal input stream
	
	protected RawPCM pcm;
	
	protected boolean wrote = false;

}
