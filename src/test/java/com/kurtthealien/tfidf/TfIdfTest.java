package com.kurtthealien.tfidf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.kurtthealien.tfidf.TfIdf;
import com.kurtthealien.tfidf.impl.TfIdfImpl;

import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TemporaryFolder;

@RunWith(Enclosed.class)
public class TfIdfTest {
	
	private static final Logger logger = Logger.getLogger("TfIdfLog");
	
	public static class CheckParameterLists {
		List<String> terms = Arrays.asList("word1", "word2", "word3");
		List<String> files = Arrays.asList("file1.txt", "file2.txt", "file3.txt","file4.txt");

		@Test
		public void checkTermList() {
			logger.info("Testing term registration without files: " + terms.toString());
			
			TfIdf tfidf = new TfIdfImpl();
			
			for(String term: terms) {
				assertEquals(true, tfidf.addNewTerm(term));
			}
			
			List<String> termList = tfidf.getTermList(); 
			long numTerms = termList.size();
			
			logger.info("Terms registered: " + termList.toString());
			
			logger.info("Expecting 3 words, retrieved " + numTerms);
			assertEquals(3, numTerms);
			assertTrue(terms.equals(termList));
		}
		
		@Test
		public void checkNoFilesWithoutTerms() {
			logger.info("Testing file registration without terms: " + files.toString());
			
			TfIdf tfidf = new TfIdfImpl();
						
			for(String file: files) {
				assertEquals(false, tfidf.addNewFile(Paths.get(file)));
			}
			
			List<Path> fileList = tfidf.getFileList(); 
			long numFiles = fileList.size();
			
			logger.info("Terms registered: " + fileList.toString());
			
			logger.info("Expecting 0 files, retrieved " + numFiles);
			assertEquals(0, numFiles);
		}
		
		@Test
		public void checkTermsAndFiles() {
			logger.info("Testing term registration without files: " + terms.toString());
			
			TfIdf tfidf = new TfIdfImpl();
			
			for(String term: terms) {
				assertEquals(true, tfidf.addNewTerm(term));
			}
			
			for(String file: files) {
				assertEquals(true, tfidf.addNewFile(Paths.get(file)));
			}
			
			List<String> termList = tfidf.getTermList(); 
			long numTerms = termList.size();
			
			logger.info("Terms registered: " + termList.toString());
			
			logger.info("Expecting 3 words, retrieved " + numTerms);
			assertEquals(3, numTerms);
			assertTrue(terms.equals(termList));
			
			List<Path> fileList = tfidf.getFileList(); 
			long numFiles = fileList.size();
			
			logger.info("Expecting 4 files, retrieved " + numFiles);
			assertEquals(4, numFiles);
		}
	 }
	
	@RunWith(Parameterized.class)
	public static class TestAlgorithm {
		
		TfIdf tfidf;
		List<String> terms;
		List<String> files;
		List<Double> tfidfValues;
		int resultsRequested;
		int resultsShown;
		
		public TestAlgorithm(String[] termList, String[] fileList, String[] tfidfList, String[] resultNumber) {
			
			tfidf = new TfIdfImpl();
			
			terms = Arrays.asList(termList);
			files = Arrays.asList(fileList);
			tfidfValues = new ArrayList<>();
			for (String tfidf: tfidfList ) {
				tfidfValues.add(Double.parseDouble(tfidf));
			}
			resultsRequested = Integer.parseInt(resultNumber[0]);
			resultsShown = Integer.parseInt(resultNumber[1]);
		}
		
		/* This folder and the files created in it will be deleted after
	     * tests are run, even in the event of failures or exceptions.
	     */
	    @Rule
	    public TemporaryFolder tempRootFolder = new TemporaryFolder();

