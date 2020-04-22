package com.cmclinnovations.jps.agent.quantum.calculation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import java.util.List;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmclinnovations.jps.agent.json.parser.JSonRequestParser;
import com.cmclinnovations.slurm.job.Status;

public class Utils {
	private static Logger logger = LoggerFactory.getLogger(Utils.class);	
	public static long previousTimeStamp;
	
	/**
	 * Generate unique time stamp to be used in naming quantum jobs and</br>
	 * the instance of EBR Agent.
	 * 
	 * @return
	 */
	public static long getTimeStamp(){
		long currentTimeStamp = System.nanoTime();
		while(!(currentTimeStamp > previousTimeStamp)){
			currentTimeStamp = System.nanoTime();
		}
		previousTimeStamp = currentTimeStamp;
		return currentTimeStamp;
	}
	
	/**
	 * Creates an instance of the BufferedWriter class.
	 * 
	 * @param filePathPlusName the path plus name of the file being written
	 * @return
	 * @throws IOException
	 */
	public static BufferedWriter openBufferedWriter(String filePathPlusName) throws IOException{
		return new BufferedWriter(new FileWriter(filePathPlusName));
	}
	
	/**
	 * Creates and returns an instance of the BufferedReader class.
	 * 
	 * @param filePathPlusName
	 *            the path plus name of the file being read
	 * @return
	 * @throws IOException
	 */
	public static BufferedReader openSourceFile(String filePathPlusName)
			throws IOException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(
				filePathPlusName), "UTF-8"));
	}

	public static boolean isJobCompleted(File jobFolder) throws IOException{
		return isJobFinished(jobFolder.getAbsolutePath().concat(File.separator).concat(Status.STATUS_FILE.getName()));
	}
	
	public static boolean isJobOutputProcessed(File jobFolder) throws IOException{
		return isJobOutputProcessed(jobFolder.getAbsolutePath().concat(File.separator).concat(Status.STATUS_FILE.getName()));
	}

	/**
	 * Check the status if a job finished.
	 * 
	 * @param statusFilePath
	 * @return
	 * @throws IOException
	 */
	public static boolean isJobFinished(String statusFilePath) throws IOException{
		BufferedReader statusFile = Utils.openSourceFile(statusFilePath);
		String line;
		while((line=statusFile.readLine())!=null){
			if(line.trim().startsWith(Status.ATTRIBUTE_JOB_STATUS.getName())){
				if(line.contains(Status.STATUS_JOB_COMPLETED.getName())){
					statusFile.close();
					return true;
				}
				if(line.contains(Status.STATUS_JOB_ERROR_TERMINATED.getName())){
					statusFile.close();
					return true;
				}
			}
		}
		statusFile.close();
		return false;
	}
	
	/**
	 * Check the status if a job is currently running.
	 * 
	 * @param statusFilePath the absolute path to the status file.
	 * @return
	 * @throws IOException
	 */
	public static boolean isJobOutputProcessed(String statusFilePath) throws IOException{
		BufferedReader statusFile = Utils.openSourceFile(statusFilePath);
		String line;
		while((line=statusFile.readLine())!=null){
			if(line.trim().startsWith(Status.ATTRIBUTE_JOB_OUTPUT.getName())){
				if(line.contains(Status.OUTPUT_PROCESSED.getName())){
					statusFile.close();
					return true;
				}
			}
		}
		statusFile.close();
		return false;
	}
		
	/**
	 * Modifies the status of job in the status file. 
	 * 
	 * @param filePath the path to the status file.
	 * @param status can be "running" or "completed".
	 * @throws IOException
	 */
	public static void modifyStatus(String filePath, String status) throws IOException{
		List<String> fileContent = new ArrayList<>();
		BufferedReader br = openSourceFile(filePath);
		String line;
		while((line=br.readLine())!=null){
		    if (line.trim().startsWith(Status.ATTRIBUTE_JOB_STATUS.getName())) {
		        line = Status.ATTRIBUTE_JOB_STATUS.getName().concat(" ").concat(status);
		    }
		    fileContent.add(line);
		}
		br.close();
		BufferedWriter bw = openBufferedWriter(filePath);
		for(String lineContent:fileContent){
			bw.write(lineContent.concat("\n"));
		}
		bw.flush();
		bw.close();
	}
	
	/**
	 * Modifies the output to processed in the status file. 
	 * 
	 * @param filePath the path to the status file.
	 * @param status can be empty ("") or "processed".
	 * @throws IOException
	 */
	public static void modifyOutputStatus(String filePath, String status) throws IOException{
		List<String> fileContent = new ArrayList<>();
		BufferedReader br = openSourceFile(filePath);
		String line;
		while((line=br.readLine())!=null){
		    if (line.trim().startsWith(Status.ATTRIBUTE_JOB_OUTPUT.getName())) {
		        line = Status.ATTRIBUTE_JOB_OUTPUT.getName().concat(" ").concat(status);
		    }
		    fileContent.add(line);
		}
		br.close();
		BufferedWriter bw = openBufferedWriter(filePath);
		for(String lineContent:fileContent){
			bw.write(lineContent.concat("\n"));
		}
		bw.flush();
		bw.close();
	}
	
	/**
	 * Finds the status file from a list of files related to a job.
	 *  
	 * @param filePaths paths to all files in a job folder.
	 * @return
	 */
	public static File getStatusFile(List<String> filePaths){
		for(String filePath:filePaths){
			if(filePath.toLowerCase().endsWith(Status.STATUS_FILE.getName().toLowerCase())){
				return new File(filePath);
			}
		}
		return null;
	}

	/**
	 * Returns the status file if the job folder is provided. 
	 *  
	 * @param jobFolder path to the job folder.
	 * @return
	 */
	public static File getStatusFile(File jobFolder){
		if(jobFolder.isDirectory()){
			if((new File(jobFolder.getAbsolutePath().concat(File.separator).concat(Status.STATUS_FILE.getName())).isFile())){
				return new File(jobFolder.getAbsolutePath().concat(File.separator).concat(Status.STATUS_FILE.getName()));
			}
		}
		return null;
	}
