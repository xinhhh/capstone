# First stage: build war file
#==================================================================================================
FROM maven:3.6-openjdk-11-slim as builder

# Copy all files into root's home, including the source, pom file, ./m2 directory, credentials and config files
ADD . /root

# Populate settings templates with credentials, repo name
WORKDIR /root/.m2
# (Note that | rather than / is used as the sed delimiter, since encrypted passwords can contain the latter, but not the former
RUN sed -i "s|MASTER_PASSWORD|$(mvn --encrypt-master-password master_password)|" settings-security.xml
RUN sed -i "s|REPO_USERNAME|$(cat ../credentials/repo_username.txt)|;s|REPO_PASSWORD|$(cat ../credentials/repo_password.txt|xargs mvn --encrypt-password)|" settings.xml

# Build
WORKDIR /root/DistrictHeatingAgent/
RUN --mount=type=cache,target=/root/.m2/repository mvn package
# RUN mvn package


#==================================================================================================

# Second stage: copy the downloaded dependency into a new image and build into an app
#==================================================================================================
FROM tomcat:9.0 as agent

WORKDIR /app

# Copies all files in config directory to destination
COPY ./config/. /root/
COPY ./DistrictHeatingAgent/src/test/resources/. /root/

# Set the required environment variables
ENV CSVPATH="/root/StaticDataHeatingNetwork.csv"

# Set the required environment variable
ENV TSPATHTEST="/root/TSDataHeatingNetworkTest.csv"

# Set the required environment variable
ENV CLIENTPROPERTY="/root/client.properties"

# Set the required environment variable
ENV MAPPINGSPATH="/root/mappings"

COPY --from=builder /root/DistrictHeatingAgent/output/district-heating-agent##1.0.0.war $CATALINA_HOME/webapps/

# Start the Tomcat server
ENTRYPOINT ["catalina.sh", "run"]
#==================================================================================================
