package si.virag.arso;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.logging.Logger;

import ar.com.hjg.pngj.ImageLine;
import ar.com.hjg.pngj.PngChunk;
import ar.com.hjg.pngj.PngReader;

public class ImageProcessor {

	private static final Logger log = Logger.getLogger(ImageProcessor.class.getName());
	
	
	private final byte[] imageData;
	
	private static final int PAD_TOP = 50;
	private static final int PAD_BOTTOM = 140;
	private static final int PAD_LEFT = 160;
	private static final int PAD_RIGHT = 50;
	
	public static final int[][] referencePixels = {
		{ 200, 200, 200 },			// CLEAR
		{ 153, 160, 193 },			// CLOUD 40
		{ 178, 179, 219 },			// CLOUD 60
		{ 153, 160, 193 },			// CLOUD 80
		{ 253, 253, 153 },			// RAIN 1
		{ 227, 249, 127 }, 			// RAIN 2
		{ 202, 247, 108 },			// RAIN 5
		{ 156, 223, 100 },			// RAIN 10
		{ 139, 201, 99 },			// RAIN 20
		{ 50, 152, 74 },			// RAIN 30
		{ 31, 126, 65 },			// RAIN 50
		{ 19, 163, 110 },			// RAIN 60
		{ 16, 184, 135 },			// RAIN 70
		{ 15, 205, 158 },			// RAIN 80
		{ 60, 253, 204 }			// RAIN 100
	};
	
	public ImageProcessor(byte[] sourceImage) 
	{
		this.imageData = sourceImage;
	}
	
	public byte[][] processImage()
	{
		PngReader reader = new PngReader(new ByteArrayInputStream(imageData));

		int[][] palette = getPalette(reader.getChunks1());
		
		if (palette == null)
		{
			log.severe("Could not parse image palette!");
			return null;
		}
		
		byte[][] processedData = new byte[reader.imgInfo.samplesPerRow - (PAD_LEFT + PAD_RIGHT)][reader.imgInfo.rows - (PAD_TOP + PAD_BOTTOM)];
		
		for (int y = 0; y < reader.imgInfo.rows; y++)
		{	
			ImageLine line = reader.readRow(y);
			
			if (y < PAD_TOP || y >= (reader.imgInfo.rows - PAD_BOTTOM))
				continue;
			
			for (int x = PAD_LEFT; x < (line.len - PAD_RIGHT); x++)
			{	
				processedData[x - PAD_LEFT][y - PAD_TOP] = findClosest(palette[line.scanline[x]][0], palette[line.scanline[x]][1], palette[line.scanline[x]][2]);
			}
		}
		
		reader.end();
		
		return processedData;
	}
	
	private int[][] getPalette(List<PngChunk> chunks)
	{
		for (PngChunk chunk : chunks)
		{
			if (chunk.id.equals("PLTE"))
			{
				int[][] palette = new int[chunk.data.length / 3][3];
				
				int entry = 0;
				
				for (int i = 0; i < chunk.data.length; i += 3)
				{
					palette[entry][0] = (chunk.data[i] & 0xFF);
					palette[entry][1] = (chunk.data[i + 1] & 0xFF);
					palette[entry][2] = (chunk.data[i + 2] & 0xFF);
					entry++;
				}
				
				return palette;
			}
		}
		
		return null;
	}
	
	private byte findClosest(int r, int g, int b)
	{
		byte index = 0; long distance = Long.MAX_VALUE;
		
		for (byte i = 0; i < referencePixels.length; i++)
		{
			// Euclidean pixel distance
			long dist = Math.round(Math.pow(referencePixels[i][0] - r, 2) + Math.pow(referencePixels[i][1] - g, 2) + Math.pow(referencePixels[i][2] - b, 2));
			
			if (dist < distance)
			{
				distance = dist;
				index = i;
			}
		}
		
		return index;
	}
}
