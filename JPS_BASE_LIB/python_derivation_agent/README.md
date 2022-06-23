# Description #

The `pyderivationagent` package provides a python wrapper for derivation agents as part of [TheWorldAvatar](https://github.com/cambridge-cares/TheWorldAvatar) project. It is a python equivalent of `uk.ac.cam.cares.jps.base.agent.DerivationAgent.java` but based on [Flask](https://flask.palletsprojects.com/en/2.0.x/) application behind a [gunicorn](https://gunicorn.org/) server, inspired by the [Example: Python agent](https://github.com/cambridge-cares/TheWorldAvatar/tree/main/Deploy/examples/python_agent). `pyderivationagent` uses `py4jps>=1.0.21` to access derivation operations provided in `uk.ac.cam.cares.jps.base.derivation.DerivationClient.java`. For technical details, below are a few useful links:
- [`py4jps`](https://github.com/cambridge-cares/TheWorldAvatar/tree/main/JPS_BASE_LIB/python_wrapper) - python wrapper for jps-base-lib
- [`DerivationClient`](https://github.com/cambridge-cares/TheWorldAvatar/blob/main/JPS_BASE_LIB/src/main/java/uk/ac/cam/cares/jps/base/derivation) - derivation framework
- [`DerivationAgent.java`](https://github.com/cambridge-cares/TheWorldAvatar/blob/main/JPS_BASE_LIB/src/main/java/uk/ac/cam/cares/jps/base/agent/DerivationAgent.java) - derivation agent java class that uses methods provided in `DerivationClient`
- [`DerivationAsynExample`](https://github.com/cambridge-cares/TheWorldAvatar/tree/main/Agents/DerivationAsynExample) - example on directed acyclic graph (DAG) of derivations operated by derivation agents

# Installation #
For development and testing reasons, follow below instructions to get a copy of the project up and running on your local system.

## Virtual environment setup

It is highly recommended to install `pyderivationagent` packages in a [virtual environment](https://docs.python.org/3/tutorial/venv.html). The following steps can be taken to build a virtual environment:

`(Windows)`

```cmd
$ python -m venv <venv_name>
$ <venv_name>\Scripts\activate.bat
(<venv_name>) $
```

`(Linux)`
```sh
$ python3 -m venv <venv_name>
$ source <venv_name>/bin/activate
(<venv_name>) $
```

The above commands will create and activate the virtual environment `<venv_name>` in the current directory.

## Installation via pip

The following command can be used to install the `pyderivationagent` package and `agentlogging` package. This is a workaround as PyPI does NOT allow `install_requires` direct links, so we could NOT add package `agentlogging` from `'agentlogging @ git+https://github.com/cambridge-cares/TheWorldAvatar@main#subdirectory=Agents/utils/python-utils'` as dependency, therefore, in order to make the semi-automated release process working, we here introduce a workaround to install agentlogging to the virtual environment but NOT as dependency in the setup.py of `pyderivationagent`. A long term solution could be that we publish `agentlogging` in PyPI as well.

```sh
(<venv_name>) $ pip install pyderivationagent
(<venv_name>) $ pip install "git+https://github.com/cambridge-cares/TheWorldAvatar@main#subdirectory=Agents/utils/python-utils"
```

# How to use #

## Develop derivation agent

When creating a new derivation python agent, it's strongly advised to stick to the folder structure below:

*Recommended python agent folder layout*

    .
    ├── ...                         # other project files (README, LICENSE, etc..)
    ├── youragent                   # project source files for your agent
    │   ├── agent                   # module that contains the agent logic
    │   │   ├── __init__.py
    │   │   └── your_agent.py       # your derivation agent
    │   ├── data_model              # module that contains the dataclasses for concepts
    │   │   ├── __init__.py
    │   │   └── your_onto.py        # dataclasses for your ontology concepts 
    │   ├── kg_operations           # module that handles knowledge graph operations
    │   │   ├── __init__.py
    │   │   └── your_sparql.py      # sparql query and update strings for your agent
    │   ├── other_modules           # other necessary modules for your agent
    │   │   ├── __init__.py
    │   │   ├── module1.py
    │   │   └── module2.py
    │   ├── entry_point.py          # agent entry point
    │   └── __init__.py
    ├── youragent.env.example       # example environment variables for configuration
    ├── docker-compose.yml          # docker compose file
    ├── Dockerfile                  # Dockerfile
    └── tests                       # tests files

*Example code*

`your_agent.py`

```python
from pyderivationagent import DerivationAgent
from pyderivationagent import DerivationInputs
from pyderivationagent import DerivationOutputs
from youragent.kg_operations import YourSparqlClient
from youragent.data_model import YOUR_CONCEPT
from rdflib import Graph

class YourAgent(DerivationAgent):
    def process_request_parameters(self, derivation_inputs: DerivationInputs, derivation_outputs: DerivationOutputs):
        # Provide your agent logic that converts the agent inputs to triples of new created instances
        # The derivation_inputs will be in the format of key-value pairs with the concept as key and instance iri as value
        # For example: 
        # {
        #     "https://www.example.com/triplestore/repository/Ontology.owl#Concept_1": ["https://www.example.com/triplestore/repository/Concept_1/Instance_1"],
        #     "https://www.example.com/triplestore/repository/Ontology.owl#Concept_2": ["https://www.example.com/triplestore/repository/Concept_2/Instance_2"],
        #     "https://www.example.com/triplestore/repository/Ontology.owl#Concept_3":
        #         ["https://www.example.com/triplestore/repository/Concept_3/Instance_3_1", "https://www.example.com/triplestore/repository/Concept_3/Instance_3_2"],
        #     "https://www.example.com/triplestore/repository/Ontology.owl#Concept_4": ["https://www.example.com/triplestore/repository/Concept_4/Instance_4"]
        # }

        # The instance IRIs of the interested class (rdf:type) can be accessed via derivation_inputs.getIris(clz)
        # e.g. assume we will take the first iri that has rdf:type YOUR_CONCEPT
        instance_iri = derivation_inputs.getIris(YOUR_CONCEPT)[0]

        # You may want to create instance of YourSparqlClient for specific queries/updates you would like to perform
        # YourSparqlClient should be defined in your_sparql.py that will be introduced later in this README.md file
        # This client can be initialised with the configuration you already initialised in YourAgent.__init__ method
        self.sparql_client = YourSparqlClient(
            self.kgUrl, self.kgUpdateUrl, self.kgUser, self.kgPassword,
            self.fs_url, self.fs_user, self.fs_password
        )

        # Please note here we are using instance_iri of class YOUR_CONCEPT within the provided derivation_inputs
        response = self.sparql_client.your_sparql_query(instance_iri)

        # You may want to log something during agent execution
        self.logger.info("YourAgent has done something.")
        self.logger.debug("And here are some details...")

        # The new created instances should be added to the derivation_outputs in the form of triples
        # e.g. <http://example/ExampleClass_UUID> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example/ExampleClass>.
        # <http://example/ExampleClass_UUID> <http://example/hasValue> 5.
        derivation_outputs.createNewEntity("http://example/ExampleClass_UUID", "http://example/ExampleClass")
        derivation_outputs.addTriple("http://example/ExampleClass_UUID", "http://example/hasValue", 5)

        # Alternatively, you may create an instance of rdflib.Graph and add the whole graph to derivation_outputs
        # In which case, the above triples can be added by:
        g = Graph()
        g.add((URIRef("http://example/ExampleClass_UUID"), RDF.type, URIRef("http://example/ExampleClass")))
        g.add((URIRef("http://example/ExampleClass_UUID"), URIRef("http://example/hasValue"), Literal(5)))
        derivation_outputs.addGraph(g)
```

`your_onto.py`

```python
from __future__ import annotations # imported to enable pydantic postponed annotations
import pydantic
from typing import Optional

# Module pyderivationagent.data_model.iris.py provides some IRI of basic/common concepts and relationships
# In case you are defining new concepts in your project, you may put it here
YOUR_CONCEPT = 'https://www.example.com/triplestore/repository/Ontology.owl#YOUR_CONCEPT'

# You may also want to define dataclasses for some concepts you are working on
# Please note normally you need to define the concept before it gets referenced by others, as python is a scripting language
class YourOtherConcept(pydantic.BaseModel):
    instance_iri: str

# However, if you have to reference other concepts that will have to appear at a later point in your script, for example, in the situation of self-referencing or circular referencing (both of which are quite common in ontologies), you may do so like:
class AnotherConcept(pydantic.BaseModel):
    instance_iri: str
    refers_to_oneself: AnotherConcept # Here the AnotherConcept self-reference
    refers_to_yourconcept: YourConcept # This object relationship refers to YourConcept which will be defined in next lines
    # For both reference to work, one must call update_forward_refs() after the definition of YourConcept to make it resolvable

class YourConcept(pydantic.BaseModel):
    instance_iri: str
    # Here you may want to declare an object relationship that points to YourOtherConcept
    # However, you may want to make it Optional with a default None
    example_object_relationship_to: Optional[YourOtherConcept] = None
    circular_reference_to: AnotherConcept # Here we complete the circular-reference situation

# Call update_forward_refs() to make AnotherConcept resolvable
# For more details, please see https://pydantic-docs.helpmanual.io/usage/postponed_annotations/
AnotherConcept.update_forward_refs()
```

`your_sparql.py`

```python
from pyderivationagent import PySparqlClient
from youragent.data_model import YourConcept

class YourSparqlClient(PySparqlClient):
    # PySparqlClient class provides a few utility functions that developer can call within their own functions:
    #  - checkInstanceClass(self, instance, instance_class)
    #  - getAmountOfTriples(self)
    #  - performQuery(self, query)
    #  - performUpdate(self, update)
    #  - uploadOntology(self, filePath)
    #  - uploadFile(self, local_file_path)
    #  - downloadFile(self, remote_file_path, downloaded_file_path)

    def your_sparql_query(self, your_instance_iri: str) -> YourConcept:
        # Construct your SPARQL query string
        query = """SELECT ?target WHERE {?target ?predicate ?object.}"""

        # Perform SPARQL query with provided method performQuery in PySparqlClient class
        response = self.performQuery(query)

        # Instantiate TargetClass instances with query results
        new_target_instance = YourConcept(response[0]['target'])

        return new_target_instance

    def your_sparql_update(self, your_instance_iri: str):
        # Construct your SPARQL update string
        update = """INSERT DATA {<http://subject> <http://predicate> <http://object>.}"""

        # Perform SPARQL update with provided method performUpdate in PySparqlClient class
        self.performUpdate(update)
```

`entry_point.py`

```python
from pyderivationagent.conf import config_derivation_agent
from youragent.agent import YourAgent

def create_app():
    # If you would like to deploy your agent within a docker container (using docker-compose.yml and youragent.env which will be introduced later in this README.md file), then you may use:
    agent_config = config_derivation_agent()
    # Else, if you would like to create agent to run in your memory, then you may want to provide the path to youragent.env file as argument to function config_derivation_agent(), i.e.,
    # agent_config = config_derivation_agent("/path/to/youragent.env")

    # Create agent instance
    agent = YourAgent(
        agent_iri = agent_config.ONTOAGENT_SERVICE_IRI,
        time_interval = agent_config.DERIVATION_PERIODIC_TIMESCALE,
        derivation_instance_base_url = agent_config.DERIVATION_INSTANCE_BASE_URL,
        kg_url = agent_config.SPARQL_QUERY_ENDPOINT,
        kg_update_url = agent_config.SPARQL_UPDATE_ENDPOINT,
        kg_user = agent_config.KG_USERNAME,
        kg_password = agent_config.KG_PASSWORD,
        fs_url = agent_config.FILE_SERVER_ENDPOINT,
        fs_user = agent_config.FILE_SERVER_USERNAME,
        fs_password = agent_config.FILE_SERVER_PASSWORD,
        agent_endpoint = agent_config.ONTOAGENT_OPERATION_HTTP_URL,
        app = Flask(__name__)
        flask_config = FlaskConfig(),
        logger_name = "dev"
    )

    # Start listening sync/monitoring async derivations
    agent.start_monitoring_derivations()

    # Expose flask app of agent
    app = agent.app

    return app
```

`youragent.env.example`

```
ONTOAGENT_SERVICE_IRI=http://www.example.com/resources/agents/Service__Example#Service
DERIVATION_PERIODIC_TIMESCALE=60
DERIVATION_INSTANCE_BASE_URL=http://www.example.com/triplestore/repository/
SPARQL_QUERY_ENDPOINT=http://www.example.com/blazegraph/namespace/kb/sparql
SPARQL_UPDATE_ENDPOINT=http://www.example.com/blazegraph/namespace/kb/sparql
KG_USERNAME=
KG_PASSWORD=
FILE_SERVER_ENDPOINT=http://www.example.com/FileServer/
FILE_SERVER_USERNAME=
FILE_SERVER_PASSWORD=
ONTOAGENT_OPERATION_HTTP_URL=/Example
```
You may want to commit this example file without credentials to git as a template for your agent configuration. At deployment, you can make a copy of this file, rename it to `youragent.env` and populate the credentials information. It is suggested to add `*.env` entry to your `.gitignore` of the agent folder, thus the renamed `youragent.env` (including credentials) will NOT be committed to git.

*NOTE: you may want to provide `SPARQL_QUERY_ENDPOINT` and `SPARQL_UPDATE_ENDPOINT` as the internal port of the triple store (most likely blazegraph) docker container, e.g., `http://blazegraph:8080/blazegraph/namespace/kb/sparql` (`blazegraph:8080` depends on your specification in the `docker-compose.yml` which will be introduced later in this README.md file), if you would like to deploy your derivation agent and the triple store within the same docker stack. At deployment, configurations in this file will be picked up by `config_derivation_agent()` when instantiating the agent in `create_app()` of `entry_point.py`.*

`docker-compose.yml`

```yml
version: '3.8'

services:
  # Your derivation agent
  your_agent:
    image: your_agent:1.0.0
    container_name: your_agent
    environment:
      LOG4J_FORMAT_MSG_NO_LOOKUPS: "true"
    build:
      context: .
      dockerfile: ./Dockerfile
    ports:
      - 7000:5000
    env_file:
      - ./youragent.env

  # Blazegraph
  blazegraph:
    image: docker.cmclinnovations.com/blazegraph:1.0.0-SNAPSHOT
    container_name: "blazegraph_test"
    ports:
      - 27149:8080
    environment:
      BLAZEGRAPH_PASSWORD_FILE: /run/secrets/blazegraph_password
    # Add a secret to set the password for BASIC authentication
    secrets:
      - blazegraph_password

# Secrets used to set runtime passwords
secrets:
  blazegraph_password:
    file: tests/dummy_services_secrets/blazegraph_passwd.txt
```

You may refer to [DoEAgent](https://github.com/cambridge-cares/TheWorldAvatar/tree/133-dev-design-of-experiment/Agents/DoEAgent) for a concrete implementation of the above suggested folder structure based on `pyderivationagent`. The design of `pyderivationagent` is continually evolving, and as the project grows, we hope to make it more accessible to developers and users.

## Dockerised integration test
The `pyderivationagent` package also provides two sets of dockerised integration tests, following the same context as [`DerivationAsynExample`](https://github.com/cambridge-cares/TheWorldAvatar/tree/main/Agents/DerivationAsynExample). Interested developer may refer to the README of the Java example for more context, or `TheWorldAvatar/JPS_BASE_LIB/python_derivation_agent/tests` for more technical details.

One may execute below commands for each set of dockerised integration test:

- Agents instantiated and run in memory, operating on a blazegraph docker container with dynamic port decided on deployment

    `(Linux)`
    ```sh
    $ cd /absolute_path_to/TheWorldAvatar/JPS_BASE_LIB/python_derivation_agent
    $ pytest -s ./tests/test_derivation_agent.py
    ```

- All agents and blazegraph deployed within the same docker stack and they communicate via internal port address

    `(Linux)`
    ```sh
    $ cd /absolute_path_to/TheWorldAvatar/JPS_BASE_LIB/python_derivation_agent
    $ pytest -s --docker-compose=./docker-compose.test.yml ./tests/test_docker_integration.py
    ```

Ideally, we would like to provide this set of dockerised integration test to demo how one may develop integration test for derivation agents. Any ideas/discussions/issues/PRs on how to make this more standardised and accessible to developers are more than welcome.

# New features and package release #

Developers who add new features to the python agent wrapper handle the distribution of package `pyderivationagent` on PyPI and Test-PyPI. If you want to add new features that suit your project and release the package independently, i.e. become a developer/maintainer, please contact the repository's administrator to indicate your interest.

The release procedure is currently semi-automated and requires a few items:

- Your Test-PyPI and PyPI account and password
- The version number x.x.x for the release
- Clone of `TheWorldAvatar` repository on your local machine
- Docker-desktop is installed and running on your local machine
- You have access to the docker.cmclinnovations.com registry on your local machine, for more information regarding the registry, see: https://github.com/cambridge-cares/TheWorldAvatar/wiki/Docker%3A-Image-registry

Please create and checkout to a new branch from your feature branch once you are happy with the feature and above details are ready. The release process can then be started by using the commands below, depending on the operating system you're using. (REMEMBER TO CHANGE THE CORRECT VALUES IN THE COMMANDS BELOW!)

WARNING: at the moment, releasing package from Windows OS has an issue that the package will not be installed correctly while executing "$PIPPATH --disable-pip-version-check install -e $SPATH"[dev]"" in install_script_pip.sh. Please use Linux to release future versions before the issue is fixed.

`(Windows)`

```cmd
$ cd \absolute_path_to\TheWorldAvatar\JPS_BASE_LIB\python_derivation_agent
$ release_pyderivationagent_to_pypi.sh -v x.x.x
```

`(Linux)`
```sh
$ cd /absolute_path_to/TheWorldAvatar/JPS_BASE_LIB/python_derivation_agent
$ ./release_pyderivationagent_to_pypi.sh -v x.x.x
```

Please follow the instructions presented in the console once the process has begun. If everything goes well, change the version number in `JPS_BASE_LIB/python_derivation_agent/release_pyderivationagent_to_pypi.sh` to the one you used for the script release.
```sh
echo "./release_pyderivationagent_to_pypi.sh -v 0.0.1   - release version 0.0.1"
```

The changes mentioned above should then be committed with the changes performed automatically during the release process, specifically in python script `JPS_BASE_LIB/python_derivation_agent/pyderivationagent/__init__.py`
```
__version__ = "0.0.1"
```

and `JPS_BASE_LIB/python_derivation_agent/setup.py`
```
version='0.0.1',
```

Finally, merge the release branch back to the feature branch and make a Pull Request for the feature branch to be merged back into the `main` branch.

# Authors #

Jiaru Bai (jb2197@cam.ac.uk)