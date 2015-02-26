package com.digiturtle.decoder;

/**
 * Represents the decoder to read a single page
 * @author Jonathan
 */
class PageDecoder {
	
	protected PageDecoder() {
		// Protect Instantiation
	}
	
	protected boolean getPageAndPacket() {
		Vorbis vorbis = Vorbis.getVorbis();
		// validate the data
		int index = vorbis.syncState.buffer(4096); // send Vorbis an ogg page
		vorbis.buffer = vorbis.syncState.data;	// add the synced bitstream as the scratch buffer
		if (vorbis.buffer == null) {
			vorbis.endOfStream = true;	// validation of the scratch buffer
			return false;
		}
		try {
			vorbis.bytes = vorbis.inputStream.read(vorbis.buffer, index, 4096); // read a block of raw data
		} catch (Exception e) {
			vorbis.endOfStream = true;
			return false;
		}
		vorbis.syncState.wrote(vorbis.bytes); // mark these bytes as loaded
		// Get the first page.
		if (vorbis.syncState.pageout(vorbis.page) != 1) {
			// have we simply run out of data?  If so, we're done.
			if (vorbis.bytes < 4096)
				return false;
			// error case.  Must not be Vorbis data
			vorbis.endOfStream = true;
			return false;
		}
		// Use the serial number to prepare the bitstream
		vorbis.streamState.init(vorbis.page.serialno());
		// validate the OGG bitstream
		vorbis.oggInfo.init();
		vorbis.comment.init();
		if (vorbis.streamState.pagein(vorbis.page) < 0) {
			// error; stream version mismatch perhaps
			System.out.println("Are you sure line 45 is Vorbis?");
			vorbis.endOfStream = true;
			return false;
		}

		if (vorbis.streamState.packetout(vorbis.packet) != 1) {
			// no page? must not be vorbis
			System.out.println("Are you sure line 51 is Vorbis?");
			vorbis.endOfStream = true;
			return false;
		}

		if (vorbis.oggInfo.synthesis_headerin(vorbis.comment, vorbis.packet) < 0) {
			// error case; not a vorbis header
			System.out.println("Are you sure line 59 is Vorbis?");
			vorbis.endOfStream = true;
			return false;
		}
		// Read the comment and codebook headers
		int i = 0;
		while (i < 2) {
			while (i < 2) {
				int result = vorbis.syncState.pageout(vorbis.page);	// load a page
				if (result == 0)
					break; // Need more data
				if (result == 1) {
					vorbis.streamState.pagein(vorbis.page); // weld a logical OGG page together
					while (i < 2) {
						result = vorbis.streamState.packetout(vorbis.packet);	// weld a packet together
						if (result == 0)
							break;
						if (result == -1) {
							// Data at some point was corrupted or missing!
							vorbis.endOfStream = true;
							return false;
						}
						vorbis.oggInfo.synthesis_headerin(vorbis.comment, vorbis.packet);
						i++;
					}
				}
			}
			// no harm in not checking before adding more
			index = vorbis.syncState.buffer(4096);
			vorbis.buffer = vorbis.syncState.data;
			try {
				vorbis.bytes = vorbis.inputStream.read(vorbis.buffer, index, 4096);	// read the next block
			} catch (Exception e) {
				vorbis.endOfStream = true;
				return false;
			}
			if (vorbis.bytes == 0 && i < 2) {
				vorbis.endOfStream = true;
				return false;
			}
			vorbis.syncState.wrote(vorbis.bytes);
		}
		vorbis.convsize = 4096 / vorbis.oggInfo.channels;	// calculate the size of the conversion buffers
		// initialized the decoder
		vorbis.dspState.synthesis_init(vorbis.oggInfo); // central decode state
		vorbis.vorbisBlock.init(vorbis.dspState); // local state for most of the decode		
		return true;
	}

}
