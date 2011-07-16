package si.virag.arso;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

public class Datastore 
{
	private static Datastore instance;
	
	public static synchronized Datastore getInstance()
	{
		if (instance == null)
		{
			instance = new Datastore();
		}
		
		return instance;
	}
	
	private final PersistenceManagerFactory pFactory;
	
	private Datastore()
	{
		pFactory = JDOHelper.getPersistenceManagerFactory("transactions-optional");
	}
	
	public PersistenceManager getManager()
	{
		return pFactory.getPersistenceManager();
	}
}
