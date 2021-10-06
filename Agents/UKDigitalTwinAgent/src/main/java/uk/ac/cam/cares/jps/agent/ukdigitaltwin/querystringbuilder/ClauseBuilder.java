package uk.ac.cam.cares.jps.agent.ukdigitaltwin.querystringbuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import uk.ac.cam.cares.jps.agent.ukdigitaltwin.tools.Printer;
import uk.ac.cam.cares.jps.agent.ukdigitaltwin.querystringbuilder.PowerFlowModelVariableForQuery;
import uk.ac.cam.cares.jps.base.query.sparql.PrefixToUrlMap;
import uk.ac.cam.cares.jps.base.query.sparql.Prefixes;

/**
 * ClauseBuilder developed for constructing the query string clause blocks in building, which can be used in both query and update request.
 * 
 * @author Wanni Xie (wx243@cam.ac.uk)
 * 
 */
public class ClauseBuilder implements Prefixes{

	public String entityName;
	public String entityType;
	
	public ArrayList<List<String>> prefixList = new ArrayList<List<String>>();
	public List<String> selectClause = new ArrayList<String>(); // pattern: queried entity + queried attributes 
	public ArrayList<List<String>> insertClause = new ArrayList<List<String>>();
	public ArrayList<List<String>> whereClause = new ArrayList<List<String>>(); 
	public ArrayList<List<String>> deleteClause = new ArrayList<List<String>>();
	
	public ArrayList<List<String>> optionalClause = new ArrayList<List<String>>();
	public List<String> filterClause = new ArrayList<String>();
	
	public boolean queryFlag;
	public boolean updateFlag;
	public boolean distinctFlag = true;
	public boolean reducedFlag = false;
	public int limit = -1;
	
	/**
	 * queryClauseBuilder is designed for creating the QUERY clauses
	 * 
	 * @queryFlag : the flag to identify the clause is build for SPARQL query.
	 * @updateFlag : the flag to identify the clause is build for SPARQL update.
	 * @entityName : the name of the query entity.
	 * @entityType : the query entity type, e.g. "j1:Generator".
	 * All the arguments could be passed from attribute values of an instance of a java class, e.g. PowerFlow which will be queried/updated.
	 */
	
	public  ClauseBuilder(boolean queryFlag, boolean updateFlag, String entityName, String entityType) {
		if((queryFlag && updateFlag) || (!queryFlag && !updateFlag)) {
			System.out.print("The queryFlag and updateFlag cannot be both true or false.");
		}else {
			this.queryFlag = queryFlag;
			this.updateFlag = updateFlag;
			this.entityName = entityName;
			this.entityType = entityType;
		}	
	}
	
	//TODO: overload ClauseBuilder with querySentence, insertSentence, deleteSentence....;
	
	/**
	 * prefixClauseBuilder is designed for creating the prefix clauses in a SPARQL query string
	 * 
	 * @PrefixAbbrList : a list contains the abbreviations of Prefix used in the query. The abbreviation pattern follows the PrefixToUrlMap.
	 * 
	 * Argument could be passed from attribute values of an instance of a java class, e.g. PowerFlow which will be queried/updated.
	 */
	public void prefixClauseBuilder(List<String> PrefixAbbrList){
		for(String pre: PrefixAbbrList) {
			String prefixiri = PrefixToUrlMap.getPrefixUrl(pre);
			List<String> prefixPair = Arrays.asList(pre, prefixiri);
			this.prefixList.add(prefixPair);
		}	
	} 
	
	/**
	 * selectClauseAndWhereClauseBuilderWithoutLabels is designed for creating the selectClause and WhereClause without labeled variables
	 * 
	 * @varNameIdentifier : an identifier used to distinct the object which will be used in the rdf:type triple. 
	 * @classPrefix_unlabeledVariable : a hashmap which maps the namespace of the variables class to the variables. A same namespace can map with multiple variables.
	 * @unlabeledVariable_querySentence : a LinkedHashMap maps between the unlabeled variables with its query sentence pattern collection used to construct the query body (e.g. whereClause).
	 *  
	 * All the arguments could be passed from attribute values of an instance of a java class, e.g. PowerFlow which will be queried/updated.
	 */
	
