package uk.ac.imperial.pipe.io;

import java.net.URL;

public class FileUtils {

    public static String fileLocation(String path) {
    	String location; 
    	URL url = FileUtils.class.getResource(path); 
    	if (url != null) {
    		location = url.getPath(); 
    	}
    	else  location = path;
        return location;
    }
}
