package si.virag.arso.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class WeatherData {
	public static final String KEY = "weatherdata";
	
	@SuppressWarnings("unused")
	@PrimaryKey
	private String weatherData;
	
	@Persistent
	private Date updateTime;
	@Persistent
	private List<WeatherImage> images;
	
	
	public WeatherData(Date updateTime, List<WeatherImage> images) {
		super();
		this.weatherData = KEY;
		
		this.updateTime = updateTime;
		this.images = images;
		
		Collections.sort(this.images, new Comparator<WeatherImage>() 
		{
			@Override
			public int compare(WeatherImage o1, WeatherImage o2) 
			{
				return o1.getValid().compareTo(o2.getValid());
			}
			
		});
	}


	public Date getUpdateTime() {
		return updateTime;
	}


	public List<WeatherImage> getImages() {
		return images;
	}
}
