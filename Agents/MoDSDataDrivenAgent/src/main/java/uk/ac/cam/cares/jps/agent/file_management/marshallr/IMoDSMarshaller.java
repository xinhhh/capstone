package uk.ac.cam.cares.jps.agent.file_management.marshallr;

import java.io.IOException;
import java.util.List;

import uk.ac.cam.cares.jps.agent.mechanism.datadriven.MoDSDataDrivenAgentException;


public interface IMoDSMarshaller {
	public void initialise(String jobFolderName) throws IOException, MoDSDataDrivenAgentException;
	
	public void plugInKinetics(List<String> experimentIRI, String mechanismIRI, List<String> reactionIRIList, String otherOptions) throws IOException, MoDSDataDrivenAgentException;
	
	public void plugInCantera(List<String> experimentIRI, String mechanismIRI, List<String> reactionIRIList, String otherOptions) throws IOException, MoDSDataDrivenAgentException;
	
	public String marshall() throws IOException, MoDSDataDrivenAgentException;

	public void plugInModelDataDriven(List<String> dataVar, String mechanismIRI, List<String> reactionIRIList, String otherOptions) throws IOException, MoDSDataDrivenAgentException;

	/**
	 * Set up all the components of executable in the MoDS input file. 
	 * 
	 * @throws IOException
	 * @throws MoDSDataDrivenAgentException
	 */
	void setUpMoDS() throws IOException, MoDSDataDrivenAgentException;
}
