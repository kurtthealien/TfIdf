package com.kurtthealien.tfidf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.kurtthealien.tfidf.FileMonitor;
import com.kurtthealien.tfidf.impl.FileMonitorImpl;

import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TemporaryFolder;

@RunWith(Enclosed.class)
public class FileMonitorTest {
	
	private static final Logger logger = Logger.getLogger("TfIdfLog");
	
	private static String createNewTempFolder(TemporaryFolder tempRootFolder) {
		String destinationTempPath;
		
		try {
			String folderName = "files";
			File tempFolder = tempRootFolder.newFolder(folderName);
			destinationTempPath = tempFolder.getAbsolutePath();
			logger.info("File temp path: " + destinationTempPath);
		} catch( IOException ioEx ) {
            logger.error("Error creating temporary directory: " + ioEx.getMessage());
            destinationTempPath = "";
		}
			
		return destinationTempPath;
	}
	
	private static Boolean copyTestFiles(String sourcePath, String destinationPath, List<String> filesToCopy) {
		try {
	    	for (String testFile: filesToCopy ) {
	    		String resourceFile = sourcePath + File.separator + testFile;
	    		String destinationFile = destinationPath + File.separator + testFile;
	    		logger.info("Copying test file from: " + resourceFile + " to " + destinationFile);
	    		
				Files.copy(Paths.get(resourceFile), Paths.get(destinationFile));
            	TimeUnit.MILLISECONDS.sleep(10);
	    	}
	    	
	    	File targetDir = new File(destinationPath);
	    	File[] files = targetDir.listFiles();
	    	for (File file: files) {
	    		logger.info("File copied: " + file.toString());
	    	}
		} catch( IOException ioEx ) {
			logger.error("Error copying file: " + ioEx.getMessage());
            return false;
		} catch (InterruptedException intEx) {
	    	logger.error("Error executing delay: " + intEx.getMessage());
	    	return false;
	    }

		return true;
	}
	
	@RunWith(Parameterized.class)
	public static class TestCurrentFiles {
		
		FileMonitor fileMonitor;
		List<String> copiedFiles;
		List<String> expectedFiles;
		
		String absoluteResourcesPath;
		String absoluteTempPath;
		
		public TestCurrentFiles(String[] copiedFileList, String[] expectedFileList) {
			
			fileMonitor = new FileMonitorImpl();
			
			copiedFiles = Arrays.asList(copiedFileList);
			expectedFiles = Arrays.asList(expectedFileList);
			
			Path resourceDirectory = Paths.get("src","test","resources");
			absoluteResourcesPath = resourceDirectory.toFile().getAbsolutePath();
			logger.info("File resources path: " + absoluteResourcesPath);
		}
		
		/* This folder and the files created in it will be deleted after
	     * tests are run, even in the event of failures or exceptions.
	     */
	    @Rule
	    public TemporaryFolder tempRootFolder = new TemporaryFolder();

