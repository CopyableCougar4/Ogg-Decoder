package com.digiturtle.decoder;
class StreamDecoder {
	
	protected void readStream(PageDecoder pageDecoder) {
		Vorbis vorbis = Vorbis.getVorbis();
		while (true) {
			if (vorbis.endOfBitStream) {
				if (!pageDecoder.getPageAndPacket()) {	// check if the next stream is valid
					break;
				}
				vorbis.endOfBitStream = false;
			}
			if (!vorbis.inited) {
				vorbis.inited = true; // mark the OGG block as initialized
				readStream(pageDecoder);
			}
			float[][][] _pcm = new float[1][][]; // storage for raw PCM data
			int[] _index = new int[vorbis.oggInfo.channels]; // ogg channel data
			// decode data for as long as possible
			while (!vorbis.endOfBitStream) {
				while (!vorbis.endOfBitStream) {
					int result = vorbis.syncState.pageout(vorbis.page); // validate the current page of ogg data
					if (result == 0) {
						break; // get more data
					}
					if (result == -1) {
						// ("Corrupt or missing data in bitstream");
					} else {
						vorbis.streamState.pagein(vorbis.page); // stream the page to turn synced data into ogg pages
						while (true) {
							result = vorbis.streamState.packetout(vorbis.packet);	// validate the next packet
							if (result == 0) {
								break; // we need more data to read
							}
							if (result == -1) {
								// ignore corrupt data
							} else {
								// decode the next packet
								int samples;
								if (vorbis.vorbisBlock.synthesis(vorbis.packet) == 0) { // read a block for OGG data
									vorbis.dspState.synthesis_blockin(vorbis.vorbisBlock);
								}
								// **pcm is a multichannel float vector. In stereo, for
								// example, pcm[0] is left, and pcm[1] is right. samples is
								// the size of each channel. Convert the float values
								// (-1.<=range<=1.) to whatever PCM format and write it out
								while ((samples = vorbis.dspState.synthesis_pcmout(_pcm, _index)) > 0) {
									// read the raw PCM data
									float[][] pcm = _pcm[0]; // store PCM data
									int bout = (samples < vorbis.convsize ? samples : vorbis.convsize); // min(samples, convsize)
									for (int i = 0; i < vorbis.oggInfo.channels; i++) {
										int ptr = i * 2;
										//int ptr=i;
										int mono = _index[i];
										for (int j = 0; j < bout; j++) {
											int val = (int) (pcm[i][mono + j] * 32767.);
											// might as well guard against clipping
											if (val > 32767) {
												val = 32767;
											}
											if (val < -32768) {
												val = -32768;
											}
											if (val < 0)
											val = val | 0x8000;
											if (vorbis.bigEndian) {
												vorbis.convbuffer[ptr] = (byte) (val >>> 8);
												vorbis.convbuffer[ptr + 1] = (byte) (val);
											} else {
												vorbis.convbuffer[ptr] = (byte) (val);
												vorbis.convbuffer[ptr + 1] = (byte) (val >>> 8);
											}
											// load the raw PCM data into a conversion buffer
											ptr += 2 * (vorbis.oggInfo.channels);
										}
									}
									// read the decoded PCM data into the resulting buffer
									int bytesToWrite = 2 * vorbis.oggInfo.channels * bout;
									if (bytesToWrite >= vorbis.pcmBuffer.remaining()) {
										// ("Read block from OGG that was too big to be buffered: " + bytesToWrite);
									} else {
										vorbis.pcm.write(vorbis.convbuffer);
										vorbis.pcmBuffer.put(vorbis.convbuffer, 0, bytesToWrite);
									}
									vorbis.wrote = true;
									vorbis.dspState.synthesis_read(bout); // tell vorbis the latest number of samples
								}
							}
						}
						// check if a new page needs to be read / decoded
						if (vorbis.page.eos() != 0) {
							vorbis.endOfBitStream = true;
							}
						if ((!vorbis.endOfBitStream) && (vorbis.wrote)) {
							return;	// check if the data should still be written
						}
					}
				}
				if (!vorbis.endOfBitStream) {
					vorbis.bytes = 0;
					int index = vorbis.syncState.buffer(4096);
					if (index >= 0) {
						vorbis.buffer = vorbis.syncState.data;
						try {
							vorbis.bytes = vorbis.inputStream.read(vorbis.buffer, index, 4096);
						} catch (Exception e) {
							e.printStackTrace();
							vorbis.endOfStream = true;
							return;
						}
					} else {
						vorbis.bytes = 0;
					}
					vorbis.syncState.wrote(vorbis.bytes);
					if (vorbis.bytes == 0) {
						vorbis.endOfBitStream = true;
					}
				}
			}
			vorbis.streamState.clear(); // clean up the current bitstream
			// tell Vorbis to clear its data
			vorbis.vorbisBlock.clear();
			vorbis.dspState.clear();
			// vorbis.oggInfo.clear();
		}
		vorbis.syncState.clear(); // clean up data
		vorbis.endOfStream = true;
	}

}
