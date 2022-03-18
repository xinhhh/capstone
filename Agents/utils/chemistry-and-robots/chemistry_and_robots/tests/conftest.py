from testcontainers.core.container import DockerContainer
from pathlib import Path
from enum import Enum
import logging
import pkgutil
import pytest
import time
import uuid
import os

logging.getLogger("py4j").setLevel(logging.INFO)
logger = logging.getLogger('chemistry_and_robots_tests')

import requests
from requests import status_codes

from chemistry_and_robots.kg_operations.sparql_client import ChemistryAndRobotsSparqlClient

THIS_DIR = os.path.dirname(os.path.abspath(__file__))
RESOURCE_DIR = str(Path(__file__).absolute().parent.parent)+'/resources/'
SECRETS_PATH = os.path.join(THIS_DIR,'dummy_services_secrets')
SECRETS_FILE_PATH = os.path.join(THIS_DIR,'dummy_services_secrets', 'dummy_test_auth')
URL_FILE_PATH = os.path.join(THIS_DIR,'dummy_services_secrets', 'dummy_test_url')

KG_SERVICE = "blazegraph"
KG_ROUTE = "blazegraph/namespace/kb/sparql"
FS_SERVICE = "fileserver"
FS_ROUTE = "FileServer/"

def pytest_sessionstart(session):
    """ This will run before all the tests"""
    if os.path.exists(SECRETS_FILE_PATH):
        os.remove(SECRETS_FILE_PATH)
    if os.path.exists(URL_FILE_PATH):
        os.remove(URL_FILE_PATH)

def pytest_sessionfinish(session):
    """ This will run after all the tests"""
    if os.path.exists(SECRETS_FILE_PATH):
        os.remove(SECRETS_FILE_PATH)
    if os.path.exists(URL_FILE_PATH):
        os.remove(URL_FILE_PATH)

# ----------------------------------------------------------------------------------
# Session-scoped test fixtures
# ----------------------------------------------------------------------------------

@pytest.fixture(scope="session")
def get_service_url(session_scoped_container_getter):
    def _get_service_url(service_name, url_route):
        service = session_scoped_container_getter.get(service_name).network_info[0]
        service_url = f"http://localhost:{service.host_port}/{url_route}"
        return service_url

    # this will run only once per entire test session and ensures that all the services
    # in docker containers are ready. Increase the sleep value in case services need a bit
    # more time to run on your machine.
    time.sleep(8)
    return _get_service_url

@pytest.fixture(scope="session")
def get_service_auth():
    def _get_service_auth(service_name):
        password_file = os.path.join(SECRETS_PATH,service_name+'_passwd.txt')
        user_file = os.path.join(SECRETS_PATH,service_name+'_user.txt')

        # read service auth from files
        username = ''
        password = ''
        if os.path.exists(user_file):
            with open(user_file) as f:
                username = f.read().strip()
        if os.path.exists(password_file):
            with open(password_file) as f:
                password = f.read().strip()

        return username, password

    return _get_service_auth

@pytest.fixture(scope="session")
def initialise_triples(get_service_url, get_service_auth):
    # Create SparqlClient for testing
    sparql_endpoint = get_service_url(KG_SERVICE, url_route=KG_ROUTE)
    sparql_client = ChemistryAndRobotsSparqlClient(sparql_endpoint, sparql_endpoint)

    # Upload the example triples for testing
    pathlist = Path(RESOURCE_DIR).glob('**/*.ttl')
    for path in pathlist:
        sparql_client.uploadOntology(str(path))

    yield sparql_client

    # Clear logger at the end of the test
    clear_loggers()

@pytest.fixture(scope="session")
def initialise_files(get_service_url, get_service_auth):
    # Retrieve fs_url and auth
    fs_url = get_service_url(FS_SERVICE, url_route=FS_ROUTE)
    fs_user, fs_pwd = get_service_auth(FS_SERVICE)
    fs_auth = (fs_user, fs_pwd)

    yield fs_url, fs_auth

    # Clear logger at the end of the test
    clear_loggers()

# method adopted from https://github.com/pytest-dev/pytest/issues/5502#issuecomment-647157873
def clear_loggers():
    """Remove handlers from all loggers"""
    import logging
    loggers = [logging.getLogger()] + list(logging.Logger.manager.loggerDict.values())
    for logger in loggers:
        handlers = getattr(logger, 'handlers', [])
        for handler in handlers:
            logger.removeHandler(handler)
