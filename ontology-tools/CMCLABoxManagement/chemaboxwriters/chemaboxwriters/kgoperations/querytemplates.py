from chemaboxwriters.kgoperations.queryendpoints import SPARQL_ENDPOINTS
from chemaboxwriters.kgoperations.querykg import querykg

# from py4j.java_gateway import Py4JJavaError
import logging
import re
from SPARQLWrapper import SPARQLWrapper, JSON
from typing import Optional

logger = logging.getLogger(__name__)


def spec_inchi_query(inchi_string):

    inchi_string_standard = re.sub("^InChI=1(?!S)", "InChI=1S", inchi_string)
    inchi_string_non_standard = re.sub("^InChI=1S", "InChI=1", inchi_string)

    query = """
    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
    PREFIX OntoSpecies: <http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    SELECT ?speciesIRI
    WHERE
    {
    OPTIONAL {
           ?speciesIRI rdf:type OntoSpecies:Species .
           ?speciesIRI OntoSpecies:inChI "#inchi_string_standard#"^^xsd:string .}
    OPTIONAL {
           ?speciesIRI rdf:type OntoSpecies:Species .
           ?speciesIRI OntoSpecies:inChI "#inchi_string_non_standard#"^^xsd:string .}
    }
    """
    query = query.replace("#inchi_string_standard#", inchi_string_standard).replace(
        "#inchi_string_non_standard#", inchi_string_non_standard
    )
    return query


def get_species_iri(inchi: str) -> Optional[str]:
    # Query OntoSpecies to find Species IRI that corresponds to a given InChI.
    speciesIRI = None
    results = []
    try:
        # results  = querykg(SPARQL_ENDPOINTS['ontospecies'], spec_inchi_query(inchi)) #query_endpoint(endpoint, spec_inchi_query(inchi))
        # TODO, find a way to fix this query on the java side so that it works as expected
        sparql = SPARQLWrapper(SPARQL_ENDPOINTS["ontospecies"])
        sparql.setReturnFormat(JSON)
        query = spec_inchi_query(inchi)
        sparql.setQuery(query)
        results = sparql.queryAndConvert()
    except Exception:
        logger.warning("Warning: get_species_iri query failed.")
    if results:
        bindings = results["results"]["bindings"][0]  # type: ignore
        target = bindings.get("speciesIRI")
        if target is not None:
            speciesIRI = target.get("value")
    return speciesIRI


def get_assemblyModel(modularity, planarity, gbu_number, symmetry):
    # queries the assembly model of a particular MOP.
    queryStr = """
    PREFIX OntoMOPs: <http://www.theworldavatar.com/ontology/ontomops/OntoMOPs.owl#>
	PREFIX OntoSpecies: <http://www.theworldavatar.com/ontology/ontospecies/OntoSpecies.owl#>
	PREFIX Measure: <http://www.ontology-of-units-of-measure.org/resource/om-2/>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
	PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?AssemblyModel ?NumberValue ?Planarity ?Modularity ?Symmetry
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
    FILTER ((?Modularity) = "#MODULARITY") .
    FILTER ((?Planarity) = "#PLANARITY") .
    FILTER ((?NumberValue) = "#NUMBER") .
    FILTER ((?Symmetry) = "#SYMMETRY") .
    }"""
    queryStr = queryStr.replace("#MODULARITY", str(modularity))
    queryStr = queryStr.replace("#PLANARITY", str(planarity))
    queryStr = queryStr.replace("#NUMBER", str(gbu_number))
    queryStr = queryStr.replace("#SYMMETRY", str(symmetry))
    return queryStr


def get_assembly_iri(modularity, planarity, gbu_number, symmetry):
    # Query OntoSpecies to find Species IRI that corresponds to a given InChI.
    target = None
    results = querykg(
        SPARQL_ENDPOINTS["ontomops"],
        get_assemblyModel(modularity, planarity, gbu_number, symmetry),
    )
    if results:
        target = []
        for k in range(len(results)):
            if "AssemblyModel" in results[k].keys():
                target.append(results[k]["AssemblyModel"])
    return target
