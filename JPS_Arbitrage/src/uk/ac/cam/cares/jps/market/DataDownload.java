package uk.ac.cam.cares.jps.market;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.hp.hpl.jena.util.FileUtils;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import uk.ac.cam.cares.jps.util.PythonHelper;

public class DataDownload {

	
	
	public static String Downloading_market_data() throws Exception {
		
		/** this function executes 4 Python scripts which download market data and stores it in separate CSV files */ 
		
		String CPO_download = new String("caresjpsarbitrage/CPO_download.pyw"); 
		String FAME_download = new String("caresjpsarbitrage/FAME_download.pyw"); 
		String ZCE_download = new String("caresjpsarbitrage/ZCE_download.pyw"); 
		String HNG_download = new String("caresjpsarbitrage/HNG_download.pyw");
		
		String CPO_data = new String("C:\\Users\\Janusz\\Desktop\\JParkSimulator-git\\JPS_Arbitrage\\workingdir\\CPO_data.csv"); 
		String FAME_data = new String("C:\\Users\\Janusz\\Desktop\\JParkSimulator-git\\JPS_Arbitrage\\workingdir\\FAME_data.csv");
		String ZCE_data = new String("C:\\Users\\Janusz\\Desktop\\JParkSimulator-git\\JPS_Arbitrage\\workingdir\\ZCE_data.csv"); 
		String HNG_data = new String("C:\\Users\\Janusz\\Desktop\\JParkSimulator-git\\JPS_Arbitrage\\workingdir\\HNG_data.csv"); 
		
		String[][] commands = {{CPO_download, CPO_data},
				{FAME_download, FAME_data},
				{ZCE_download, ZCE_data},
				{HNG_download, HNG_data}
				};
		
		for (int i = 0; i <commands.length; i++){
			String result = PythonHelper.callPython(commands[i][0], commands[i][1], new DataDownload());
			System.out.println(commands[i][0]+" "+result);
		}

		
		/** URIs of ontologies used to define KBs in which market data will be stored*/
		String ontoPath = "http://www.mascem.gecad.isep.ipp.pt/ontologies/electricity-markets.owl";
		String ontoPath2 = "http://www.semanticweb.org/janusz/ontologies/2018/3/untitled-ontology-13";

		 
		
		/** knowledge base from an owl file in a jenaOWL model; URIs of relevant individuals and their properties are defined and
		 * locations of the CSV files with the market data are stored in KB one by one */
		String filePath = "C:/Users/Janusz/Desktop/Commodity_prices/Ontology/OntoArbitrage_Market_KB.owl";
		FileInputStream inFile= new FileInputStream(filePath);
		Reader in = new InputStreamReader(inFile,"UTF-8");
		JenaOWLModel jenaOwlModel = ProtegeOWL.createJenaOWLModelFromReader(in);
		
		
		String[][] addresses = {{ontoPath+"#"+"data", ontoPath2+"#"+"CMECrudePalmOil_001"},
				{ontoPath+"#"+"data", ontoPath2+"#"+"CMEBiodiesel_001"},
				{ontoPath+"#"+"data", ontoPath2+"#"+"ZCEMethanol_001"},
				{ontoPath+"#"+"data", ontoPath2+"#"+"CMENaturalGas_001"}
				};
		
		for (int i = 0; i <addresses.length; i++){
			RDFProperty property = jenaOwlModel.getRDFProperty(addresses[i][0]);
			RDFIndividual individual = jenaOwlModel.getRDFIndividual(addresses[i][1]);
			individual.setPropertyValue(property, commands[i][1]);
		}
				

		   
		/**save the updated model file; also, any error messages are collected and printed */
		Collection errors = new ArrayList();
		jenaOwlModel.save(new URI("file:/"+filePath), FileUtils.langXMLAbbrev, errors, jenaOwlModel.getOntModel());
		System.out.println("File saved with " + errors.size() + " errors.");  
		  
		   return "Nothing for now";
		   
	}
	
