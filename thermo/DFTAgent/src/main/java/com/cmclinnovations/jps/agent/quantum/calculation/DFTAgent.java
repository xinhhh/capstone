package com.cmclinnovations.jps.agent.quantum.calculation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;

import com.cmclinnovations.jps.agent.job.request.parser.JSonRequestParser;
import com.cmclinnovations.jps.agent.workspace.management.Workspace;
import com.cmclinnovations.jps.kg.OntoSpeciesKG;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.StreamGobbler;
import ch.ethz.ssh2.Session;

/**
 * Quantum Calculation Agent developed for setting-up and running quantum
 * jobs at increasing levels of theory.   
 * 
 * @author msff2
 *
 */
@Controller
public class DFTAgent extends HttpServlet{
	private Logger logger = LoggerFactory.getLogger(DFTAgent.class);	
	String server = "login-skylake.hpc.cam.ac.uk";
	String username = "msff2";
	String password = "Abcdl955_l7_l7_l7_aB";
	String command = "ls";
	Connection connection;
	Session session;
	boolean isAuthenticated;
	static HashSet<String> jobPool = new HashSet<>();
	
	private static File taskSpace; 
	
	public static void main(String[] args) throws ServletException, DFTAgentException{
		DFTAgent dftAgent = new DFTAgent();
		dftAgent.init();
	}
	
	/**
	 * Initialises property values.
	 * 
	 * @throws DFTAgentException
	 */
	public void init() throws ServletException{
        logger.info("---------- Quantum Calculation Agent has started ----------");
        System.out.println("---------- Quantum Calculation Agent has started ----------");
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        DFTAgent dftAgent = new DFTAgent();
       	executorService.scheduleAtFixedRate(dftAgent::monitorTasks, 1, 20, TimeUnit.SECONDS);
        logger.info("---------- Qunatum jobs are being monitored  ----------");
        System.out.println("---------- Qunatum jobs are being monitored  ----------");
       	
	}

