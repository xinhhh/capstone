package uk.ac.cam.cares.jps.agent.file_management.mods;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.cam.cares.jps.agent.file_management.mods.algorithms.AlgorithmS;
import uk.ac.cam.cares.jps.agent.file_management.mods.cases.CaseS;
import uk.ac.cam.cares.jps.agent.file_management.mods.files.FileS;
import uk.ac.cam.cares.jps.agent.file_management.mods.functions.FunctionS;
import uk.ac.cam.cares.jps.agent.file_management.mods.models.ModelS;
import uk.ac.cam.cares.jps.agent.file_management.mods.parameters.ParameterS;

@XmlRootElement(name = "mods")
@XmlType(propOrder = { "algorithmS", "modelS", "caseS", "fileS", "functionS", "parameterS" })
@XmlAccessorType(XmlAccessType.FIELD)
public class MoDS {
	@JsonProperty("xmlns")
	@XmlAttribute(name = "xmlns")
	private String xmlns;

	public String getXmlns() {
		return xmlns;
	}

	public void setXmlns(String xmlns) {
		this.xmlns = xmlns;
	}
	
	@JsonProperty("xmlnsXsi")
	@XmlAttribute(name = "xmlns:xsi")
	private String xmlnsXsi;

	public String getXmlnsXsi() {
		return xmlnsXsi;
	}

	public void setXmlnsXsi(String xmlnsXsi) {
		this.xmlnsXsi = xmlnsXsi;
	}
	
	@JsonProperty("xsiSchemaLocation")
	@XmlAttribute(name = "xsi:schemaLocation")
	private String xsiSchemaLocation;

	public String getXsiSchemaLocation() {
		return xsiSchemaLocation;
	}

	public void setXsiSchemaLocation(String xsiSchemaLocation) {
		this.xsiSchemaLocation = xsiSchemaLocation;
	}
	
	@JsonProperty("algorithms")
	@XmlElement(name = "algorithms")
	private AlgorithmS algorithmS;

	public AlgorithmS getAlgorithmS() {
		return algorithmS;
	}

	public void setAlgorithmS(AlgorithmS algorithmS) {
		this.algorithmS = algorithmS;
	}

	@JsonProperty("models")
	@XmlElement(name = "models")
	private ModelS modelS;

	public ModelS getModelS() {
		return modelS;
	}

	public void setModelS(ModelS modelS) {
		this.modelS = modelS;
	}

	@JsonProperty("cases")
	@XmlElement(name = "cases")
	private CaseS caseS;

	public CaseS getCaseS() {
		return caseS;
	}

	public void setCaseS(CaseS caseS) {
		this.caseS = caseS;
	}

	@JsonProperty("files")
	@XmlElement(name = "files")
	private FileS fileS;

	public FileS getFileS() {
		return fileS;
	}

	public void setFileS(FileS fileS) {
		this.fileS = fileS;
	}
	
	@JsonProperty("functions")
	@XmlElement(name = "functions")
	private FunctionS functionS;
	
	public FunctionS getFunctionS() {
		return functionS;
	}
	
	public void setFunctionS(FunctionS functionS) {
		this.functionS = functionS;
	}

	@JsonProperty("parameters")
	@XmlElement(name = "parameters")
	private ParameterS parameterS;

	public ParameterS getParameterS() {
		return parameterS;
	}

	public void setParameterS(ParameterS parameterS) {
		this.parameterS = parameterS;
	}
}
