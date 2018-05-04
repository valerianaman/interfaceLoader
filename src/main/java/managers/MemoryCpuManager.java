package managers;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

import ch.qos.logback.classic.LoggerContext;

import java.lang.management.RuntimeMXBean;

import org.slf4j.Logger;

public class MemoryCpuManager {
	
	// memory staff
	private Runtime runtime;
	private long maxMemory;
	private long freeMemory;
	private long totalMemory;
	private final long avrgSize = 1048576 * 500; // the average size of a class instantiation
	private long usedMemory;
	// cpu staff
	private OperatingSystemMXBean osbean;
	private  RuntimeMXBean runbean;
	private LoggerContext lc;
	private Logger logger;

	public MemoryCpuManager(Logger lc) {
		// memory
		runtime = Runtime.getRuntime();
		maxMemory = runtime.maxMemory();
		refreshMemory();
		// cpu
		osbean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		runbean = ManagementFactory.getRuntimeMXBean();
		logger=lc;
	}

	private void refreshMemory() {
		//logger.info("[Module begin] MemoryCpuManager.refreshMemory, Parameters: N/A");
		freeMemory = runtime.freeMemory();
		totalMemory = runtime.totalMemory();
		usedMemory = totalMemory - freeMemory;
		//logger.info("[Module end] MemoryCpuManager.refreshMemory");
	}

	public long getMaxMemory() {
		return maxMemory;
	}

	public long getTotalMemory() {
		return totalMemory;
	}

	public long getFreeMemory() {
		refreshMemory();
		return freeMemory;
	}

	public long getUsedMemory() {
		refreshMemory();
		return usedMemory;
	}

	public void gc() {
		System.gc();
	}
	
	public double getCpuUsage () {
//		double processCpuTime = osbean.getProcessCpuLoad();
		return osbean.getSystemCpuLoad();
	}
	
	public boolean canAllocate() {
		//logger.info("[Module begin] MemoryCpuManager.canAllocate, Parameters: N/A");
		refreshMemory();
		//logger.info("[Module end] MemoryCpuManager.canAllocate");
		return ((totalMemory < maxMemory)? (maxMemory-totalMemory+freeMemory > avrgSize) : (freeMemory >= avrgSize));
	}
	
	public boolean canProcess() {
		return osbean.getSystemCpuLoad() <= 0.95;
	}

	public String toString() {
		return "Max memory:\t" + getMaxMemory() / 1073741824 + " GB\t" 
				+ (getMaxMemory() % 1073741824)/1048576 + " MB\t" 
				+ ((getMaxMemory() % 1073741824)%1048576)/1024 + " KB\t" 
				+ ((getMaxMemory() % 1073741824)%1048576)%1024 + " B\n"
				
				+ "Total memory:\t" + getTotalMemory() / 1073741824 + " GB\t" 
				+ (getTotalMemory() % 1073741824)/1048576 + " MB\t" 
				+ ((getTotalMemory() % 1073741824)%1048576)/1024 + " KB\t" 
				+ ((getTotalMemory() % 1073741824)%1048576)%1024 + " B\n"
				
				+ "Free memory:\t" + getFreeMemory() / 1073741824 + " GB\t" 
				+ (getFreeMemory() % 1073741824)/1048576 + " MB\t" 
				+ ((getFreeMemory() % 1073741824)%1048576)/1024 + " KB\t" 
				+ ((getFreeMemory() % 1073741824)%1048576)%1024 + " B\n"
				
				+ "Used memory:\t"  + getUsedMemory() / 1073741824 + " GB\t" 
				+ (getUsedMemory() % 1073741824)/1048576 + " MB\t" 
				+ ((getUsedMemory() % 1073741824)%1048576)/1024 + " KB\t" 
				+ ((getUsedMemory() % 1073741824)%1048576)%1024 + " B\n"
				
				+ "CPU usage:\t" + osbean.getSystemCpuLoad() + " %\n";
	}
}
