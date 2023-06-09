# First stage: build war file
#==================================================================================================
FROM maven:3.8.6-eclipse-temurin-11-focal as builder

# Copy all files into root's home, including the source, pom file, ./m2 directory, and credentials files
ADD /HistoricalPumpDataInstantiationAgent /root/HistoricalPumpDataInstantiationAgent
ADD /credentials /root/credentials
ADD /.m2 /root/.m2

# Populate settings templates with credentials, repo name
WORKDIR /root/.m2
# (Note that | rather than / is used as the sed delimiter, since encrypted passwords can contain the latter, but not the former
RUN sed -i "s|MASTER_PASSWORD|$(mvn --encrypt-master-password master_password)|" settings-security.xml
RUN sed -i "s|REPO_USERNAME|$(cat ../credentials/repo_username.txt)|;s|REPO_PASSWORD|$(cat ../credentials/repo_password.txt|xargs mvn --encrypt-password)|" settings.xml

# Build
WORKDIR /root/HistoricalPumpDataInstantiationAgent
RUN --mount=type=cache,target=/root/.m2/repository mvn clean package

#==================================================================================================

# Second stage: copy the downloaded dependency into a new image and build into an app
#==================================================================================================
FROM tomcat:9.0 as agent

COPY --from=builder /root/HistoricalPumpDataInstantiationAgent/output/historical-pump-data-instantiation-agent##1.0.0.war $CATALINA_HOME/webapps/

# Start the Tomcat server
ENTRYPOINT ["catalina.sh", "run"]
#==================================================================================================