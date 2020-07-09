package com.everis.ws.integrator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AzureValues {

	InputStream inputStream;
 
	public String getPropValues() throws IOException {
 
		String aux="";
		try {
			Properties prop = new Properties();
			String propFileName = "prop.properties";
 
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
 
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
 
			
			// get the property value and print it out
			aux = prop.getProperty("token");
			} catch (Exception e) {
			
				System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}
		
		return aux;
	}
	
}
