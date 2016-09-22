package uk.ac.imperial.pipe.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

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
    	checkFileCopiedToWorkingDirectorySubdirectory("singleInclude.xml", "xml"); 
    }
    @Test
    public void copyToDirectorySubdirectoryWorksWithEmbeddedBlanks() throws Exception {
    	checkFileCopiedToWorkingDirectorySubdirectory("singleInclude.xml", "a b"); 
    }
	protected void checkFileCopiedToWorkingDirectorySubdirectory(String filename, String dirName)
			throws IOException {
		File file = new File(filename); 
    	assertFalse(file.exists()); 
    	File newFile = FileUtils.copyToWorkingDirectorySubdirectory(dirName,XMLUtils.getSingleIncludeHierarchyFile());  
    	checkAndCleanupDirectoryAndFile(file, newFile, dirName);
	}
	protected void checkAndCleanupDirectoryAndFile(File file, File newFile, String dirName) {
		assertTrue(newFile.exists());
    	assertTrue(newFile.delete());
    	File dir = new File(dirName); 
    	assertTrue(dir.exists()); 
    	assertTrue(dir.delete()); 
    	assertFalse(file.exists()); 
    	assertFalse(newFile.exists()); 
    	assertFalse(dir.exists());
	}

}
