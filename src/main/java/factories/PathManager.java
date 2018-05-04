package factories;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import ch.qos.logback.classic.LoggerContext;

public class PathManager {
	
	private List<File> listOfFiles;
	private LoggerContext lc;
	private Logger logger;
	
	public PathManager(String path, Logger lc) {
		File folder = new File(path);
		if (folder.isDirectory())
        listOfFiles = new ArrayList<>(Arrays.asList(folder.listFiles()));
		logger=lc;
	}

	/**
	 *
	 * @return copy of an arraylist of files as String in dir
	 */
	public List<File> getAllFiles() {
		return new ArrayList<>(listOfFiles);
	}
	
	public List<String> getRoutes(String extension) {
		logger.info("[Module begin] PathManager.getRoutes, Parameters: extension(String)");
		List<String> paths = new ArrayList<>();
		for (File f: listOfFiles) {
			logger.warn(f.getName());
			if (FilenameUtils.getExtension(f.getAbsolutePath()).equals(extension)) {
				logger.warn(f.getAbsolutePath().replaceAll("\\\\", "/"));
				paths.add(f.getAbsolutePath().replaceAll("\\\\", "/"));
			}
		}
		logger.info("[Module end] PathManager.getRoutes");
		return paths;
	}
	
	public void close() {
		listOfFiles.clear();
		listOfFiles = null;
	}
	
	@Override
	public String toString () {
		logger.info("[Module begin] PathManager.toString, Parameters: N/A");
		String result = "";
		for(File f: listOfFiles) {
			result += f.getAbsolutePath() + "\n";
		}
		logger.info("[Module end] PathManager.toString");
		return result;
	}
}
