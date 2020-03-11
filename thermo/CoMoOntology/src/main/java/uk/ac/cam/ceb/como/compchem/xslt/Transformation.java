package uk.ac.cam.ceb.como.compchem.xslt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import uk.ac.cam.ceb.como.jaxb.parsing.utils.FileUtility;
import uk.ac.cam.ceb.como.jaxb.parsing.utils.Utility;

/**
 * 
 * The Class Transformation.
 *
 * @author <p>nk510 This class implements methods for xslt transformations from
 *         compchem XML files to RDF graph as Abox assertions of CoMo compchem
 *         ontology ver 0.1.</p>
 *
 */ 

public class Transformation {

	/**
	 * The folder path.
	 *
	 * @author nk510 <p>Folder path where generated Compchem XML files are saved.</p>
	 * 
	 */

	static String xmlFolderPath = "src/test/resources/compchem_xml/test/";

	/**
	 * The xslt path.
	 *
	 * @author nk510 Path to XSLT file.
	 */

//	static String xsltPath = "src/test/resources/xslt/ontochem_rdf.xsl"; //this version of xslt uses http://como.cheng.cam.ac.uk/molhub/compchem/ URI

	static String xsltPath = "src/test/resources/xslt/gxmltoowl.xsl";
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws TransformerException the transformer exception
	 * @throws IOException Signals that an I/O exception has occurred
	 * 
	 */
	
	public static void main(String[] args) throws TransformerException, IOException {

		Utility utility = new FileUtility();
		
		/**
		 * 
		 * Gets the file list.
		 *
		 * @author nk510
		 * @param folderPath <p>File list of all compchem xml files stored in a folder.</p>
		 * @return the file list. <p>Method reads all compchem XML files in given folder path and saves it in file list. Supported
		 *         file extension is '.xml'.</p>
		 *         
		 */

		File[] fileList = utility.getFileList(xmlFolderPath,".xml");

		/**
		 * 
		 * @author nk510 <p>Iterates over file list given in folder 'src/test/resources/ontochem_xml/' </p>
		 * 
		 */
        
		for (File f : fileList) {

			InputStream xmlSource = new FileInputStream(f.getPath());

			StreamSource xsltSource = new StreamSource(xsltPath);

			/**
			 * 
			 * @author nk510 <p>Creates output path for each compchem XML file from file list. Generated files have rdf file extension.</p>
			 * 
			 */

			String outputPath = "src/test/resources/ontology/compchem_abox/" + f.getName().replace(".xml", "").toString() + ".rdf";

			FileOutputStream outputStream = new FileOutputStream(new File(outputPath));

			/**
			 * @author nk510
			 * @param randomString <p>Used to create an IRI as an instance of 'compchemkb:G09' class. </p>
			 */
			String randomString= UUID.randomUUID().toString();

			/**
			 * @author nk510 <p>Runs XSLT transformation for each compchem XML file form file
			 *         list. XSLT transformation without species IRI.</p> 
			 */
			
			trasnformation(randomString,xmlSource, outputStream, xsltSource);
			
			String speciesIRI = "http://www.theworldavatar.com/kb/ontospecies/00b537ef-8b6f-3246-9a7e-edd0259c6e09.owl#00b537ef-8b6f-3246-9a7e-edd0259c6e09";
			
			/**
			 * XSLT transformation including species IRI.
			 */
//			transfromation(randomString,speciesIRI, xmlSource, outputStream, xsltSource ); 
		}
	}

	/**
	 * 
	 * Trasnformation.
	 *
	 * @author nk510
	 * @param XmlSource            <p> It is a source compchem XML file generated by parsing Gaussian files</p>
	 *            (g09)
	 * @param outputStream           <p>It is RDF file generated by using XSLT transformations.</p>
	 * @param xsltSource            <p> It is file that contains the implementation of transformations
	 *            (business logic) from compchem xml file to RDF file. </p>
	 * @param xmlFolderName xml folder name.            
	 * @throws TransformerException             <p>This method implements transformation from compchem XML files to
	 *             RDF files by using Java 8 @see{https://docs.oracle.com/javase/7/docs/api/javax/xml/transform/TransformerFactory.html}
	 *             class.</p>
	 *             
	 */

	public static void trasnformation(String xmlFolderName, InputStream XmlSource, FileOutputStream outputStream, StreamSource xsltSource)
			throws TransformerException {
		
		/**
		 * @author nk510
		 * <p>In case of using SaxonHE parser, we need to set/add the following System property:
		 * System.setProperty("javax.xml.transform.TransformerFactory","net.sf.saxon.TransformerFactoryImpl");</p>
		 */
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();	
		Transformer transformer = transformerFactory.newTransformer(xsltSource);
		
		/**
		 * @author nk510
		 * <p>To set parameter that will be used in XSLT code. In this case 'xmlFolderName' (uuid) will be used as parameter in XSLT code.</p>
		 */
		transformer.setParameter("xmlFolderName", xmlFolderName);
		
		transformer.transform(new StreamSource(XmlSource), new StreamResult(outputStream));
	
	}	
	/**
	 * 
	 * @author NK510 (caresssd@hermes.cam.ac.uk)
	 * 
	 * @param xmlFolderName  <p> xml folder name </p>
	 * @param speciesIRI <p> A species IRI </p>
	 * @param XmlSource  <p> It is a source compchem XML file generated by parsing Gaussian files</p>
	 * @param outputStream 
	 * @param xsltSource <p> It is file that contains the implementation of transformations (business logic) from compchem xml file to RDF file. </p>
	 * @throws TransformerException the exception 
	 */
	public static void transfromation(String xmlFolderName, String speciesIRI, InputStream XmlSource, FileOutputStream outputStream, StreamSource xsltSource ) throws TransformerException {
		
		/**
		 * 
		 * @author nk510 (caresssd@hermes.cam.ac.uk)
		 * 
		 * <p>In case of using SaxonHE parser, we need to set/add the following System property:
		 * System.setProperty("javax.xml.transform.TransformerFactory","net.sf.saxon.TransformerFactoryImpl");</p>
		 */
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();	
		Transformer transformer = transformerFactory.newTransformer(xsltSource);
		
		/**
		 * @author nk510 (caresssd@herems.cam.ac.uk)
		 * 
		 * <p>To set parameter that will be used in XSLT code. In this case 'xmlFolderName' (uuid) will be used as first parameter in XSLT code.</p>
		 */
		transformer.setParameter("xmlFolderName", xmlFolderName);
		
		/**
		 * @author nk510 (caresssd@herems.cam.ac.uk)
		 * 
		 * <p>To set parameter that will be used in XSLT code. In this case 'speciesIRI' will be used as a second parameter in XSLT code.</p>
		 */
		transformer.setParameter("speciesIRI",speciesIRI);
		
		
		transformer.transform(new StreamSource(XmlSource), new StreamResult(outputStream));
		
		
	}
}