	public static void Downloading_currencies() throws Exception {
		
		/** this function executes a Python script which downloads exchange rates and stores it in separate CSV files;
		 * the currencies are defined within the script; the rates are printed to the console by the script thus allowing to store them
		 * in KB */ 

		String currency_download = new String("caresjpsarbitrage/exchange_rates.pyw"); 

		String currency_data = new String("C:\\Users\\Janusz\\Desktop\\JParkSimulator-git\\JPS_Arbitrage\\workingdir\\exchange_rates.csv"); 


		String result = PythonHelper.callPython(currency_download, currency_data, new DataDownload());
		System.out.println(result);

		/** split the console output into headers and exchange rates*/
		int results_size =result.split(",").length;
		String[] headers = Arrays.copyOfRange(result.split(","), 0, results_size/2);
		String[] rates = Arrays.copyOfRange(result.split(","), results_size/2,results_size);

		
		
		/** URIs of ontologies used to define KBs in which market data will be stored*/
		String ontoPath = "http://www.semanticweb.org/janusz/ontologies/2018/3/untitled-ontology-15"; //KB
		String ontoPath2 = "http://www.theworldavatar.com/OntoCAPE/OntoCAPE/upper_level/system.owl";
		 
		/** URIs of relevant individuals and their properties are defined */
		String[][] addresses = new String[headers.length][];
		for (int i = 0; i <addresses.length; i++){
			addresses[i] = new String[] {ontoPath2+"#"+"numericalValue", ontoPath+"#"+"V_"+headers[i]};
			System.out.println(addresses[i][1]);
		}
		
		
		
		/** knowledge base from an owl file in a jenaOWL model; rates are stored in KB one by one */
		String filePath = "C:/Users/Janusz/Desktop/Commodity_prices/Ontology/OntoArbitrage_PlantInfo_KB.owl";
		FileInputStream inFile= new FileInputStream(filePath);
		Reader in = new InputStreamReader(inFile,"UTF-8");
		JenaOWLModel jenaOwlModel = ProtegeOWL.createJenaOWLModelFromReader(in);

		for (int i = 0; i <addresses.length; i++){
			RDFProperty property = jenaOwlModel.getRDFProperty(addresses[i][0]);
			RDFIndividual individual = jenaOwlModel.getRDFIndividual(addresses[i][1]);
			individual.setPropertyValue(property, rates[i]);
		}
				

		/**save the updated model file; also, any error messages are collected and printed*/
		Collection errors = new ArrayList();
		jenaOwlModel.save(new URI("file:/"+filePath), FileUtils.langXMLAbbrev, errors, jenaOwlModel.getOntModel());
		System.out.println("File saved with " + errors.size() + " errors.");  
		   
		   
		   
	}
	
	public static void Storing_Aspen_data() throws Exception {
		
		/** this function executes a Python script which prints input and output headers and data from an Aspen model;
		 *  information to be sourced from the model and printed is defined in the script;
		 *  data is stored in the relevant KB*/ 
		
		String Aspen_data = new String("caresjpsarbitrage/print_Aspen_data.pyw");
		
		String result = PythonHelper.callPython(Aspen_data, "1", new DataDownload());
		System.out.println(result);
		
		/** split the console output into headers and exchange rates*/
		int results_size =result.split(",").length;
		String[] headers = Arrays.copyOfRange(result.split(","), 0, results_size/2);
		String[] data = Arrays.copyOfRange(result.split(","), results_size/2,results_size);
		
		/**
		for (int i = 0; i <headers.length; i++){
			System.out.println(headers[i]);
			System.out.println(rates[i]);
		}
		*/

		
		/** URIs of ontologies used to define KBs in which market data will be stored */
		String ontoPath = "http://www.semanticweb.org/janusz/ontologies/2018/3/untitled-ontology-15"; //KB
		String ontoPath2 = "http://www.theworldavatar.com/OntoCAPE/OntoCAPE/upper_level/system.owl";
		
		/** URIs of relevant individuals and their properties are defined*/
		String[][] addresses = new String[headers.length][];

		for (int i = 0; i <addresses.length; i++){
			addresses[i] = new String[] {ontoPath2+"#"+"numericalValue", ontoPath+"#"+"V_"+headers[i]};
		}
		 
		
		
		/** knowledge base from an owl file in a jenaOWL model; rates are stored in KB one by one */
		String filePath = "C:/Users/Janusz/Desktop/Commodity_prices/Ontology/OntoArbitrage_PlantInfo_KB.owl";
		FileInputStream inFile= new FileInputStream(filePath);
		Reader in = new InputStreamReader(inFile,"UTF-8");
		JenaOWLModel jenaOwlModel = ProtegeOWL.createJenaOWLModelFromReader(in);

		for (int i = 0; i <addresses.length; i++){
			RDFProperty property = jenaOwlModel.getRDFProperty(addresses[i][0]);
			RDFIndividual individual = jenaOwlModel.getRDFIndividual(addresses[i][1]);
			individual.setPropertyValue(property, data[i]);
		}
				

		/**save the updated model file; also, any error messages are collected and printed*/
		Collection errors = new ArrayList();
		jenaOwlModel.save(new URI("file:/"+filePath), FileUtils.langXMLAbbrev, errors, jenaOwlModel.getOntModel());
		System.out.println("File saved with " + errors.size() + " errors.");  
		   
		   
		   
	}
	