	public void selectClauseAndWhereClauseBuilderWithoutLabels(String varNameIdentifier, HashMap<String, List<String>> classPrefix_unlabeledVariable, 
			LinkedHashMap<String, List<String>> unlabeledVariable_querySentence){
		// initialise selectClause and whereClause with the query entity
		  //case 1: set the selectClause and whereClause without labels
		if(entityName.indexOf("?") != 0) {
			this.entityName = "?" + entityName;}
		this.selectClause.add(this.entityName);	
		List<String> entityTypeTriple = Arrays.asList(this.entityName, "a", this.entityType);
		this.whereClause.add(entityTypeTriple);
		
		for(String var : unlabeledVariable_querySentence.keySet()) {//set the selectClause
			List<String> querySentence = unlabeledVariable_querySentence.get(var);
			String selectName = querySentence.get(querySentence.size() - 1) + var;
			this.selectClause.add(selectName);
			for(int i = -1; i < querySentence.size() - 2; i+=2) {// set up whereClause
				if(i == -1) {
					List<String> spo = Arrays.asList(this.entityName, querySentence.get(i+1), querySentence.get(i+2) + var);
					this.whereClause.add(spo);
				} else {
					List<String> spo = Arrays.asList(querySentence.get(i)+var, querySentence.get(i+1), querySentence.get(i+2) + var);
					this.whereClause.add(spo);					
				}				
			}							
		}
		for(String classPrefix : classPrefix_unlabeledVariable.keySet()) {
			List<String> varInSameClassPrefix = classPrefix_unlabeledVariable.get(classPrefix);
			for(String var : varInSameClassPrefix) {
				List<String> varTypeTriple = Arrays.asList(varNameIdentifier+var, "a", classPrefix + ":" + var);
				this.whereClause.add(varTypeTriple);
			}			
		}		
		
	} 
	
	/**
	 * selectClauseAndWhereClauseBuilderWithLabels is designed for creating the selectClause and WhereClause with labeled variables
	 * 
	 * @varNameIdentifier : an identifier used to distinct the object which will be used in the rdf:type triple. 
	 * @labeledVariable_labels : a LinkedHashMap maps between labeled variables and its labels. Variables of a same type can have multiple labels.  
	 * @labeledVariable_classPrefix : a hashmap the variables to the namespace of the variables class.
	 * @labeledVariable_querySentence : if label is needed in constructing the query body, a label map need to be specified. The key is the variable whose going to be labeled. 
	 *             The value is the list of label. If no labels are need, it is set to null.
	 *  
	 * All the arguments could be passed from attribute values of an instance of a java class, e.g. PowerFlow which will be queried/updated.
	 */
	
	public void selectClauseAndWhereClauseBuilderWithLabels(String varNameIdentifier, LinkedHashMap<String, List<String>> labeledVariable_labels, 
			HashMap<String, String> labeledVariable_classPrefix, HashMap<String, List<String>> labeledVariable_querySentence){
		//case 2: set up selectClause and whereClause with labels											
		for(String key: labeledVariable_labels.keySet()) {// key is the variable name needs to be labeled
			List<String> querySentence = labeledVariable_querySentence.get(key);
			for(String label:labeledVariable_labels.get(key)) {
				for(int i = -1; i < querySentence.size()-2; i+=2) {// add labels
					if(i == -1) {
						List<String> spo = Arrays.asList(this.entityName, querySentence.get(i+1), querySentence.get(i+2) + label);
						this.whereClause.add(spo);
					} else {
						List<String> spo = Arrays.asList(querySentence.get(i)+ label, querySentence.get(i+1), querySentence.get(i+2) + label);
						this.whereClause.add(spo);					
					}	
			    }
				List<String> labelTriple = Arrays.asList(varNameIdentifier + label, RDFS + ":label", "\"" + label + "\"");
				this.whereClause.add(labelTriple);
				List<String> varTypeTriple = Arrays.asList(varNameIdentifier + label, "a",  labeledVariable_classPrefix.get(key) + ":" + key);					
				this.whereClause.add(varTypeTriple);
				String selectName = querySentence.get(querySentence.size() - 1) + label;
				this.selectClause.add(selectName);
		     }				
		}			
	} 
	
	/**
	 * selectClauseAndWhereClauseBuilderWithLabels is designed for creating the selectClause and WhereClause with labeled variables
	 * 
	 * @varNameIdentifier : an identifier used to distinct the object which will be used in the rdf:type triple. 
	 * @labeledVariable_labels : a LinkedHashMap maps between labeled variables and its labels. Variables of a same type can have multiple labels.  
	 * @labeledVariable_classPrefix : a hashmap the variables to the namespace of the variables class.
	 * @labeledVariable_querySentence : if label is needed in constructing the query body, a label map need to be specified. The key is the variable whose going to be labeled. 
	 *             The value is the list of label. If no labels are need, it is set to null.
	 *  
	 * All the arguments could be passed from attribute values of an instance of a java class, e.g. PowerFlow which will be queried/updated.
	 */
	
