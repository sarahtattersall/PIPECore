package uk.ac.imperial.pipe.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

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
    	File file = new File("singleInclude.xml"); 
    	assertFalse(file.exists()); 
    	File newFile = FileUtils.copyToWorkingDirectorySubdirectory("xml",XMLUtils.getSingleIncludeHierarchyFile());  
    	assertTrue(newFile.exists());
    	assertTrue(newFile.delete());
    	File dir = new File("xml"); 
    	assertTrue(dir.exists()); 
    	assertTrue(dir.delete()); 
    	assertFalse(file.exists()); 
    	assertFalse(newFile.exists()); 
    	assertFalse(dir.exists()); 
    }

}
