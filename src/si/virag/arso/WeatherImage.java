package si.virag.arso;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;

public class WeatherImage 
{
	private static final Logger log = Logger.getLogger(WeatherImage.class.getName());

	
	private Date valid;
	private String url;
	
	public WeatherImage(Date valid, String url)
	{
		this.valid = valid;
		this.url = url;
	}

	public Date getValid() 
	{
		return valid;
	}

	public String getUrl() 
	{
		return url;
	}
	
	public void fetch() throws MalformedURLException, IOException
	{
		long startTime = System.currentTimeMillis();
		URL imageURL = new URL(this.url);
		HttpURLConnection conn = (HttpURLConnection) imageURL.openConnection();
		
		byte[] data = new byte[conn.getContentLength()];
		
		DataInputStream stream = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
		stream.read(data, 0, conn.getContentLength());
		
		ImageProcessor processor = new ImageProcessor(data);
		processor.processImage();
		
		log.info("Image from " + this.url + "(" + conn.getContentLength() + "B) received successfully.");
		
		long endTime = System.currentTimeMillis();
		
		log.info("Parsing took " + (endTime - startTime) + " ms." );
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((valid == null) ? 0 : valid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WeatherImage other = (WeatherImage) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (valid == null) {
			if (other.valid != null)
				return false;
		} else if (!valid.equals(other.valid))
			return false;
		return true;
	}
}
