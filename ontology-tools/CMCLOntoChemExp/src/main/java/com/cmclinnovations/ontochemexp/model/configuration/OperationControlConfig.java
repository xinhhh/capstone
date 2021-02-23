package com.cmclinnovations.ontochemexp.model.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Reads values of all operation control properties provided
 * in the operation.control.properties file.</br>
 * 
 * @author msff2
 *
 */
@Configuration
@PropertySource("classpath:operation.control.properties")
public class OperationControlConfig {
	@Value("${read.prime}")
	private String opReadPrime;
	
	@Value("${write.prime}")
	private String opWritePrime;
	
	@Value("${read.owl}")
	private String opReadOwl;
	
	@Value("${write.owl}")
	private String opWriteOwl;

	@Value("${prime.file.extension}")
	private String primeFileExtension;

	@Value("${owl.file.extension}")
	private String owlFileExtension;

	@Value("${data.structure.saving.xml.file.name}")
	private String dataStructureSavingFileName;

	@Value("${conversion.completeness.report.name}")
	private String conversionReportName;
	
	public String getOpReadPrime() {
		return opReadPrime;
	}

	public void setOpReadPrime(String opReadPrime) {
		this.opReadPrime = opReadPrime;
	}
	
	/**
	 * @return a value to indicate that it's a PrIMe write operation.
	 * @see String
	 */
	public String getOpWritePrime() {
		return opWritePrime;
	}
	
	/**
	 * @param opWriteCtml a PrIMe write operation to set.
	 */
	public void setOpWritePrime(String opWritePrime) {
		this.opWritePrime = opWritePrime;
	}
	
	/**
	 * 
	 * @return "3" to indicate that it's an OWL read operation.
	 * @see String
	 */
	public String getOpReadOwl() {
		return opReadOwl;
	}
	
	/**
	 * @param opReadOwl an OWL read operation to set.
	 */
	public void setOpReadOwl(String opReadOwl) {
		this.opReadOwl = opReadOwl;
	}

	/**
	 * 
	 * @return "2" to indicate that it's an OWL write operation.
	 * @see String
	 */
	public String getOpWriteOwl() {
		return opWriteOwl;
	}

	/**
	 * @param opWriteOwl an OWL write operation to set.
	 */
	public void setOpWriteOwl(String opWriteOwl) {
		this.opWriteOwl = opWriteOwl;
	}

	/**
	 * @return the name of the OWL file extension.
	 * @see String
	 */
	public String getOwlFileExtension() {
		return owlFileExtension;
	}

	/**
	 * @param owlFileExtension the name of the OWL file extension to set.
	 */
	public void setOwlFileExtension(String owlFileExtension) {
		this.owlFileExtension = owlFileExtension;
	}

	public String getPrimeFileExtension() {
		return primeFileExtension;
	}

	public void setPrimeFileExtension(String primeFileExtension) {
		this.primeFileExtension = primeFileExtension;
	}

	public String getDataStructureSavingFileName() {
		return dataStructureSavingFileName;
	}

	public void setDataStructureSavingFileName(String dataStructureSavingFileName) {
		this.dataStructureSavingFileName = dataStructureSavingFileName;
	}

	public String getConversionReportName() {
		return conversionReportName;
	}

	public void setConversionReportName(String conversionReportName) {
		this.conversionReportName = conversionReportName;
	}
}
