package com.kurtthealien.tfidf;

import java.nio.file.Path;
import java.util.List;

public interface FileMonitor {

	public Boolean configure(Path directory);
	
	public List<Path> retrieveCurrentTextFiles();
	
	public List<Path> retrieveNewTextFiles();
}
