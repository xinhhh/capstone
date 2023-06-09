# First stage: build war file
#==================================================================================================
FROM maven:3.6-openjdk-8-slim as builder

# Copy Maven settings templates and credentials
COPY /credentials /root/credentials
COPY /.m2 /root/.m2

# Populate settings templates with credentials, repo name
WORKDIR /root/.m2
# (Note that | rather than / is used as the sed delimiter, since encrypted passwords can contain the former, but not the latter
RUN sed -i "s|MASTER_PASSWORD|$(mvn --encrypt-master-password master_password)|" settings-security.xml
RUN sed -i "s|REPO_USERNAME|$(cat ../credentials/repo_username.txt)|;s|REPO_PASSWORD|$(cat ../credentials/repo_password.txt|xargs mvn --encrypt-password)|" settings.xml

WORKDIR /root/HeatEmissionAgentGeoJson

# Copy the pom.xml file
COPY /HeatEmissionAgentGeoJson/pom.xml ./pom.xml
COPY /HeatEmissionAgentGeoJson/WEB-INF ./WEB-INF
# Retrieve all of the dependencies
RUN --mount=type=cache,id=agent-mvn,target=/root/.m2/repository,sharing=locked mvn clean dependency:resolve

# Copy the code
COPY /HeatEmissionAgentGeoJson/src ./src

# Build
RUN --mount=type=cache,id=agent-mvn,target=/root/.m2/repository,sharing=locked mvn package

#==================================================================================================

# Second stage: copy the downloaded dependency into a new image and build into an app
#==================================================================================================
FROM tomcat:8.5 as agent

WORKDIR /app

COPY --from=builder /root/HeatEmissionAgentGeoJson/output/heatemissionagentgeojson##1.0.0.war $CATALINA_HOME/webapps/

# Start the Tomcat server
ENTRYPOINT ["catalina.sh", "run"]
#==================================================================================================