package com.kurtthealien.tfidf.impl;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.kurtthealien.tfidf.TfIdf;

public class TfIdfImpl implements TfIdf {

	private final int ROUND_SCALE_DECIMALS = 4;
	
	private Map<String, Double> keywords;   //map containing keywords and their updated idf
	
	private Map<Path, Map<String, Long>> termOcurrencesPerFile;
	
	Map<Path, Double> weightedFileList;
	
	private static final Logger logger = Logger.getLogger("TfIdfLog");
	
	public TfIdfImpl() {
		keywords = new LinkedHashMap<>();

		termOcurrencesPerFile = new LinkedHashMap<>();
		
		weightedFileList = new LinkedHashMap<>();
	}
	
	public Boolean addNewTerm(String word) {
		// Sanity check to avoid adding new terms when there are files processed
		if (!termOcurrencesPerFile.isEmpty()) {
			logger.error("Unable to add more terms as there are files already processed");
			return false;
		}
		
		keywords.put(word,  0.0);
		return true;
	}
	
	public Boolean addNewFile(Path fileName) {
		
		// Sanity check to avoid adding new files with no words defined
		if (keywords.isEmpty()) {
			logger.error("Unable to add files as there are no terms defined");
			return false;
		}
				
		logger.debug("New text file found: " + fileName);
		
		//register new file
		termOcurrencesPerFile.put(fileName, new LinkedHashMap<>());
		
		for (String keyword: keywords.keySet()) {
			
			long occurrencesInFile = calculateTf(fileName, keyword);
			termOcurrencesPerFile.get(fileName).put(keyword, occurrencesInFile);
		}
		
		return true;
	}
	
	public List<String> getTermList() {
		return new ArrayList<String>(keywords.keySet());
	}
	
	public List<Path> getFileList() {
		return new ArrayList<Path>(termOcurrencesPerFile.keySet());
	}
	
	public Map<Path,Double> retrieveResults(Boolean updateNeeded, long resultNumber) {
		if (updateNeeded) {
			updateAllIdfs();
			weightedFileList = calculateFileWeights(resultNumber);
		}
		
		return weightedFileList;
	}
	
	private double roundValue(double inputValue) {
		BigDecimal bigDecimal = new BigDecimal(inputValue).setScale(ROUND_SCALE_DECIMALS, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
	}
	
	private Long retrieveTf(Path fileName, String word) {
		if (!termOcurrencesPerFile.containsKey(fileName)) {
			logger.error("Error: file not registered.");
			return 0L;
		}
		
		if (!termOcurrencesPerFile.get(fileName).containsKey(word)) {
			logger.error("Error: keyword not registered.");
			return 0L;
		}
			
		return termOcurrencesPerFile.get(fileName).get(word);		
	}
	
	private Double retrieveIdf(String word) {
		if (!keywords.containsKey(word)) {
			logger.error("Error: keyword not registered.");
			return 0.0;
		}
		
		return keywords.get(word);
	}	
	
	// Defined as the number of occurrences in the document
	private long calculateTf(Path fileName, String word) {
		
		String fileContents;
		try {
			fileContents = Files.readString(fileName).replace("\r", "").replace("\n", " ");
        } catch (IOException ioEx) {
        	logger.error("  Error reading file: " + ioEx.getMessage());
            return 0;
        }
		
		long wordFrequency = Arrays.stream(fileContents.split(" "))
			      .filter(str -> str.equals(word))
			      .count();
		
		logger.debug("  File " + fileName + " has " + wordFrequency + " ocurrences for " + word);
		
		return wordFrequency;
	}
	
	// Defined as log(number of documents containing the work / the total number of documents)
	private void updateIdf(String word) {
		logger.debug("  Updating idf for " + word);
		
		double numDocsWithWord = termOcurrencesPerFile.entrySet().stream()
			      .filter(docsWithWord -> docsWithWord.getValue().get(word) > 0)
			      .count();
		
		double numDocs = termOcurrencesPerFile.size();
		
		logger.debug("    " + (int)numDocsWithWord + " docs out of " + (int)numDocs + " have the word " + word);
		
		Double idf = 0.0;
		if (numDocsWithWord == 0) {
			logger.warn("    Term " + word + " has no occurrences, so considering idf=0.");
		} else {
			idf = roundValue(Math.log10(numDocs/numDocsWithWord));
		}
		keywords.put(word, idf);
		
		logger.debug("  Term " + word + " has idf " + idf);
	}
	
	private void updateAllIdfs() {
		logger.debug("Updating idf for " + keywords.size() + " words.");
		for (String keyword: keywords.keySet()) {
			updateIdf(keyword);
		}
	}
	
	public Double calculateTfIdf(Path fileName, String word) {
		return roundValue(retrieveTf(fileName, word) * retrieveIdf(word));
	}
	
	private Double calculateTotalTfIdf(Path fileName) {
		Double totalTfIdf = 0.0;
		
		logger.debug("Calculating TfIdf for File " + fileName);
		
		for (String word: keywords.keySet()) {
			Double tempTfIdf = calculateTfIdf(fileName, word);
			logger.trace("  File " + fileName + " has TfIdf " + tempTfIdf + " for word " + word);
			totalTfIdf += tempTfIdf;
		}
		
		totalTfIdf /= keywords.size();
		totalTfIdf = roundValue(totalTfIdf);
		
		logger.debug("File " + fileName + " has total TfIddf " + totalTfIdf);
		
		return totalTfIdf;
	}
	
	private Map<Path, Double> calculateFileWeights(long resultNumber) {
		
		Map<Path, Double> allWeightedFiles = termOcurrencesPerFile.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e->calculateTotalTfIdf(e.getKey())));
		
		logger.debug("All calculated results: " + allWeightedFiles.toString());
		
		return allWeightedFiles.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.limit(resultNumber)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
	} 
}
