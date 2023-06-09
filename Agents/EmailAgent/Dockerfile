#########################
#
# This docker file creates an Image with an environment
# for running the EmailAgent within a Tomcat server.
# 
# The "docker build" command used to build this file
# into a Image should be run from the EmailAgent directory.
# See the README for more details.
#
#########################

# First stage builds the WAR file
FROM maven:3.8.5-openjdk-17-slim as build

# Copy all files into root's home directory
ADD . /root

# Populate settings templates with credentials, repo name
WORKDIR /root/.m2

RUN sed -i "s|MASTER_PASSWORD|$(mvn --encrypt-master-password master_password)|" settings-security.xml
RUN sed -i "s|REPO_USERNAME|$(cat ../credentials/repo_username.txt)|;s|REPO_PASSWORD|$(cat ../credentials/repo_password.txt|xargs mvn --encrypt-password)|" settings.xml

# Build the project
WORKDIR /root/code
RUN mvn package


# Second stage grabs the WAR and runs Tomcat
FROM tomcat:9.0.69-jdk17 as final
WORKDIR /app

# Copy the built WAR file
# Note here that if the WAR version changes, you'll need to update this line
COPY --from=build /root/code/output/email_agent##1.0.1.war $CATALINA_HOME/webapps/

# Expose port 8080
EXPOSE 8080

# Start the Tomcat server
ENTRYPOINT ["catalina.sh", "run"]