	private void monitorTasks(){
        Workspace workspace = new Workspace();
        if(taskSpace == null){
        	taskSpace = workspace.getWorkspaceName(Property.AGENT_WORKSPACE_DIR.getPropertyName(), Property.AGENT_CLASS.getPropertyName());
        }
        try {
        	Map<String, List<String>> jobsUnfinished = Utils.getUnfinishedJobs(taskSpace);
			System.out.println("Number of unfinished jobs:"+jobsUnfinished.size());
			int countJob = 0;
			for(String job:jobsUnfinished.keySet()){
				System.out.println("Job ["+ ++countJob+"]: "+job);
			}
			checkRunningJobs(jobsUnfinished);
//        	runNotStartedJobs(jobs);
		} catch (IOException e) {
			e.printStackTrace();
		} 
//		List<String> jobList = new ArrayList<>();
//        try{
//        for(String jsonString:jsonRequests){
//        	setUpQuantumJob(jsonString);
//			getUnfinishedJobs(taskSpace);
////        	runQuantumJob();
//        }
//        }catch(IOException e){
//        	logger.error(e.getMessage());
//        	e.printStackTrace();
//        }catch(DFTAgentException e){
//        	logger.error(e.getMessage());
//        	e.printStackTrace();
//        }
 catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void checkRunningJobs(Map<String, List<String>> jobs) throws IOException, InterruptedException{
		Map<String, List<String>> runningJobs = Utils.getRunningJobs(jobs);
		for(String runningJob:runningJobs.keySet()){
			File statusFile = Utils.getStatusFile(runningJobs.get(runningJob));
			if(statusFile!=null){
				checkRunningJob(statusFile);
			}
		}
	}
	
	private void checkRunningJob(File statusFile) throws IOException, InterruptedException{
		String jobId = Utils.getJobId(statusFile.getAbsolutePath());
		if(jobId!=null){
			boolean isJobRunning = isJobRunning(jobId);
			if(!isJobRunning){
				Utils.modifyStatus(statusFile.getAbsolutePath(), Jobs.STATUS_JOB_COMPLETED.getName());						
			}
		}
	}
	
	private void startScheduledTasks(){
        List<String> jobList = new ArrayList<>();
        List<String> jsonRequests = new ArrayList<>();
        // Temporary block the assertion of the list of 5 species IRIs starts here.
        jsonRequests.add("{\"job\":{\"levelOfTheory\":\"B3LYP/6-31G(d)\",\"keyword\":\"Opt\",\"algorithmChoice\": \"Freq\"},\"speciesIRI\":\"http://www.theworldavatar.com/kb/ontospecies/00b7e248-ae24-35bf-b7a0-b470b923ddf6.owl#00b7e248-ae24-35bf-b7a0-b470b923ddf6\"}");
        jsonRequests.add("{\"job\":{\"levelOfTheory\":\"B3LYP/6-31G(d)\",\"keyword\":\"Opt\",\"algorithmChoice\": \"Freq\"},\"speciesIRI\":\"http://www.theworldavatar.com/kb/ontospecies/00b537ef-8b6f-3246-9a7e-edd0259c6e09.owl#00b537ef-8b6f-3246-9a7e-edd0259c6e09\"}");
        jsonRequests.add("{\"job\":{\"levelOfTheory\":\"B3LYP/6-31G(d)\",\"keyword\":\"Opt\",\"algorithmChoice\": \"Freq\"},\"speciesIRI\":\"http://www.theworldavatar.com/kb/ontospecies/00c4803e-ba0b-3b8a-b8b1-8cd00bb6172d.owl#00c4803e-ba0b-3b8a-b8b1-8cd00bb6172d\"}");
        jsonRequests.add("{\"job\":{\"levelOfTheory\":\"B3LYP/6-31G(d)\",\"keyword\":\"Opt\",\"algorithmChoice\": \"Freq\"},\"speciesIRI\":\"http://www.theworldavatar.com/kb/ontospecies/00f46355-2ea4-3ef1-b61a-e3d87e91a8db.owl#00f46355-2ea4-3ef1-b61a-e3d87e91a8db\"}");
        jsonRequests.add("{\"job\":{\"levelOfTheory\":\"B3LYP/6-31G(d)\",\"keyword\":\"Opt\",\"algorithmChoice\": \"Freq\"},\"speciesIRI\":\"http://www.theworldavatar.com/kb/ontospecies/0a1a4723-19ad-3272-b334-615587274e3c.owl#0a1a4723-19ad-3272-b334-615587274e3c\"}");
        // Temporary block the assertion of the list of 5 species IRIs ends here.        
        try{
        for(String jsonString:jsonRequests){
        	File workspace = setUpJobOnAgentMachine(jsonString);
//        	transferJobToHPC(workspace);
//			getUnfinishedJobs();
//        	runQuantumJob();
        }
        }catch(IOException e){
        	logger.error(e.getMessage());
        	e.printStackTrace();
        }catch(DFTAgentException e){
        	logger.error(e.getMessage());
        	e.printStackTrace();
        }
	}
	
	private File setUpJobOnAgentMachine(String jsonString) throws IOException, DFTAgentException{
		Workspace workspace = new Workspace();
		File workspaceFolder = workspace.createAgentWorkspace(Property.AGENT_WORKSPACE_DIR.getPropertyName(), Property.AGENT_CLASS.getPropertyName());
		if(workspaceFolder == null){
			logger.info("Workspace could not be created.");
		}else{
			File jobFolder = workspace.createJobFolder(workspaceFolder.getAbsolutePath());
			if(jobFolder == null){
				logger.info("Job folder could not be created.");
			}else{
				setUpQuantumJob(workspace, workspaceFolder, jobFolder, jsonString);
			}
		}
		return workspaceFolder;
	}
	
	private void setUpQuantumJob(Workspace ws, File workspaceFolder, File jobFolder, String jsonString) throws IOException, DFTAgentException{
		OntoSpeciesKG oskg = new OntoSpeciesKG(); 
    	String speciesIRI = JSonRequestParser.getSpeciesIRI(jsonString);
		String speciesGeometry = oskg.querySpeciesGeometry(speciesIRI);
		System.out.println("SpeciesGeometry:"+speciesGeometry);
		ws.createInputFile(ws.getInputFilePath(jobFolder), jobFolder.getName(), speciesGeometry, jsonString);
		ws.createStatusFile(workspaceFolder, ws.getStatusFilePath(jobFolder));
		ws.copyScriptFile(Property.SLURM_SCRIPT_FILE_PATH.getPropertyName(), jobFolder.getAbsolutePath());
	}
	
	/**
	 * Runs a set of quantum jobs.
	 * 
	 * @return
	 */
	private String runQuantumJob(){
		try {
			setupJob(); // In the next iteration of development, we will include code for setting up jobs.   
			String slurmScriptName = "G09Slurm_darwin_S1.sh";
			String inputFileName = "water_ex.com";
//			uploadFile("C:/Users/msff2/Documents/HPC/KnowledgeCapturedFromAngiras/".concat(inputFileName),
//					"rds/hpc-work/gaussian");
//			uploadFile("C:/Users/msff2/Documents/HPC/KnowledgeCapturedFromAngiras/".concat(slurmScriptName),
//					"rds/hpc-work/gaussian");
			uploadFile("C:/Users/msff2/Documents/HPC/KnowledgeCapturedFromAngiras/".concat(inputFileName),
			"/home/".concat(username));
			uploadFile("C:/Users/msff2/Documents/HPC/KnowledgeCapturedFromAngiras/".concat(slurmScriptName),
			"/home/".concat(username));
			
			runQuantumJob(slurmScriptName, inputFileName);
		} catch (JSchException e) {
			logger.error(e.getMessage());
		} catch (SftpException e) {
			logger.error(e.getMessage());
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	/**
	 * Decides the time interval between the current and next</br>
	 * status check operations of a quantum job.   
	 * 
	 * @param count
	 * @throws InterruptedException
	 */
	private void waitBeforeStatusCheck() throws InterruptedException{
		Thread.sleep(2000);
	}
	
	/**
	 * Checks if a job is still running using the job id.
	 * 
	 * @param jobId
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private boolean isJobRunning(String jobId) throws IOException, InterruptedException{
		String command = "squeue -j " + jobId + "--start";
		ArrayList<String> outputs = executeCommand(command);
		boolean jobRunning = isJobRunning(outputs);
		return jobRunning;
	}
	
	/**
	 * Analyses the outputs following the execution of the</br>
	 * job status check command to understand if the job</br>
	 * is still running or terminated.
	 * 
	 * @param outputs
	 * @return
	 */
	private boolean isJobRunning(ArrayList<String> outputs){
		if(outputs!=null && outputs.size()==1){
			return false;
		}
		return true;
	}
	
	/**
	 * Extracts the job id from the 
	 * 
	 * @param outputs
	 * @return
	 */
	private String getJobId(ArrayList<String> outputs){
		for(String output: outputs){
			if(output.contains("Submitted batch job")){
				String tokens[] = output.split(" ");
				if(tokens.length>=4){
					return tokens[3].trim();
				}
			}
		}
		return null;
	}
	
	private void setupJob(){
		
	}
	
	/**
	 * Runs a quantum job and copies the output file (log file) from CSD3
	 * to the machine where DFT Agent is hosted.
	 * 
	 * @param command
	 * @throws IOException
	 */
	private String runQuantumJob(String scriptName, String inputFileName) throws IOException, InterruptedException{
//		String command = "cd rds/hpc-work/gaussian && sbatch ".concat(scriptName);
		String command = "cd /home/".concat(username).concat(" && sbatch ").concat(scriptName);

		ArrayList<String> outputs = executeCommand(command);
		if (outputs == null) {
			return null;
		}
		String jobId = getJobId(outputs);
		System.out.println("Job id:" + jobId);
		jobPool.add(jobId);
		boolean isJobRunning = isJobRunning(jobId);
		int count = 0;
		System.out.println("Is the job running? " + isJobRunning);
		while (isJobRunning) {
			count++;
			waitBeforeStatusCheck();
			isJobRunning = isJobRunning(jobId);
		}
		try {
//			downloadFile("rds/hpc-work/gaussian/".concat(inputFileName.replace(".com", ".log")),
//					"C:/Users/msff2/Documents/HPC/KnowledgeCapturedFromAngiras");
			downloadFile("/home/msff2/".concat(inputFileName.replace(".com", ".log")),
			"C:/Users/msff2/Documents/HPC/KnowledgeCapturedFromAngiras");
		} catch (JSchException e) {
			logger.error(e.getMessage());
		} catch (SftpException e) {
			logger.error(e.getMessage());
		}
		return jobId;
	}
	
	/**
	 * Connects the client to the server via server IP address or name. 
	 * 
	 * @param server IP address or name
	 * @throws IOException
	 */
	public void connect(String server) throws IOException{
        if(connection == null){
        	connection = new Connection(server);
            connection.connect();
    		authenticate(username, getPassword(password));
    		openSession();
        }
	}
	
	/**
	 * Authenticate the user with the password.
	 * 
	 * @param username the name of user
	 * @param password the password of user
	 * @throws IOException
	 */
	public void authenticate(String username, String password) throws IOException{
        if(connection == null)
        	throw new IOException("Not connected to the server.");
        if (!connection.isAuthenticationComplete())
        	isAuthenticated = connection.authenticateWithPassword(username, password);
        if (isAuthenticated == false)
            throw new IOException("Authentication failed.");        		
	}
	
	/**
	 * Open a session in order to allow to execute commands.
	 * 
	 * @throws IOException
	 */
	public void openSession() throws IOException{
		if(connection == null)
        	throw new IOException("Not connected to the server.");
		if (!connection.isAuthenticationComplete())
			throw new IOException("Authentication is not complete.");
		session = connection.openSession();
	}
	
	/**
	 * Executes a command within a session.
	 * 
	 * @param command
	 * @throws IOException
	 */
	public void executeCommand1(String command) throws IOException{
		if(session == null)
			throw new IOException("No session is open.");
		session.execCommand(command);
	}
	
	/**
	 * Create the reader to read the outputs of the most recently executed</br>
	 * command.
	 * 
	 * @return
	 * @throws IOException
	 */
	public BufferedReader getReader() throws IOException{
        if(session == null)
        	throw new IOException("No session is open.");
        return new BufferedReader(new InputStreamReader(new StreamGobbler(session.getStdout())));
	}
	
	/**
	 * Read the output of the most recently executed command.
	 * 
	 * @param br
	 * @return
	 * @throws IOException
	 */
	public ArrayList<String> readCommandOutput(BufferedReader br) throws IOException{
		if(br == null)
			throw new IOException("The reader is not initialised.");
		String line;
		ArrayList<String> outputs = new ArrayList<>();
		while((line=br.readLine())!=null){
			outputs.add(line);
		}
		return outputs;
	}
	
	/**
	 * Close the currently open session and connection. 
	 * 
	 * @throws IOException
	 */
	public void closeSessionAndConnection() throws IOException{
        if(session == null)
        	throw new IOException("No session is open.");
		session.close();
        if(connection == null)
        	throw new IOException("No connection is open.");
		connection.close();
		System.out.println("Connection closed test:"+(session==null)+" session is null:"+session.getState());
	}
	
	/**
	 * Display every item in a list of string array.
	 * 
	 * @param list
	 */
	public void displayArray(ArrayList<String> list){
		for(String item:list){
			System.out.println(item);
		}
	}
	
	public void uploadFile(String src, String dest) throws JSchException, SftpException{
		JSch jsch = new JSch();
		com.jcraft.jsch.Session session = jsch.getSession(username, server);
		String pwd = getPassword(password);
		session.setPassword(pwd);
        session.setConfig("StrictHostKeyChecking", "no");
        System.out.println("Establishing Connection to transfer "+src+" to "+dest);
        session.connect();
		ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
		sftpChannel.connect();

		sftpChannel.put(src, dest);
		sftpChannel.disconnect();
		System.out.println("Closing the connection.");
	}
	
	public void downloadFile(String src, String dest) throws JSchException, SftpException{
		JSch jsch = new JSch();
		com.jcraft.jsch.Session session = jsch.getSession(username, server);
		String pwd = getPassword(password);
		session.setPassword(pwd);
        session.setConfig("StrictHostKeyChecking", "no");
        System.out.println("Establishing Connection to transfer "+src+" to "+dest);
        session.connect();
		ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
		sftpChannel.connect();

		sftpChannel.get(src, dest);
		sftpChannel.disconnect();
		System.out.println("Closing the connection.");
	}

	/**
	 * Delete a folder or file name from an HPC, if the complete path is provided.</br>
	 * For example, to delete a folder called "test", user needs to provide "rds/.../test"
	 * 
	 * @param folderOrFileName 
	 * @throws JSchException
	 * @throws SftpException
	 */
	public void deleteFolderOrFile(String folderOrFileName) throws JSchException, SftpException{
		executeCommand("rm -r "+folderOrFileName);
	}

	/**
	 * Create a folder on an HPC, if the complete path is provided.</br>
	 * For example, to create a folder called "test", user needs to provide "rds/.../test"
	 * 
	 * @param folder 
	 * @throws JSchException
	 * @throws SftpException
	 */
	public void createFolder(String folder) throws JSchException, SftpException{
		executeCommand("mkdir "+folder);
	}
	
	public ArrayList<String> executeCommand(String Command){
		ArrayList<String> outputs = null;
		try {
			JSch jsch = new JSch();
			com.jcraft.jsch.Session session = jsch.getSession(username, server);
			String pwd = getPassword(password);
			session.setPassword(pwd);
			session.setConfig("StrictHostKeyChecking", "no");
			System.out.println("Establishing a connection to perform the following command:" + Command);
			session.connect();
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(Command);
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(channel.getInputStream()));
			channel.connect();
			outputs = readCommandOutput(stdInput);
			channel.disconnect();
			session.disconnect();
			System.out.println("DONE");
		} catch (JSchException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputs;
	}

	public void SSHClient(String serverIp,String command, String username,String password) throws IOException{
        System.out.println("inside the ssh function");
        try
        {
            Connection conn = new Connection(serverIp);
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
            if (isAuthenticated == false)
                throw new IOException("Authentication failed.");        
            ch.ethz.ssh2.Session sess = conn.openSession();
            sess.execCommand(command);  
            InputStream stdout = new StreamGobbler(sess.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            System.out.println("the output of the command is");
            while (true)
            {
                String line = br.readLine();
                if (line == null)
                    break;
                System.out.println(line);
            }
            System.out.println("ExitCode: " + sess.getExitStatus());
            sess.close();
            conn.close();
        }
        catch (IOException e)
        {
            e.printStackTrace(System.err);

        }
    }
	
	private String getPassword(String password){
		return password.replace("l", "1").replace("_", "").replace("7", "3").replace("3", "4");
	}
}