		@Parameterized.Parameters
		public static Collection<?> testCases() {
			return Arrays.asList(new String[][][] {
				{{"password"}, {"file1.txt", "file2.txt"}, {"0.602", "0.0"}, {"3", "2"}},
				{{"password"}, {"file1.txt", "file2.txt"}, {"0.602", "0.0"}, {"2", "2"}},
				{{"password"}, {"file1.txt", "file2.txt"}, {"0.602"}, {"1", "1"}},
				{{"password"}, {"file1.txt", "file2.txt", "file3.txt"}, {"0.3522", "0.1761", "0.0"}, {"4", "3"}},
				{{"password"}, {"file1.txt", "file2.txt", "file3.txt"}, {"0.3522", "0.1761", "0.0"}, {"3", "3"}},
				{{"password"}, {"file1.txt", "file2.txt", "file3.txt"}, {"0.3522", "0.1761"}, {"2", "2"}},
				{{"password"}, {"file1.txt", "file2.txt", "file3.txt"}, {"0.3522"}, {"1", "1"}},
				{{"password"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"0.2498", "0.1249", "0.1249", "0.0"}, {"5", "4"}},
				{{"password"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"0.2498", "0.1249", "0.1249", "0.0"}, {"4", "4"}},
				{{"password"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"0.2498", "0.1249", "0.1249"}, {"3", "3"}},
				{{"password"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"0.2498", "0.1249"}, {"2", "2"}},
				{{"password"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"0.2498"}, {"1", "1"}},
				{{"password", "try"}, {"file1.txt", "file2.txt"}, {"0.4515", "0.0"}, {"2", "2"}},
				{{"password", "try"}, {"file1.txt", "file2.txt"}, {"0.4515"}, {"1", "1"}},
				{{"password", "try"}, {"file1.txt", "file2.txt", "file3.txt"}, {"0.2641", "0.1761", "0.0"}, {"4", "3"}},
				{{"password", "try"}, {"file1.txt", "file2.txt", "file3.txt"}, {"0.2641", "0.1761", "0.0"}, {"3", "3"}},
				{{"password", "try"}, {"file1.txt", "file2.txt", "file3.txt"}, {"0.2641", "0.1761"}, {"2", "2"}},
				{{"password", "try"}, {"file1.txt", "file2.txt", "file3.txt"}, {"0.2641"}, {"1", "1"}},
				{{"password", "try"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"0.1873", "0.1249", "0.1249", "0.0"}, {"5", "4"}},
				{{"password", "try"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"0.1873", "0.1249", "0.1249", "0.0"}, {"4", "4"}},
				{{"password", "try"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"0.1873", "0.1249", "0.1249"}, {"3", "3"}},
				{{"password", "try"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"0.1873", "0.1249"}, {"2", "2"}},
				{{"password", "try"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"0.1873"}, {"1", "1"}},
				{{"password", "try", "again"}, {"file1.txt", "file2.txt"}, {"0.301", "0.1003"}, {"2", "2"}},
				{{"password", "try", "again"}, {"file1.txt", "file2.txt"}, {"0.301"}, {"1", "1"}},
				{{"password", "try", "again"}, {"file1.txt", "file2.txt", "file3.txt"}, {"0.2348", "0.1761", "0.0587"}, {"4", "3"}},
				{{"password", "try", "again"}, {"file1.txt", "file2.txt", "file3.txt"}, {"0.2348", "0.1761", "0.0587"}, {"3", "3"}},
				{{"password", "try", "again"}, {"file1.txt", "file2.txt", "file3.txt"}, {"0.2348", "0.1761"}, {"2", "2"}},
				{{"password", "try", "again"}, {"file1.txt", "file2.txt", "file3.txt"}, {"0.2348"}, {"1", "1"}},
				{{"password", "try", "again"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"0.1665", "0.1665", "0.1249", "0.0416"}, {"5", "4"}},
				{{"password", "try", "again"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"0.1665", "0.1665", "0.1249", "0.0416"}, {"4", "4"}},
				{{"password", "try", "again"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"0.1665", "0.1665", "0.1249"}, {"3", "3"}},
				{{"password", "try", "again"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"0.1665", "0.1665"}, {"2", "2"}},
				{{"password", "try", "again"}, {"file1.txt", "file2.txt", "file3.txt", "file4.txt"}, {"0.1665"}, {"1", "1"}}
			});
		}
		
		private Boolean addTestFiles() {
			
			try {
				Path resourceDirectory = Paths.get("src","test","resources");
				String absoluteResourcesPath = resourceDirectory.toFile().getAbsolutePath();
				logger.info("File resources path: " + absoluteResourcesPath);
				
				String folderName = "files";
				File tempFolder = tempRootFolder.newFolder(folderName);
				String absoluteTempPath = tempFolder.getAbsolutePath();
				logger.info("File temp path: " + absoluteTempPath);
	    	
	        	for (String testFile: files ) {
	        		String resourceFile = absoluteResourcesPath + File.separator + testFile;
	        		String destinationFile = absoluteTempPath + File.separator + testFile;
	        		logger.info("Copying test file from: " + resourceFile + " to " + destinationFile);
	        		
	    			Files.copy(Paths.get(resourceFile), Paths.get(destinationFile));
    				tfidf.addNewFile(Paths.get(destinationFile));
	        	}
	        	
	        	String[] actual = tempFolder.list();
	        	logger.info("Test files listing: " + actual.length);
	        	
			} catch( IOException ioEx ) {
	            System.err.println("Error listing files in temporary directory: " + ioEx.getMessage());
	            return false;
			}
        	
        	return true;
		}

		@Test
		public void testTfIdfCalculation() {
			logger.info("Testing tfidf algorithm with terms " + terms.toString() + " and files: " + files.toString());
			
			for (String term: terms ) {
				tfidf.addNewTerm(term);
			}
			
			long realTermNumber = tfidf.getTermList().size(); 
			long expectedTermNumber = terms.size();
			logger.info("Expecting " + expectedTermNumber + " words, retrieved " + realTermNumber);
			assertEquals(expectedTermNumber, realTermNumber);
			
			assertTrue(addTestFiles());
        	
			long realFileNumber = tfidf.getFileList().size(); 
			long expectedFileNumber = files.size();
			logger.info("Expecting " + expectedFileNumber + " files, retrieved " + realFileNumber);
			assertEquals(expectedFileNumber, realFileNumber);
			
			Map<Path,Double> tfidfResults = tfidf.retrieveResults(true, resultsRequested);
			logger.info("Requested " + resultsRequested + ", expecting " + resultsShown + ", retrieved " + tfidfResults.size() + " results");
			assertEquals(resultsShown, tfidfResults.size());
			
			logger.info("Comparing expected " + tfidfValues.toString() + " with retrieved " + tfidfResults.values().toString());
			
			assertTrue(Arrays.equals(tfidfValues.toArray(), tfidfResults.values().toArray()));
		}
	 }
}