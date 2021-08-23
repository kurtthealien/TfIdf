package com.kurtthealien.tfidf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import com.kurtthealien.tfidf.impl.FileMonitorImpl;
import com.kurtthealien.tfidf.impl.TfIdfImpl;

public class TfIdfApp {
	
	private static final Logger logger = Logger.getLogger("TfIdfLog");
	
	private int resultNumber;
	private int reportPeriod;
	
	private TfIdf tfidf;
	private FileMonitor fileMonitor;
	
	public TfIdfApp() {
		
		tfidf = new TfIdfImpl();
		fileMonitor = new FileMonitorImpl();
	}
	
	public Boolean configure(String[] parameters) {
		//parse arguments
		int paramsNumber = parameters.length;
        if ((paramsNumber%2 != 0) || (paramsNumber < 8)) {
        	return false;
        }
        
        Options options = createParserConfiguration();
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		
		try {
			cmd = parser.parse(options, parameters);
		} catch (ParseException e) {
			logger.error(e.getMessage());
			return false;
		}
				
		try {
			resultNumber = Integer.parseInt(cmd.getOptionValue("resultNumber"));
        }
        catch (NumberFormatException nfe) {
            logger.error("Error parsing result number.");
            return false;
        }
		
		try {
			reportPeriod = Integer.parseInt(cmd.getOptionValue("period"));
        }
        catch (NumberFormatException nfe) {
            logger.error("Error parsing period.");
            return false;
        }
		
		configureTerms(cmd.getOptionValue("keywords"));
		
		Path directory = Paths.get(cmd.getOptionValue("directory"));
		if (!Files.isDirectory(directory)) {
			logger.error("Error: directory " + directory + " not found");
            return false;
		}
		
		fileMonitor.configure(directory);
		
		logger.debug("Monitoring \"" + directory + "\" every " + reportPeriod + " seconds to show best " + resultNumber + " results");
		logger.debug("Configuration finished successfully");
        
        return true;
	}
	
	public void launchProcess() {
		Boolean updateResults = checkFiles(true);
		
        for (;;) {
        	
        	reportResults(updateResults);
        	
        	logger.trace("Sleeping " + reportPeriod + " seconds...");
        	
        	try {
            	TimeUnit.SECONDS.sleep(reportPeriod);
            } catch (InterruptedException intEx) {
            	logger.error("Error executing planned delay: " + intEx.getMessage());
                break;
            }
        	
        	updateResults = checkFiles(false);
        }
	}
	
	private Boolean checkFiles(Boolean allOfThem) {
		List<Path> newPaths = allOfThem ? fileMonitor.retrieveCurrentTextFiles() : fileMonitor.retrieveNewTextFiles();
		Boolean newFilesRegistered = false;
		
		for(Path fileName : newPaths) {
			if (!tfidf.addNewFile(fileName)) {
				logger.error("Error registering new file: " + fileName);
				continue;
			}
			newFilesRegistered = true;
		}
		
		return newFilesRegistered;
	}
	
	private void reportResults(Boolean updateNeeded) {
		
		Map<Path, Double> results = tfidf.retrieveResults(updateNeeded, resultNumber);
		int numFiles = tfidf.getFileList().size();

		if (results.size() > 0) {
			logger.info("Reporting " + results.size() + " filtered results from " + numFiles + " files:");
			for(Map.Entry<Path,Double> fileResult : results.entrySet()) {
				logger.info("  " + fileResult.getKey() + " " + fileResult.getValue());
			}
		}
	}
	
	private Options createParserConfiguration() {
		Options options = new Options();
    	
		Option directoryParam = new Option("d", "directory", true, "Directory");
		directoryParam.setRequired(true);
		options.addOption(directoryParam);
		
		Option resultNumberParam = new Option("n", "resultNumber", true, "Results to show");
		resultNumberParam.setRequired(true);
		options.addOption(resultNumberParam);
		
		Option periodParam = new Option("p", "period", true, "Reporting period");
		periodParam.setRequired(true);
		options.addOption(periodParam);
		
		Option termsParam = new Option("t", "keywords", true, "Set of terms");
		termsParam.setRequired(true);
		options.addOption(termsParam);
		
		return options;
	}
	
	private void configureTerms(String terms) {
		logger.trace("Processing terms: " + terms);
		StringTokenizer termTokenizer = new StringTokenizer(terms);
		logger.trace("  Number of terms found : " + termTokenizer.countTokens());
		while (termTokenizer.hasMoreTokens())
		{
			String newToken = termTokenizer.nextToken();
			logger.trace("  Adding new term: " + newToken);
			tfidf.addNewTerm(newToken);
		}
	}

	private static void showUsage() {
        logger.error("Usage: TfIdf -d [directory] -n [results to show] -p [reporting period] -t [set of terms]");
        logger.error("Example: TfIdf -d dir -n 5 -p 300 -t \"password try again\"");
        System.exit(-1);
    }
	
	public static void main(String[] args) {

		logger.debug("Starting tfidf app...");
		
		TfIdfApp tfIdfApp = new TfIdfApp();
		
		if (!tfIdfApp.configure(args)) {
			showUsage();
		}
		
		tfIdfApp.launchProcess();
		
		logger.debug("Finishing tfidf...");
	}
}
