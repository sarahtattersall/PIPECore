package uk.ac.imperial.pipe.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
    		System.out.println("url: "+url);
    		try {
    			newPath = Paths.get(url.toURI()); // escape characters URL doesn't handle
    			System.out.println("new path: "+newPath);
    		} catch (URISyntaxException e) {
    			System.out.println("URI syntax exception: "+path+"\n"+e.toString());
    		} 
    		location = newPath.toString(); 
    	}
        return location;
    }
    public static File copyToWorkingDirectory(String path) throws IOException {
    	return copyToDirectory(System.getProperty("user.dir"), path);
    }
	public static File copyToWorkingDirectorySubdirectory(String directory,
			String path) throws IOException {
		File dir = buildSubDirectory(directory);
		return copyToDirectory(dir.getAbsolutePath(), path);
	}
	public static File copyToDirectory(String directoryName,
			String path) throws IOException {
		Path source = Paths.get(resourceLocation(path)); 
		Path target = Paths.get(directoryName,source.getFileName().toString());
		File newFile = new File(target.toUri()); 
		Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING); 
		return newFile; 
	}
	protected static File buildSubDirectory(String directory) {
		File dir = new File(System.getProperty("user.dir")+File.separator+directory); 
		if (!dir.exists()) dir.mkdir();
		return dir;
	}
	public static FileReader getFileReader(String location) {
		FileReader reader = null;
		File file = new File(location);
		if (file.exists()) {
			String normalized = file.toURI().getPath(); 
			try {
				reader = new FileReader(normalized);
			} catch (FileNotFoundException e) {
			} 
		}
		return reader; 
	}
}