		@Parameterized.Parameters
		public static Collection<?> testCases() {
			return Arrays.asList(new String[][][] {
				{{}, {}},
				{{"alien.jpg"}, {}},
				{{"file1.txt"}, {"file1.txt"}},
				{{"file1.txt", "alien.jpg"}, {"file1.txt"}},
				{{"file1.txt", "file2.txt"}, {"file1.txt", "file2.txt"}},
				{{"file1.txt", "file2.txt", "alien.jpg"}, {"file1.txt", "file2.txt"}},
				{{"file1.txt", "file2.txt", "file3.txt"}, {"file1.txt", "file2.txt", "file3.txt"}},
				{{"file1.txt", "file2.txt", "file3.txt", "alien.jpg"}, {"file1.txt", "file2.txt", "file3.txt"}},
				{{"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}},
				{{"file1.txt", "file2.txt", "file3.txt", "file4.txt", "alien.jpg"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}}
			});
		}

		@Test
		public void testCurrentFiles() {
			logger.info("Testing file monitor with existing files " + copiedFiles.toString());
			
			String absoluteTempPath = createNewTempFolder(tempRootFolder);
			assertNotEquals("", absoluteTempPath);
			assertTrue(copyTestFiles(absoluteResourcesPath, absoluteTempPath, copiedFiles));
			
			fileMonitor.configure(Paths.get(absoluteTempPath));
			
			List<Path> currentTextFiles = fileMonitor.retrieveCurrentTextFiles();
			
			long realFileNumber = currentTextFiles.size();
			long expectedFileNumber = expectedFiles.size();
			logger.info("Expecting " + expectedFileNumber + " files, retrieved " + realFileNumber);
			assertEquals(expectedFileNumber, realFileNumber);
			
			logger.info("Retrieved " + currentTextFiles.toString());
			
			List<String> currentFileNameList = currentTextFiles.stream()
                    .map(file -> file.getFileName().toString())
                    .collect(Collectors.toList());
			
			logger.info("Comparing expected " + expectedFiles.toString() + " with retrieved and simplified " + currentFileNameList.toString());			
			
			assertTrue(Arrays.equals(expectedFiles.toArray(), currentFileNameList.toArray()));
		}
	 }
	
	@RunWith(Parameterized.class)
	public static class TestNewFiles {
		
		FileMonitor fileMonitor;
		List<String> initialFiles;
		List<String> copiedFiles;
		List<String> expectedFiles;
		
		String absoluteResourcesPath;
		String absoluteTempPath;
		
		public TestNewFiles(String[] initialFileList, String[] copiedFileList, String[] expectedFileList) {
			
			fileMonitor = new FileMonitorImpl();
			
			initialFiles = Arrays.asList(initialFileList);
			copiedFiles = Arrays.asList(copiedFileList);
			expectedFiles = Arrays.asList(expectedFileList);
			
			Path resourceDirectory = Paths.get("src","test","resources");
			absoluteResourcesPath = resourceDirectory.toFile().getAbsolutePath();
			logger.info("File resources path: " + absoluteResourcesPath);
		}
		
		/* This folder and the files created in it will be deleted after
	     * tests are run, even in the event of failures or exceptions.
	     */
	    @Rule
	    public TemporaryFolder tempRootFolder = new TemporaryFolder();

		@Parameterized.Parameters
		public static Collection<?> testCases() {
			return Arrays.asList(new String[][][] {
				{{}, {"alien.jpg"}, {}},
				{{}, {"file1.txt", "alien.jpg"}, {"file1.txt"}},
				{{}, {"file1.txt", "file2.txt", "alien.jpg"}, {"file1.txt", "file2.txt"}},
				{{}, {"file1.txt", "file2.txt", "file3.txt", "alien.jpg"}, {"file1.txt", "file2.txt", "file3.txt"}},
				{{}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt", "alien.jpg"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}},
				{{"file1.txt"}, {"alien.jpg"}, {}},
				{{"file1.txt", "file2.txt"}, {"alien.jpg"}, {}},
				{{"file1.txt", "file2.txt", "file3.txt"}, {"alien.jpg"}, {}},
				{{"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"alien.jpg"}, {}},
				{{"alien.jpg"}, {"file1.txt"}, {"file1.txt"}},
				{{"alien.jpg"}, {"file1.txt", "file2.txt"}, {"file1.txt", "file2.txt"}},
				{{"alien.jpg"}, {"file1.txt", "file2.txt", "file3.txt"}, {"file1.txt", "file2.txt", "file3.txt"}},
				{{"alien.jpg"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}}
			});
		}

		@Test
		public void testNewFiles() {
			logger.info("Testing file monitor with existing files " + initialFiles.toString());
			
			String absoluteTempPath = createNewTempFolder(tempRootFolder);
			assertNotEquals("", absoluteTempPath);
			assertTrue(copyTestFiles(absoluteResourcesPath, absoluteTempPath, initialFiles));
			
			fileMonitor.configure(Paths.get(absoluteTempPath));
			
			assertTrue(copyTestFiles(absoluteResourcesPath, absoluteTempPath, copiedFiles));
			
			List<Path> newTextFiles = fileMonitor.retrieveNewTextFiles();

			long realFileNumber = newTextFiles.size();
			long expectedFileNumber = expectedFiles.size();
			logger.info("Expecting " + expectedFileNumber + " files, retrieved " + realFileNumber);
			assertEquals(expectedFileNumber, realFileNumber);
			
			logger.info("Retrieved " + newTextFiles.toString());
			
			List<String> currentFileNameList = newTextFiles.stream()
                    .map(file -> file.getFileName().toString())
                    .collect(Collectors.toList());
			
			assertTrue(Arrays.equals(expectedFiles.toArray(), currentFileNameList.toArray()));			
			logger.info("Comparing expected " + expectedFiles.toString() + " with retrieved and simplified " + currentFileNameList.toString());			
		}
	 }
}