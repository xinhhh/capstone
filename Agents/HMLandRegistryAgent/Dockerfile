# Install the HM Land Registry Agent in Docker container
#==================================================================================================

# Reference published Docker image for Stack-Client resources to use
FROM docker.cmclinnovations.com/stack-client:1.6.2 as stackclients

#------------------------------------------------------
# Base image to be reused
#------------------------------------------------------
FROM python:3.9.14-slim-buster as base
# Meta data
LABEL authors = "mh807@cam.ac.uk"
LABEL description = "HM Land Registry (Property Sales) Instantiation Agent"

# Keeps Python from generating .pyc files in the container
ENV PYTHONDONTWRITEBYTECODE=1
# Turns off buffering for easier container logging
ENV PYTHONUNBUFFERED=1

# Install Java
RUN apt update && apt install -y openjdk-11-jdk-headless git

# directory structure matches requirements for jpsrm to update py4jps resources)
RUN mkdir -p ./tmp_stack
COPY --from=stackclients /app ./tmp_stack

# Set the default working directory & copy files to install Python
WORKDIR /app
COPY ./README.md .
COPY ./setup.py .
# Add JDBC driver to allow for retrieval of JDBC connection properties from PostGIS client
ADD https://jdbc.postgresql.org/download/postgresql-42.5.0.jar ./resources/

# Install Python package and the required libraries
RUN python -m pip install --upgrade pip
RUN pip install -e .

# Add StackClients py4jps resources
RUN stack_clients_jar=$(find ../tmp_stack/stack-clients*.jar) && stack_clients_jar=${stack_clients_jar##*/} && \
    jpsrm install StackClients ../tmp_stack --jar $stack_clients_jar

# Expose port
EXPOSE 5000

#------------------------------------------------------
# Debugging image reusing the base
#------------------------------------------------------
FROM base as debug
# Install additional dependencies for debugging
RUN pip install debugpy

# Set flask app and development environment to allow for hot-reloading
ENV FLASK_APP=app/agent/flaskapp/wsgi.py
ENV FLASK_DEBUG=1

# Launch application in container
WORKDIR /app/agent/flaskapp/
CMD python -m debugpy --listen 0.0.0.0:5678 --wait-for-client -m flask run -h 0.0.0.0 -p 5000


#------------------------------------------------------
# Production image reusing the base
#------------------------------------------------------
FROM base as prod
# Install additional dependencies
RUN pip install gunicorn==20.1.0

# Copy required source code (as code is not attached as volume)
WORKDIR /app
COPY ./agent ./agent
COPY ./app_entry_point.sh .

# Set the entrypoint
ENTRYPOINT ./app_entry_point.sh
#==================================================================================================