# Base image is a lightweight version of Python
FROM python:3.8-slim-buster

# Set the default environmental variables
ENV KG_PROTOCOL="https"
ENV OUTPUT_DIR="../test_data_output"
ENV SKIP_BUILDING="false"

# This line is for testing purpose will be removed in production version
ENV MAX_NUM_BUILDINGS="None"

# Set the working directory
WORKDIR /app

# Install the required Python libraries
COPY requirements.txt requirements.txt
RUN pip3 install -r requirements.txt

# Copy the required Python script into the image
COPY utilities/__init__.py utilities/__init__.py
COPY utilities/geojson_creator.py utilities/geojson_creator.py
COPY utilities/SparqlErrors.py utilities/SparqlErrors.py
COPY utilities/utils_argo.py utilities/utils_argo.py
COPY kingslynnagent/__init__.py kingslynnagent/__init__.py
COPY kingslynnagent/query_buildings_open_data_dockerised.py kingslynnagent/query_buildings_open_data_dockerised.py

# Run the script
CMD [ "python3", "-m", "kingslynnagent.query_buildings_open_data_dockerised"]
