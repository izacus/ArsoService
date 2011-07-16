package si.virag.arso.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import si.virag.arso.ImageProcessor;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

@PersistenceCapable
public class WeatherImage 
{
	private static final Logger log = Logger.getLogger(WeatherImage.class.getName());

	@SuppressWarnings("unused")
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key id;
	
	@Persistent
	private Date valid;
	@Persistent
	private String url;
	@Persistent(serialized = "true")
	private byte[][] locationData;
	
	@NotPersistent
	private Future<HTTPResponse> fetchResult;
	
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
	
	public byte[][] getLocationData() {
		return locationData;
	}
	
	public void fetch() throws MalformedURLException, IOException
	{
		URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();
		URL imageURL = new URL(this.url);
		fetchResult = fetcher.fetchAsync(imageURL);
	}
	
	public void parse() throws IOException
	{
		long startTime = System.currentTimeMillis();

		HTTPResponse response;
		
		try 
		{
			response = fetchResult.get();
		} catch (InterruptedException e) {
			throw new IOException(e.getMessage());
		} catch (ExecutionException e) {
			throw new IOException(e.getMessage());
		}
		
		byte[] data = response.getContent();
		
		ImageProcessor processor = new ImageProcessor(data);
		this.locationData = processor.processImage();
		
		if (getLocationData() == null)
			throw new IOException("Image was not parsed successfully.");
		
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
