package uk.ac.imperial.pipe.io;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class FileUtils {

	
	
	
	public static String fileLocation(String path) {
		String location = resourceLocation(path); 
		if (location == null) {
			System.out.println("else..."+path);
			location = path;
			File file = new File(location);
			if (file.exists()) {
				String normalized = file.toURI().getPath(); 
				location = normalized; 
				System.out.println("normalized..."+location);
			} else {
				location = System.getProperty("user.dir") + File.separator + path; 
				System.out.println("userdir..."+location);
			}
		}
		return location;
	}
    public static String resourceLocation(String path) {
    	String location = null; 
    	Path newPath = null;
    	URL url = FileUtils.class.getResource(path); 
    	if (url != null) {
    		try {
    			newPath = Paths.get(url.toURI()); // escape characters URL doesn't handle
    		} catch (URISyntaxException e) {
    			System.out.println("URI syntax exception: "+path+"\n"+e.toString());
    		} 
    		location = newPath.toString(); 
    	}
        return location;
    }
    public static File copyToWorkingDirectory(String path) throws IOException {
    	Path source = Paths.get(resourceLocation(path)); 
    	File file = new File(source.toString());
    	Path target = Paths.get(System.getProperty("user.dir")+File.separator+file.getName());
    	File newFile = new File(target.toString()); 
    	Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING); 
    	return newFile; 
    }
	public static File copyToWorkingDirectorySubdirectory(String directory,
			String path) throws IOException {
		Path source = Paths.get(resourceLocation(path)); 
		File file = new File(source.toString());
		File dir = buildSubDirectory(directory);
		String fullName = dir+File.separator+file.getName(); 
		System.out.println(fullName);
		Path target = Paths.get(fullName);
		System.out.println("target path: "+target);
		File newFile = new File(target.toUri()); 
//		File newFile = new File(target.toString()); 
		Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING); 
		return newFile; 
	}
	protected static File buildSubDirectory(String directory) {
		File dir = new File(System.getProperty("user.dir")+File.separator+directory); 
		if (!dir.exists()) dir.mkdir();
		return dir;
	}
}
