package com.kurtthealien.tfidf;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface TfIdf {

	public Boolean addNewTerm(String word);
		
	public Boolean addNewFile(Path fileName);
	
	public List<String> getTermList();
	
	public List<Path> getFileList();
	
	public Map<Path,Double> retrieveResults(Boolean updateNeeded, long resultNumber);
}