	public static void Reading_data() throws Exception {
		
		/** this function executes a Python script which prints input and output headers and data from an Aspen model;
		 *  information to be sourced from the model and printed is defined in the script;
		 *  data is stored in the relevant KB*/ 
		
		String print = new String("caresjpsarbitrage/print_headers.pyw");
		
		String result = PythonHelper.callPython(print, "1", new DataDownload());
		System.out.println(result);
		
		/** split the console output into headers and exchange rates*/
		String[] headers = result.split(",");
		
		/** URIs of ontologies used to define KBs in which market data will be stored*/ 
		String ontoPath = "http://www.semanticweb.org/janusz/ontologies/2018/3/untitled-ontology-15"; //KB
		String ontoPath2 = "http://www.theworldavatar.com/OntoCAPE/OntoCAPE/upper_level/system.owl";
		
		
		/** ontology addresses*/
		String ontoPath3 = "http://www.mascem.gecad.isep.ipp.pt/ontologies/electricity-markets.owl";
		String ontoPath4 = "http://www.semanticweb.org/janusz/ontologies/2018/3/untitled-ontology-13";
		
		
		
		String[][] addresses2 = {{ontoPath3+"#"+"data", ontoPath4+"#"+"CMECrudePalmOil_001"},
				{ontoPath3+"#"+"data", ontoPath4+"#"+"CMEBiodiesel_001"},
				{ontoPath3+"#"+"data", ontoPath4+"#"+"ZCEMethanol_001"},
				{ontoPath3+"#"+"data", ontoPath4+"#"+"CMENaturalGas_001"}
				};

		
		/** URIs of relevant individuals and their properties are defined */
		String[][] addresses = new String[headers.length][];
		for (int i = 0; i <addresses.length; i++){
			addresses[i] = new String[] {ontoPath2+"#"+"numericalValue", ontoPath+"#"+headers[i]};
			System.out.println(addresses[i][1]);
		}
		

		   /**get model from an owl file*/
		   String filePath = "C:/Users/Janusz/Desktop/Commodity_prices/Ontology/OntoArbitrage_PlantInfo_KB.owl";
		   OWLModel owlModel = null;
		   
		   try {
		      owlModel = ProtegeOWL.createJenaOWLModelFromURI("file:/"+filePath);
		     } catch (OntologyLoadException e1) {
		      e1.printStackTrace();
		     }

				
			for (int i = 0; i <addresses.length; i++){
				RDFIndividual individual = owlModel.getRDFIndividual(addresses[i][1]);
				String name = individual.getPropertyValueLiteral(owlModel.getRDFProperty(addresses[i][0])).getString();
				System.out.println(name);
				}

			
			
			   /**get model from an owl file*/
			   String filePath2 = "C:/Users/Janusz/Desktop/Commodity_prices/Ontology/OntoArbitrage_Market_KB.owl";
			   OWLModel owlModel2 = null;
			   
			   try {
				   owlModel2 = ProtegeOWL.createJenaOWLModelFromURI("file:/"+filePath2);
			     } catch (OntologyLoadException e1) {
			      e1.printStackTrace();
			     }
			
			
			for (int i = 0; i <addresses2.length; i++){
				RDFIndividual individual = owlModel2.getRDFIndividual(addresses2[i][1]);
				String name = individual.getPropertyValueLiteral(owlModel2.getRDFProperty(addresses2[i][0])).getString();
				System.out.println(name);
				}
		   
		   
	}
	
	public static void main(String[] args) throws Exception {
		//Downloading_market_data();
		//Downloading_currencies();
		//Storing_Aspen_data();
		//Reading_data();
	}
	
}
