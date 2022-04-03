import json
import os
import time
import pytest
from pathlib import Path
from testcontainers.core.container import DockerContainer

from metoffice.dataretrieval.stations import *
from metoffice.errorhandling.exceptions import APIException

# Import module under test from gasgridagent
from metoffice.datainstantiation.stations import *


@pytest.fixture()
def initialise_triple_store():
    # Define temporary Docker container based on empty Blazegraph image from CMCL registry
    blazegraph = DockerContainer('docker.cmclinnovations.com/blazegraph_for_tests:1.0.0')
    blazegraph.with_exposed_ports(9999)
    yield blazegraph


def test_instantiate_stations(initialise_triple_store):

    # Read test station data
    station_data = _read_station_data()
    data1 = [station_data['station1']]
    data2 = [station_data['station1'], station_data['station2']]
    data3 = [station_data['station3']]

    # Spin up temporary docker container
    with initialise_triple_store as container:
        # Wait some arbitrary time until container is reachable
        time.sleep(3)
        # Retrieve SPARQL endpoint
        endpoint = _get_sparql_endpoint(container)

        # Verify that knowledge base is empty
        res = get_all_metoffice_stations(query_endpoint=endpoint)
        assert len(res) == 0

        # Instantiate first station  
        instantiate_stations(data1, query_endpoint=endpoint, update_endpoint=endpoint)      
        res = get_all_metoffice_stations(query_endpoint=endpoint)
        assert res[0] == station_data['station1']['id']
        triples = _get_number_of_triples(endpoint)
        assert triples == 6

        # Instantiate second station   
        instantiate_stations(data2, query_endpoint=endpoint, update_endpoint=endpoint)           
        res = get_all_metoffice_stations(query_endpoint=endpoint)
        assert len(res) == 3
        triples = _get_number_of_triples(endpoint)
        assert triples == 17

        # Instantiate third station   
        instantiate_stations(data3, query_endpoint=endpoint, update_endpoint=endpoint)           
        res = get_all_metoffice_stations(query_endpoint=endpoint)
        assert len(res) == 4
        triples = _get_number_of_triples(endpoint)
        assert triples == 21


def test_retrieve_api_data_exceptions():

    with pytest.raises(APIException) as excinfo:
    # Check correct exception type
        retrieve_api_data(None)
    # Check correct exception message
    expected = 'No Met Office DataPoint API key provided.'
    assert expected in str(excinfo.value)


def test_instantiate_all_stations(initialise_triple_store, mocker):

    # Read test station data
    station_data = _read_station_data()
    station_data = [station_data[i] for i in station_data]
    # Mock call to Met Office DataPoint API
    m = mocker.patch('metoffice.datainstantiation.stations.retrieve_api_data',
                     return_value=station_data)

    # Spin up temporary docker container
    with initialise_triple_store as container:
        # Wait some arbitrary time until container is reachable
        time.sleep(3)
        # Retrieve SPARQL endpoint
        endpoint = _get_sparql_endpoint(container)

        # Verify that knowledge base is empty
        res = get_all_metoffice_stations(query_endpoint=endpoint)
        assert len(res) == 0

        # Instantiate all stations
        instantiate_all_stations('test_api_key', query_endpoint=endpoint,
                                 update_endpoint=endpoint)
        # Verify that data gets added
        res = get_all_metoffice_stations(query_endpoint=endpoint)
        assert len(res) == 3
        triples = _get_number_of_triples(endpoint)
        assert triples == 15

        # Instantiate all stations
        instantiate_all_stations('test_api_key', query_endpoint=endpoint,
                                 update_endpoint=endpoint)
        # Verify that same data does not get added twice
        res = get_all_metoffice_stations(query_endpoint=endpoint)
        assert len(res) == 3
        triples = _get_number_of_triples(endpoint)
        assert triples == 15


def _get_sparql_endpoint(docker_container):
    # Retrieve SPARQL endpoint for temporary testcontainer
    # endpoint acts as both Query and Update endpoint
    endpoint = 'http://' + docker_container.get_container_host_ip().replace('localnpipe', 'localhost') + ':' \
               + docker_container.get_exposed_port(9999)
    # 'kb' is default namespace in Blazegraph
    endpoint += '/blazegraph/namespace/kb/sparql'
    return endpoint


def _get_number_of_triples(query_endpoint):
    # Construct KG client with correct query
    kg_client = KGClient(query_endpoint, query_endpoint)
    query_string = \
        """
        SELECT (count(*) as ?count)
        WHERE {?s ?p ?o}
        """
    # Execute query
    results = kg_client.performQuery(query=query_string)
    # Extract results
    res = int(results[0]['count'])
    
    return res


def _read_station_data():
    fp = os.path.join(Path(__file__).parent, "data", "station_data.json")
    with open(fp, 'r') as file:
        stations = json.load(file)
    return stations
