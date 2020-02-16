package uk.ac.imperial.pipe.io;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class FileUtilsTest {

    @Test
    public void copyToWorkingDir() throws Exception {
        File file = new File("singleInclude.xml");
        assertFalse(file.exists());
        File newFile = FileUtils.copyToWorkingDirectory(XMLUtils.getSingleIncludeHierarchyFile());
        assertTrue(newFile.exists());
        assertTrue(newFile.delete());
        assertFalse(file.exists());
        assertFalse(newFile.exists());
    }

    @Test
    public void copyToWorkingDirectorySubdirectory() throws Exception {
        File newFile = copyFileToWorkingDirectorySubdirectory("xml");
        cleanupFileAndDirectory("xml", newFile);
    }

    @Test
    public void copyToDirectorySubdirectoryWorksWithEmbeddedBlanks() throws Exception {
        File newFile = copyFileToWorkingDirectorySubdirectory("a b");
        cleanupFileAndDirectory("a b", newFile);
    }

    @Test
    public void verifyFormattedFilenamesResolveToActualFile() throws Exception {
        File newFile = copyFileToWorkingDirectorySubdirectory("a b");
        assertTrue("URI starts with file:", newFile.toURI().toString().startsWith("file:"));
        assertTrue("URI escapes blanks", newFile.toURI().toString().endsWith("a%20b/singleInclude.xml"));
        assertTrue("path restores blank", newFile.toURI().getPath().endsWith("a b/singleInclude.xml"));
        assertFalse("path removes file: prefix", newFile.toURI().getPath().startsWith("file:"));
        assertNotNull(FileUtils.getNormalizedLocation("a b/singleInclude.xml"));
        assertNull(FileUtils.getNormalizedLocation("a%20b/singleInclude.xml"));
        cleanupFileAndDirectory("a b", newFile);
    }

    protected File copyFileToWorkingDirectorySubdirectory(String dirName)
            throws IOException {
        File newFile = FileUtils.copyToWorkingDirectorySubdirectory(dirName, XMLUtils.getSingleIncludeHierarchyFile());
        assertTrue(newFile.exists());
        return newFile;
    }

    protected void cleanupFileAndDirectory(String dirName, File newFile) {
        assertTrue(newFile.delete());
        File dir = new File(dirName);
        assertTrue(dir.exists());
        assertTrue(dir.delete());
        assertFalse(newFile.exists());
        assertFalse(dir.exists());
    }

}
