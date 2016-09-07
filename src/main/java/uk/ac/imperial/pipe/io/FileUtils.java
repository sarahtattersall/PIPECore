package uk.ac.imperial.pipe.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class FileUtils {

    public static String fileLocation(String path) {
    	String location; 
    	URL url = FileUtils.class.getResource(path); 
    	if (url != null) {
    		location = url.getPath(); 
    	}
    	else  {
    		location = path;
    		File file = new File(location);
    		if (!file.exists()) {
    			location = System.getProperty("user.dir") + File.separator + path; 
    		}
    	}
        return location;
    }
    public static File copyToWorkingDirectory(String path) throws IOException {
    	Path source = Paths.get(fileLocation(path)); 
    	File file = new File(source.toString());
    	Path target = Paths.get(System.getProperty("user.dir")+File.separator+file.getName());
    	File newFile = new File(target.toString()); 
    	Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING); 
    	return newFile; 
    }
	public static File copyToWorkingDirectorySubdirectory(String directory,
			String path) throws IOException {
		Path source = Paths.get(fileLocation(path)); 
		File file = new File(source.toString());
		File dir = new File(System.getProperty("user.dir")+File.separator+directory); 
		if (!dir.exists()) dir.mkdir(); 
		Path target = Paths.get(dir+File.separator+file.getName());
		File newFile = new File(target.toString()); 
		Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING); 
		return newFile; 
	}
}
