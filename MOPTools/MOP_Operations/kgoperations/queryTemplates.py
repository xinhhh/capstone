def getMOPIRIs():
    queryStr = """
    PREFIX OntoMOPs: <http://www.theworldavatar.com/ontology/ontomops/OntoMOPs.owl#>
	PREFIX OntoSpecies: <http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#>
	PREFIX Measure: <http://www.ontology-of-units-of-measure.org/resource/om-2/>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    SELECT ?mopIRI ?MOPFormula ?speciesIRI  ?CBUFormula ?NumberValue ?Planarity ?Modularity ?Symbol
    WHERE
    {   
    ?mopIRI OntoMOPs:hasMOPFormula ?MOPFormula .
    ?mopIRI OntoMOPs:hasAssemblyModel ?AssemblyModel .
    ?AssemblyModel OntoMOPs:hasPolyhedralShape ?PolhedralShape .
    ?PolhedralShape OntoMOPs:hasSymbol ?Symbol .
    ?AssemblyModel OntoMOPs:hasGenericBuildingUnitNumber ?GBUNumber .
    ?GBUNumber OntoMOPs:isNumberOf ?GBU .
    ?GBU OntoMOPs:hasPlanarity ?Planarity .
    ?GBU OntoMOPs:hasModularity ?Modularity . 
	?GBUNumber OntoSpecies:value ?NumberValue .
    ?mopIRI OntoMOPs:hasChemicalBuildingUnit ?CBU .
    ?CBU OntoMOPs:isFunctioningAs ?GBU .
    ?CBU OntoMOPs:hasCBUFormula ?CBUFormula .
    ?CBU OntoSpecies:hasUniqueSpecies ?speciesIRI .    
    }"""
    return queryStr

def mop_GBUs(mopIRI):
    queryStr = """
    PREFIX OntoMOPs: <http://www.theworldavatar.com/ontology/ontomops/OntoMOPs.owl#>
	PREFIX OntoSpecies: <http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#>
	PREFIX Measure: <http://www.ontology-of-units-of-measure.org/resource/om-2/>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    SELECT ?mopIRI ?MOPFormula ?CBUFormula ?NumberValue ?Planarity ?Modularity ?Symmetry ?MOPReference
    WHERE
    {   
    ?mopIRI OntoMOPs:hasMOPFormula ?MOPFormula .
    ?mopIRI OntoMOPs:hasProvenance ?Provenance .
    ?Provenance OntoMOPs:hasReferenceDOI ?MOPReference . 
    ?mopIRI OntoMOPs:hasAssemblyModel ?AssemblyModel .
    ?AssemblyModel OntoMOPs:hasSymmetryPointGroup ?Symmetry .
    ?AssemblyModel OntoMOPs:hasGenericBuildingUnitNumber ?GBUNumber .
    ?GBUNumber OntoMOPs:isNumberOf ?GBU .
    ?GBU OntoMOPs:hasPlanarity ?Planarity .
    ?GBU OntoMOPs:hasModularity ?Modularity . 
	?GBUNumber OntoSpecies:value ?NumberValue .
    ?mopIRI OntoMOPs:hasChemicalBuildingUnit ?CBU .
    ?CBU OntoMOPs:isFunctioningAs ?GBU .
    ?CBU OntoMOPs:hasCBUFormula ?CBUFormula .
    ?CBU OntoSpecies:hasUniqueSpecies ?speciesIRI .
    FILTER ((?mopIRI) = <#MOPIRI>) .  
    }"""
    queryStr = queryStr.replace('#MOPIRI', str(mopIRI))
    return queryStr

def mop_reference(string):
    queryStr = """
    PREFIX OntoMOPs: <http://www.theworldavatar.com/ontology/ontomops/OntoMOPs.owl#>
	PREFIX OntoSpecies: <http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#>
	PREFIX Measure: <http://www.ontology-of-units-of-measure.org/resource/om-2/>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    SELECT ?MOPReference ?MOPFormula ?mopIRI
    WHERE
    {   
    ?mopIRI OntoMOPs:hasMOPFormula ?MOPFormula .
    ?mopIRI OntoMOPs:hasProvenance ?Provenance .
    ?Provenance OntoMOPs:hasReferenceDOI ?MOPReference .   
    FILTER ((?MOPFormula) = "#MOPReference") .
    }"""
    queryStr = queryStr.replace('#MOPReference', str(string))
    return queryStr