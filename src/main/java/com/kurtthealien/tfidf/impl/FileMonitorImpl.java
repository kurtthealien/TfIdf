package com.kurtthealien.tfidf.impl;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

import org.apache.log4j.Logger;

import com.kurtthealien.tfidf.FileMonitor;

public class FileMonitorImpl implements FileMonitor {
	
	private static final Logger logger = Logger.getLogger("TfIdfLog");
	
	private WatchService directoryWatcher;
	private Path directory;
	
	public Boolean configure(Path directory) {
		this.directory = directory;
		
		logger.debug("Coinfiguring directory: " + directory);
		
		try {
			directoryWatcher = FileSystems.getDefault().newWatchService();
		} catch (IOException ioEx) {
            logger.error("Error configuring path watcher: " + ioEx.getMessage());
            return false;
        }
		
		try {
			directory.register(directoryWatcher, ENTRY_CREATE);
		} catch (IOException ioEx) {
            logger.error("Error adding new path to watcher: " + ioEx.getMessage());
            return false;
        }
		
		return true;
	}
	
	public List<Path> retrieveCurrentTextFiles() {
		
		List<Path> fileList = new ArrayList<Path>();

		logger.trace("Checking current directory status...");
		
		try {
			DirectoryStream<Path> initialFileStream = Files.newDirectoryStream(directory);
			StreamSupport.stream(initialFileStream.spliterator(), false)
		    	.sorted(Comparator.comparing(Path::toString))
		    	.forEach(fileName -> { 
		    		if (checkFile(fileName)) {
		    			fileList.add(fileName);
		    		}
		    	});
		} catch (IOException ioEx) {
        	logger.error("Error reading file: " + ioEx.getMessage());
        }
		
		return fileList;
	}

	public List<Path> retrieveNewTextFiles() {
		
		List<Path> fileList = new ArrayList<Path>();
		
		WatchKey key;
    	key = directoryWatcher.poll();
        
        if (key == null) {
        	logger.trace("No new files found");
        	return fileList;
        }
        
        logger.trace("Checking directory...");

        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind<?> kind = event.kind();

            if (kind == OVERFLOW) {
                continue;
            }

            @SuppressWarnings("unchecked")
            WatchEvent<Path> ev = (WatchEvent<Path>) event;
            
            Path parentDir = (Path)key.watchable();
            Path fileName = parentDir.resolve(ev.context());
            
            if (!checkFile(fileName)) {
            	continue;
            }
            
    		fileList.add(fileName);
        }
        
        //Reset the key -- if not valid, the directory is no longer accessible
        if (!key.reset()) {
        	logger.error("Error: path is no longer accessible.");
        }
        
        return fileList;
	}
	
	private Boolean isTextFile(Path fileName) {
		try {
            Path child = directory.resolve(fileName);
            
            //First we lock the file access to avoid early processing of the file
            File lockFile = new File(child.toUri());
            RandomAccessFile raFile = new RandomAccessFile(lockFile, "rw");
            FileChannel channel = raFile.getChannel();
			channel.lock();
			raFile.close();
            
            if (!Files.probeContentType(child).equals("text/plain")) {
                return false;
            }
        } catch (IOException ioEx) {
            logger.error("Error checking new file: " + ioEx.getMessage());
            return false;
        }
		
		return true;
	}
	
	private Boolean checkFile(Path fileName) {
		if (!isTextFile(fileName)) {
        	logger.warn("New file " + fileName + " is not a plain text file.");
            return false;
        }
       
        return true;
	}
}