/**
 * 	
 * @param folderName the folder name. 
 * @return zip folder and content inside that folder.
 * @throws IOException
 */
public static File getZipFile(String folderName) throws IOException {
		
		
		Path sourceDirectory = Paths.get(folderName);		
		
		String zipFileName = folderName.concat(".zip");

		try {
			
		ZipOutputStream zipOutputStream = new ZipOutputStream (new FileOutputStream(zipFileName));
		
		Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path arg0, BasicFileAttributes arg1) throws IOException {
				
				try {
					
				Path destinationFile = sourceDirectory.relativize(arg0);
				
				zipOutputStream.putNextEntry(new ZipEntry(destinationFile.toString()));
				
				byte[] bytes = Files.readAllBytes(arg0);
				
				zipOutputStream.write(bytes, 0, bytes.length);
				
				zipOutputStream.closeEntry();
				
				}catch(IOException e) {
					
					e.printStackTrace();
				}
				
				// TODO Auto-generated method stub
				//return super.visitFile(arg0, arg1);
				
				return FileVisitResult.CONTINUE;
				
			}
		});
		
		zipOutputStream.flush();
		
		zipOutputStream.close();
		
		}catch(IOException e) {
			
			e.printStackTrace();
		}
		
		File zipFile = new File(zipFileName);
		
		return zipFile;
	
	}

/**
 * 
 * @author NK510 (caresssd@hermes.cam.ac.uk)
 * 
 * @param fileUrl the gaussina file url
 * @param destinationFilePath the destination file path where Gausian file will be stored. It is given in JSON input file.
 * @throws MalformedURLException
 * @throws IOException
 */
public static void copyFileFromURL(String fileUrl, String destinationFilePath) throws MalformedURLException, IOException {
	
	InputStream in = new URL(fileUrl).openStream();
	
	Files.copy(in, Paths.get(destinationFilePath),StandardCopyOption.REPLACE_EXISTING);
}

/**
 * It creates JSON input folder under user home.  If it finds it then it deletes it and after that it creates it.
 * 
 * @author Dr Feroz Farazi msff2@cam.ac.uk
 * @author NK510 (caresssd@hermes.cam.ac.uk)
 *   
 * @param jsonInput the JSON input string. 
 * @throws IOException
 */
public static void createInputFolder(String jsonInput) throws IOException {

	String inputFolderPath =JSonRequestParser.getGaussianFolderPath(jsonInput);
	
	String[] tokens = inputFolderPath.split("/");
	
	System.out.println(tokens[0]);
	
	if(new File(SystemUtils.getUserHome()+"/"+tokens[0]).exists()) {
		
		System.out.println(SystemUtils.getUserHome()+File.separator+tokens[0]);
		
		FileUtils.deleteDirectory(new File(SystemUtils.getUserHome()+File.separator+tokens[0]));
	}
	
	new File(SystemUtils.getUserHome()+File.separator+tokens[0]).mkdir();
	new File(SystemUtils.getUserHome()+File.separator+inputFolderPath).mkdir();
	

}

}
