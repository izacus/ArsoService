package si.virag.arso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import si.virag.arso.data.WeatherData;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLine;
import ar.com.hjg.pngj.PngWriter;

public class ForecastForLocationServlet extends HttpServlet 
{
	private static final Logger log = Logger.getLogger(ForecastForLocationServlet.class.getName());

	private static final long serialVersionUID = 3885682648944820313L;

	public static final int[][] referencePixels = {
		{ 255, 255, 255 },			// CLEAR
		{ 0, 0, 120 },			// CLOUD 40
		{ 0, 0, 190 },			// CLOUD 60
		{ 0, 0, 240 },			// CLOUD 80
		{ 0, 53, 0 },			// RAIN 1
		{ 0, 70, 0 }, 			// RAIN 2
		{ 0, 120, 0 },			// RAIN 5
		{ 0, 130, 0 },			// RAIN 10
		{ 0, 150, 0 },			// RAIN 20
		{ 0, 170, 0 },			// RAIN 30
		{ 0, 190, 0 },			// RAIN 50
		{ 0, 200, 0 },			// RAIN 60
		{ 0, 210, 0 },			// RAIN 70
		{ 0, 230, 0 },			// RAIN 80
		{ 0, 255, 0 }			// RAIN 100
	};
	
	private static WeatherData data;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{		
		try
		{
			PersistenceManager manager = Datastore.getInstance().getManager();
						
			if (data == null || Calendar.getInstance().getTimeInMillis() - data.getUpdateTime().getTime() > 1000 * 60 * 30)
			{
				log.info("Cached data out of date, getting new.");
				data = Datastore.getInstance().getManager().getObjectById(WeatherData.class, WeatherData.KEY);
			}
			
			manager.close();
		}
		catch (JDOObjectNotFoundException e)
		{
			resp.getWriter().print("Data not available.");
			return;
		}
		
		byte[] image = createImage(data.getImages().get(0).getLocationData());
		
		resp.setContentType("image/png");
		
		OutputStream stream = resp.getOutputStream();
		stream.write(image);
	}
	
	private byte[] createImage(byte[][] locationData)
	{
		ImageInfo info = new ImageInfo(locationData.length, locationData[0].length, 8, false);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		PngWriter writer = new PngWriter(os, info);
		writer.doInit();
		
		for (int y = 0; y < locationData[0].length; y++)
		{
			ImageLine line = new ImageLine(info);
			line.setRown(y);
			
			for (int x = 0; x < locationData.length; x ++ )
			{
				line.scanline[x * 3] = referencePixels[locationData[x][y]][0];
				line.scanline[(x * 3) + 1] = referencePixels[locationData[x][y]][1];
				line.scanline[(x * 3) + 2] = referencePixels[locationData[x][y]][2];
			}
			
			writer.writeRow(line);
		}
		
		writer.end();
		
		return os.toByteArray();
	}
	
	
}
