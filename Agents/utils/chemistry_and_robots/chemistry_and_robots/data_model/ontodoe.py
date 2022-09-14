import pydantic
from rdflib import Graph, URIRef, RDF, RDFS, OWL, XSD, Literal
from typing import List, Optional

from chemistry_and_robots.data_model.iris import *
from pyderivationagent.data_model.utils import *

from chemistry_and_robots.data_model.base_ontology import BaseOntology
from chemistry_and_robots.data_model.ontoreaction import *

class Strategy(BaseOntology):
    clz: str = ONTODOE_STRATEGY

    def create_instance_for_kg(self, g: Graph):
        g.add((URIRef(self.instance_iri), RDF.type, URIRef(self.clz)))
        return g

class TSEMO(Strategy):
    clz: str = ONTODOE_TSEMO
    # this refers to the realisation of TSEMO algorithm in Summit python package
    # below are default value in Summit python package
    # more details, please visit: https://gosummit.readthedocs.io/en/latest/strategies.html#tsemo
    nRetries: int = 10
    nSpectralPoints: int = 1500
    nGenerations: int = 100
    populationSize: int = 100

    def create_instance_for_kg(self, g: Graph):
        g = super().create_instance_for_kg(g)
        g.add((URIRef(self.instance_iri), URIRef(ONTODOE_NRETRIES), Literal(self.nRetries)))
        g.add((URIRef(self.instance_iri), URIRef(ONTODOE_NSPECTRALPOINTS), Literal(self.nSpectralPoints)))
        g.add((URIRef(self.instance_iri), URIRef(ONTODOE_NGENERATIONS), Literal(self.nGenerations)))
        g.add((URIRef(self.instance_iri), URIRef(ONTODOE_POPULATIONSIZE), Literal(self.populationSize)))
        return g

class LHS(Strategy):
    clz: str = ONTODOE_LHS
    seed: int
    # TODO add support for object property <hasCriterion> <OntoDoE:Criterion>

class DesignVariable(BaseOntology):
    clz: str = ONTODOE_DESIGNVARIABLE

    def create_instance_for_kg(self, g: Graph):
        g.add((URIRef(self.instance_iri), RDF.type, URIRef(self.clz)))
        return g

class ContinuousVariable(DesignVariable):
    clz: str = ONTODOE_CONTINUOUSVARIABLE
    upperLimit: float
    lowerLimit: float
    positionalID: Optional[int]
    # instead of the actual class, str is used to host the concept IRI of om:Quantity for simplicity
    refersTo: str

    def create_instance_for_kg(self, g: Graph):
        g = super().create_instance_for_kg(g)
        g.add((URIRef(self.instance_iri), URIRef(ONTODOE_UPPERLIMIT), Literal(self.upperLimit)))
        g.add((URIRef(self.instance_iri), URIRef(ONTODOE_LOWERLIMIT), Literal(self.lowerLimit)))
        if self.positionalID is not None:
            g.add((URIRef(self.instance_iri), URIRef(ONTODOE_POSITIONALID), Literal(self.positionalID)))
        g.add((URIRef(self.instance_iri), URIRef(ONTODOE_REFERSTO), URIRef(self.refersTo)))
        return g

    @pydantic.root_validator
    @classmethod
    def upper_and_lower_limit(cls, values):
        # validate the upper and lower limit
        if values.get('upperLimit') <= values.get('lowerLimit'):
            raise Exception(
                'ContinuousVariable <%s> has an UpperLimit %s that is smaller then its LowerLimit %s.' 
                % (values.get('instance_iri'), values.get('upperLimit'), values.get('lowerLimit')))
        return values

class CategoricalVariable(DesignVariable):
    clz: str = ONTODOE_CATEGORICALVARIABLE
    pass

class Domain(BaseOntology):
    clz: str = ONTODOE_DOMAIN
    hasDesignVariable: List[DesignVariable]

    def create_instance_for_kg(self, g: Graph):
        # create instance for Domain
        g.add((URIRef(self.instance_iri), RDF.type, URIRef(self.clz)))

        # create instance for each DesignVariable
        for design_variable in self.hasDesignVariable:
            design_variable.create_instance_for_kg(g)
            g.add((URIRef(self.instance_iri), URIRef(ONTODOE_HASDESIGNVARIABLE), URIRef(design_variable.instance_iri)))

        return g

class SystemResponse(BaseOntology):
    clz: str = ONTODOE_SYSTEMRESPONSE
    maximise: bool
    positionalID: Optional[int]
    # instead of the actual class, str is used to host the concept IRI of om:Quantity for simplicity
    refersTo: str

    def create_instance_for_kg(self, g: Graph):
        g.add((URIRef(self.instance_iri), RDF.type, URIRef(self.clz)))
        g.add((URIRef(self.instance_iri), URIRef(ONTODOE_MAXIMISE), Literal(self.maximise)))
        if self.positionalID is not None:
            g.add((URIRef(self.instance_iri), URIRef(ONTODOE_POSITIONALID), Literal(self.positionalID)))
        g.add((URIRef(self.instance_iri), URIRef(ONTODOE_REFERSTO), URIRef(self.refersTo)))
        return g

class HistoricalData(BaseOntology):
    clz: str = ONTODOE_HISTORICALDATA
    refersTo: List[ReactionExperiment]
    numOfNewExp: int = 1

    def create_instance_for_kg(self, g: Graph):
        # create instance for HistoricalData
        g.add((URIRef(self.instance_iri), RDF.type, URIRef(self.clz)))

        # add connection to each ReactionExperiment
        # NOTE here we don't collect triples for each ReactionExperiment, we only make the connection
        for reaction_experiment in self.refersTo:
            g.add((URIRef(self.instance_iri), URIRef(ONTODOE_REFERSTO), URIRef(reaction_experiment.instance_iri)))

        # add number of new experiments
        g.add((URIRef(self.instance_iri), URIRef(ONTODOE_NUMOFNEWEXP), Literal(self.numOfNewExp)))

        return g

class DesignOfExperiment(BaseOntology):
    clz: str = ONTODOE_DESIGNOFEXPERIMENT
    usesStrategy: Strategy
    hasDomain: Domain
    hasSystemResponse: List[SystemResponse]
    utilisesHistoricalData: HistoricalData
    proposesNewExperiment: Optional[ReactionExperiment]

    def create_instance_for_kg(self, g: Graph):
        # create an instance of DesignOfExperiment
        g.add((URIRef(self.instance_iri), RDF.type, URIRef(self.clz)))

        # add the strategy
        g.add((URIRef(self.instance_iri), URIRef(ONTODOE_USESSTRATEGY), URIRef(self.usesStrategy.instance_iri)))
        g = self.usesStrategy.create_instance_for_kg(g)

        # add the domain
        g.add((URIRef(self.instance_iri), URIRef(ONTODOE_HASDOMAIN), URIRef(self.hasDomain.instance_iri)))
        g = self.hasDomain.create_instance_for_kg(g)

        # add the system response
        for system_response in self.hasSystemResponse:
            g.add((URIRef(self.instance_iri), URIRef(ONTODOE_HASSYSTEMRESPONSE), URIRef(system_response.instance_iri)))
            g = system_response.create_instance_for_kg(g)

        # add the historical data
        g.add((URIRef(self.instance_iri), URIRef(ONTODOE_UTILISESHISTORICALDATA), URIRef(self.utilisesHistoricalData.instance_iri)))
        g = self.utilisesHistoricalData.create_instance_for_kg(g)

        return g