	public void insertClauseBuilder(String varNameIdentifier, LinkedHashMap<String, List<String>> labeledVariable_labels, 
			HashMap<String, String> labeledVariable_classPrefix, HashMap<String, List<String>> labeledVariable_querySentence){
		//case 2: set up selectClause and whereClause with labels											
		for(String key: labeledVariable_labels.keySet()) {// key is the variable name needs to be labeled
			List<String> querySentence = labeledVariable_querySentence.get(key);
			for(String label:labeledVariable_labels.get(key)) {
				for(int i = -1; i < querySentence.size()-2; i+=2) {// add labels
					if(i == -1) {
						List<String> spo = Arrays.asList(this.entityName, querySentence.get(i+1), querySentence.get(i+2) + label);
						this.whereClause.add(spo);
					} else {
						List<String> spo = Arrays.asList(querySentence.get(i)+ label, querySentence.get(i+1), querySentence.get(i+2) + label);
						this.whereClause.add(spo);					
					}	
			    }
				List<String> labelTriple = Arrays.asList(varNameIdentifier + label, RDFS + ":label", "\"" + label + "\"");
				this.whereClause.add(labelTriple);
				List<String> varTypeTriple = Arrays.asList(varNameIdentifier + label, "a",  labeledVariable_classPrefix.get(key) + ":" + key);					
				this.whereClause.add(varTypeTriple);
				String selectName = querySentence.get(querySentence.size() - 1) + label;
				this.selectClause.add(selectName);
		     }				
		}			
	} 
	
	
	
	
//	// check query mode: 		
//			if(!this.queryFlag){
//				System.out.print("The method is for query only.");
//			    System.exit(0);}
//			
//			//check query sentence		
//			for(String key: unlabeledVariable_querySentence.keySet()) {
//				int sentenceLen = unlabeledVariable_querySentence.get(key).size();
//				if (sentenceLen % 2 != 0) {
//					System.out.print("The lenght of querySentence is " + sentenceLen + " of variable " + key + ".");
//					System.out.print("The lenght of querySentence should be even integer and it should be formated in p-o pairs.");
//				    System.exit(0);}
//			}	
//			
//			//check the format of entityType
//			if(!entityType.contains(":") || entityType.indexOf(":") == 0 || entityType.indexOf(":") == entityType.length() - 1) {
//				System.out.print("The entityType should be given in the farmat as nameSpace:className.");
//			    System.exit(0);
//			}
//			
//			//check the length of labeledVariable_labels, labeledVariable_classPrefix and labeledVariable_querySentence
//			if(labeledVariable_labels!=null && labeledVariable_classPrefix !=null && labeledVariable_querySentence != null && 
//					(labeledVariable_labels.size() != labeledVariable_classPrefix.size() || labeledVariable_labels.size() != labeledVariable_querySentence.size())) {
//				System.out.print("The length of labeledVariable_labels, labeledVariable_classPrefix and labeledVariable_querySentence should be equal.");
//			    System.exit(0);
//			}
//			if((labeledVariable_labels!=null && labeledVariable_classPrefix == null) || (labeledVariable_labels == null && labeledVariable_classPrefix != null)) {
//				System.out.print("The labeledVariable_labels and labeledVariable_classPrefix should be both null or both having content.");
//			    System.exit(0);
//			}
//			
//			if(labeledVariable_labels!=null) {
//				if(labeledVariable_classPrefix == null || labeledVariable_querySentence == null) {
//					System.out.print("The labeledVariable_labels, labeledVariable_classPrefix and labeledVariable_querySentence should be all null or all having content.");
//				    System.exit(0);}
//				} else {
//					if(labeledVariable_classPrefix != null || labeledVariable_querySentence != null) {
//						System.out.print("The labeledVariable_labels, labeledVariable_classPrefix and labeledVariable_querySentence should be all null or all having content.");
//					    System.exit(0);}
//				}
				
	

	
	 public static void main(String[] args) {
		  
		  PowerFlowModelVariableForQuery pfmv = new PowerFlowModelVariableForQuery(false, 2);	
		  ClauseBuilder pb = new ClauseBuilder(true, false, pfmv.genEntityName, pfmv.entityType);
		  List<String> vl = pfmv.PowerFlowModelVariablesMap.get(pfmv.genCostFuncKey);
		  HashMap<String, List<String>> classPre_var = new HashMap<String, List<String>>();
		  classPre_var.put(pfmv.variableTypePrefix, vl);
		  LinkedHashMap<String, List<String>> unlabeledVariable_querySentence = new LinkedHashMap<String, List<String>>();
		  for(int i = 0 ; i < vl.size(); i++) {
			  unlabeledVariable_querySentence.put(vl.get(i), pfmv.queryModelVariableSentence);			  
		  }		
		  pb.prefixClauseBuilder(pfmv.PrefixAbbrList);
		  pb.selectClauseAndWhereClauseBuilderWithoutLabels(pfmv.varNameIdentifier, classPre_var, unlabeledVariable_querySentence);
		  pb.selectClauseAndWhereClauseBuilderWithLabels(pfmv.varNameIdentifier, pfmv.labelMap, pfmv.labelVarCalssNameSpaceMap, pfmv.labeledVariable_querySentence);
		  ArrayList<List<String>> wc  = pb.whereClause;
		  Printer.printArrayList(wc);
		  ArrayList<List<String>> pl  = pb.prefixList;
		  Printer.printArrayList(pl);
		  System.out.println(pb.selectClause); 
		  
		  System.out.println(pb.filterClause.size()); 
	 }
	 
	 
}