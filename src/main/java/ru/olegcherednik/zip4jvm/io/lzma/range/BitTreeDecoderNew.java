package ru.olegcherednik.zip4jvm.io.lzma.range;

public class BitTreeDecoderNew
{
	short[] Models;
	int NumBitLevels;

	public BitTreeDecoderNew(int numBitLevels)
	{
		NumBitLevels = numBitLevels;
		Models = new short[1 << numBitLevels];
	}

	public void init()
	{
		DecoderNew.InitBitModels(Models);
	}

	public int Decode(DecoderNew rangeDecoder) throws java.io.IOException
	{
		int m = 1;
		for (int bitIndex = NumBitLevels; bitIndex != 0; bitIndex--)
			m = (m << 1) + rangeDecoder.decodeBit(Models, m);
		return m - (1 << NumBitLevels);
	}

	public int ReverseDecode(DecoderNew rangeDecoder) throws java.io.IOException
	{
		int m = 1;
		int symbol = 0;
		for (int bitIndex = 0; bitIndex < NumBitLevels; bitIndex++)
		{
			int bit = rangeDecoder.decodeBit(Models, m);
			m <<= 1;
			m += bit;
			symbol |= (bit << bitIndex);
		}
		return symbol;
	}

	public static int ReverseDecode(short[] Models, int startIndex,
			DecoderNew rangeDecoder, int NumBitLevels) throws java.io.IOException
	{
		int m = 1;
		int symbol = 0;
		for (int bitIndex = 0; bitIndex < NumBitLevels; bitIndex++)
		{
			int bit = rangeDecoder.decodeBit(Models, startIndex + m);
			m <<= 1;
			m += bit;
			symbol |= (bit << bitIndex);
		}
		return symbol;
	}
}
