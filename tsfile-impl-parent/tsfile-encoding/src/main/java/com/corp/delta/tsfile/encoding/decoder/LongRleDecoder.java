package com.corp.delta.tsfile.encoding.decoder;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corp.delta.tsfile.common.exception.TSFileDecodingException;
import com.corp.delta.tsfile.common.utils.ReadWriteStreamUtils;
import com.corp.delta.tsfile.encoding.bitPacking.LongPacker;
import com.corp.delta.tsfile.encoding.common.EndianType;


/**
 * @Description Decoder for long value using rle or bit-packing
 * @author XuYi xuyi556677@163.com
 * @date Mar 25, 2016
 */
public class LongRleDecoder extends RleDecoder{
	private static final Logger LOGGER = LoggerFactory.getLogger(LongRleDecoder.class);
	
	/**
	 * current value for rle repeated value
	 */
	private long currentValue;

	/**
	 * buffer to save all values in group using bit-packing
	 */
	private long[] currentBuffer;

	/**
	 * packer for unpacking long value
	 */
	private LongPacker packer;
	
	public LongRleDecoder(EndianType endianType) {
		super(endianType);
		currentValue = 0;
	}
	
	/**
	 * read a long value from InputStream
	 * 
	 * @param in - InputStream
	 * @return value - current valid value
	 */
	@Override
	public long readLong(InputStream in) {
		if(!isLengthAndBitWidthReaded){
	        //start to read a new rle+bit-packing pattern
			try {
				readLengthAndBitWidth(in);
			} catch (IOException e) {
				LOGGER.error("tsfile-encoding IntRleDecoder: error occurs when reading length",e);
			}
		}
		
		if (currentCount == 0) {
			try {
				readNext();
			} catch (IOException e) {
			    LOGGER.error("tsfile-encoding IntRleDecoder: error occurs when reading all encoding number, length is {}, bit width is {}",length,bitWidth,e);
			}
		}
		--currentCount;
		long result = 0;
		switch (mode) {
		case RLE:
			result = currentValue;
			break;
		case BIT_PACKED:
			result = currentBuffer[bitPackingNum - currentCount - 1];
			break;
		default:
			throw new TSFileDecodingException(String.format("tsfile-encoding LongRleDecoder: not a valid mode %s", mode));
		}
		
		if(!hasNextPackage()){
			isLengthAndBitWidthReaded = false;
		}
		return result;
	}

	@Override
	protected void initPacker() {
		packer = new LongPacker(bitWidth);
	}

	@Override
	protected void readNumberInRLE() throws IOException {
		currentValue = ReadWriteStreamUtils.readLongLittleEndianPaddedOnBitWidth(byteCache, bitWidth);
	}

	@Override
	protected void readBitPackingBuffer(int bitPackedGroupCount,int lastBitPackedNum) throws IOException {
		currentBuffer = new long[bitPackedGroupCount * config.RLE_MIN_REPEATED_NUM];
		byte[] bytes = new byte[bitPackedGroupCount * bitWidth];
		int bytesToRead = bitPackedGroupCount * bitWidth;
		bytesToRead = Math.min(bytesToRead, byteCache.available());
//		new DataInputStream(byteCache).readFully(bytes, 0, bytesToRead);
		byteCache.read(bytes, 0, bytesToRead);
		
		// save all long values in currentBuffer
		packer.unpackAllValues(bytes, 0, bytesToRead, currentBuffer);
	}
}
