package si.virag.arso;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.jdo.JDOObjectNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import si.virag.arso.data.WeatherData;
import si.virag.arso.data.WeatherImage;

public class ForecastForLocationServlet extends HttpServlet 
{
	private static final long serialVersionUID = 3885682648944820313L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		StringBuilder response = new StringBuilder();
		
		WeatherData data;
		
		try
		{
			data = Datastore.getInstance().getManager().getObjectById(WeatherData.class, WeatherData.KEY);
		}
		catch (JDOObjectNotFoundException e)
		{
			resp.getWriter().print("Data not available.");
			return;
		}
		
		SimpleDateFormat format = new SimpleDateFormat("HH:mm dd-MM-yyyy");
		response.append("Update time: " + format.format(data.getUpdateTime()) + "<br>");
		
		for (WeatherImage image : data.getImages())
		{
			response.append("<br>");
			response.append("Image validity: " + format.format(image.getValid()) + "<br>");
			response.append("Image url: " + image.getUrl() + "<br>");
			response.append("Image size: " + image.getLocationData().length + "x" + image.getLocationData()[0].length);
		}
		
		resp.setContentType("text/html");
		resp.getWriter().print(response.toString());
	}
	
	
}
