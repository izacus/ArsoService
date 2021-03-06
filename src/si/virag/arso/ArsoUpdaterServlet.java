package si.virag.arso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import si.virag.arso.data.WeatherData;
import si.virag.arso.data.WeatherImage;

@SuppressWarnings("serial")
public class ArsoUpdaterServlet extends HttpServlet 
{
	private static final Logger log = Logger.getLogger(ArsoUpdaterServlet.class.getName());
	
	private static final String FETCH_URL = "http://meteo.arso.gov.si/uploads/probase/www/plus/timeline/timeline_aladin_tcc-rr_si-neighbours.xml";
	private static final String IMG_URL = "http://meteo.arso.gov.si/uploads/probase/www/model/aladin/field/";
	
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat imageDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm");


	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException 
	{
		long startTime = System.currentTimeMillis();
		
		resp.setContentType("text/plain");
		
		String receivedIndex = "";
		
		try
		{
			receivedIndex = getRemoteData(FETCH_URL);
		}
		catch (Exception e)
		{
			resp.getWriter().println("UPDATE ERROR");
			log.severe("Error retrieving files: " + e.getMessage());
		}
		
		String data = extractDataNode(receivedIndex);
		
		if (data != null)
		{
			ArrayList<WeatherImage> images = getImageUrls(data);
			ArrayList<WeatherImage> fetchedImages = new ArrayList<WeatherImage>();
			ArrayList<WeatherImage> parsedImages = new ArrayList<WeatherImage>();
			
			for (WeatherImage image : images)
			{
				try
				{
					image.fetch();
					fetchedImages.add(image);
				}
				catch (Exception e)
				{
					log.warning("Failed to fetch image");
					continue;
				}
			}
			
			for (WeatherImage image : fetchedImages)
			{
				try
				{
					image.parse();
					parsedImages.add(image);
				}
				catch (Exception e)
				{
					log.warning("Failed to parse image: " + e.getMessage());
					continue;
				}			
			}
			
			if (parsedImages.size() > 0)
				storeToDatastore(parsedImages);
			
			resp.getWriter().println("UPDATE OK");
			
			long endTime = System.currentTimeMillis();
			log.info("UPDATE OK, parsing took in total: " + (endTime - startTime) + " ms.");
			
			return;
		}
		
		resp.getWriter().print("UPDATE ERROR");
	}
	
	private void storeToDatastore(List<WeatherImage> images)
	{
		Date updateTime = Calendar.getInstance().getTime();
		
		WeatherData weatherData = new WeatherData(updateTime, images);
		PersistenceManager pManager = Datastore.getInstance().getManager();
			
		try
		{
			WeatherData oldData = pManager.getObjectById(WeatherData.class, WeatherData.KEY);
			pManager.deletePersistent(oldData);
		}
		catch (JDOObjectNotFoundException e)
		{}
		
		pManager.makePersistent(weatherData);
		pManager.close();
	}

	private ArrayList<WeatherImage> getImageUrls(String data)
	{
		ArrayList<WeatherImage> images = new ArrayList<WeatherImage>();
		
		Pattern pattern = Pattern.compile("timeline:\\[(.*)\\]");
		Matcher matcher = pattern.matcher(data);

		if (matcher.find())
		{
			// Replace "today" entries first
			String imageList = matcher.group(1).replaceAll("desc:T\\+", "desc:'" + format.format(Calendar.getInstance().getTime()) + "'+");
			
			Pattern imagePattern = Pattern.compile("\\{desc:'([0-9\\-]*?)'.*?\\+'([0-9]+:[0-9]+) CEST',url:IMG\\+'(.*?)'");
			Matcher imageMatcher = imagePattern.matcher(imageList);
			
			while(imageMatcher.find())
			{
				String dateTime = imageMatcher.group(1).trim() + ' ' + imageMatcher.group(2).trim();
				
				Date imageValid;
				
				try 
				{
					imageValid = imageDateTime.parse(dateTime);
				} catch (ParseException e) {
					log.severe("Failed to parse " + dateTime + " because of " + e.getMessage());
					continue;
				}
				
				WeatherImage image = new WeatherImage(imageValid, IMG_URL + imageMatcher.group(3));
				images.add(image);
			}
			
		}
		
		return images;
	}
	
	private String getRemoteData(String remoteUrl)
			throws IOException {
		// Get main forecast index "XML"
		try
		{
			StringBuilder receivedData = new StringBuilder(1024);
			
			URL url = new URL(remoteUrl);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			
			String line;
			
			while ((line = reader.readLine()) != null)
			{
				receivedData.append(line);
			}
			
			reader.close();
			return receivedData.toString();
		}
		catch (MalformedURLException url)
		{
			log.severe("FETCH_URL is malformed!");
			throw url;
		}
		catch (IOException e)
		{
			log.severe("ARSO service is offline!");
			log.severe(e.getMessage());
			throw e;
		}
	}
	
	private String extractDataNode(String xmlData)
	{
		try 
		{
			StringReader reader = new StringReader(xmlData);
			InputSource source = new InputSource(reader);
			
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);
			
			if (doc.getChildNodes().getLength() > 0)
			{
				return doc.getChildNodes().item(0).getTextContent();
			}
			else
			{
				log.severe("No XML nodes in received XML data!");
				return null;
			}
			
		} catch (Exception e)
		{
			log.severe("Error parsing XML data " + e.getMessage());
			return null;
		}
		
	}
}
