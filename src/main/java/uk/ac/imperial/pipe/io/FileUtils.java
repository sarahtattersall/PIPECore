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
        if (location != null)
            return location;
        location = getNormalizedLocation(path);
        if (location != null)
            return location;
        location = getWorkingDirectoryLocation(path);
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
                System.out.println("URI syntax exception: " + path + "\n" + e.toString());
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
        Path target = Paths.get(directoryName, source.getFileName().toString());
        File newFile = new File(target.toUri());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        return newFile;
    }

    private static String getWorkingDirectoryLocation(String location) {
        Path path = Paths.get(System.getProperty("user.dir"), location);
        return getNormalizedLocation(path.toString());
    }

    protected static File buildSubDirectory(String directory) {
        File dir = new File(System.getProperty("user.dir") + File.separator + directory);
        if (!dir.exists())
            dir.mkdir();
        return dir;
    }

    protected static String getNormalizedLocation(String location) {
        String normalized = null;
        File file = new File(location);
        if (file.exists()) {
            normalized = file.toURI().getPath();
        }
        return normalized;
    }
}
