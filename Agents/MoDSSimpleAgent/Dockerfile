# First stage: build war file
#==================================================================================================
FROM maven:3.6-openjdk-11-slim as builder

# Copy all files into root's home, including the source, pom file, ./m2 directory and credentials
ADD . /root

# Populate settings templates with credentials, repo name
WORKDIR /root/.m2
# (Note that | rather than / is used as the sed delimiter, since encrypted passwords can contain the former, but not the latter
RUN sed -i "s|MASTER_PASSWORD|$(mvn --encrypt-master-password master_password)|" settings-security.xml
RUN sed -i "s|WA_REPO_USERNAME|$(cat ../credentials/wa_repo_username.txt)|;s|WA_REPO_PASSWORD|$(cat ../credentials/wa_repo_password.txt|xargs mvn --encrypt-password)|;s|CMCL_REPO_USERNAME|$(cat ../credentials/cmcl_repo_username.txt)|;s|CMCL_REPO_PASSWORD|$(cat ../credentials/cmcl_repo_password.txt|xargs mvn --encrypt-password)|" settings.xml

# Build
WORKDIR /root/mods-simple-agent
RUN --mount=type=cache,target=/root/.m2/repository mvn package
#==================================================================================================

# Second stage: copy the downloaded dependency into a new image and build into an app
#==================================================================================================
FROM python:3.6.15-buster

ARG MODS_BIN_DIR="/usr/local/mods-simple-agent/bin"
ARG MODS_SAVE_DIR="/usr/local/mods-simple-agent/savedsurrogates"

ARG USER="user"
RUN useradd -s /bin/bash ${USER}

RUN apt update && apt install openjdk-11-jre mpich rsync -y && \
    mkdir -p "${MODS_BIN_DIR}" && \
    mkdir -p "${MODS_SAVE_DIR}" && \
    chown user:user "${MODS_BIN_DIR}" && \
    chown user:user "${MODS_SAVE_DIR}" && \
    ln -s /usr/lib/x86_64-linux-gnu/libmpi.so "${MODS_BIN_DIR}/libmpi.so.12" && \
    ln -s /usr/lib/x86_64-linux-gnu/libmpifort.so "${MODS_BIN_DIR}/libmpifort.so.12"&& \
    ln -s /usr/local/lib/libpython3.6m.so.1.0 "${MODS_BIN_DIR}/libpython3.6m.so.1.0"
RUN apt remove *python* -y

COPY --chown=user:user mods-simple-agent/src/python/requirements.txt "${MODS_BIN_DIR}"

USER ${USER}
WORKDIR /usr/local/mods-simple-agent
COPY --chown=user:user --from=builder /root/mods-simple-agent/target/*.jar app.jar
COPY --chown=user:user --from=builder /root/mods-simple-agent/target/bin "${MODS_BIN_DIR}"

ENV MODS_BIN_DIR="${MODS_BIN_DIR}"
ARG MODS_VENV_PATH="${MODS_BIN_DIR}/env"
ENV MODS_VENV_PATH="${MODS_VENV_PATH}"

RUN cd "${MODS_BIN_DIR}" && python3 -m venv env --copies --clear && . env/bin/activate && pip3 install -r requirements.txt

COPY --chown=user:user mods-simple-agent/src/python/*.py "${MODS_VENV_PATH}"

RUN rsync -av --exclude 'site-packages' --exclude '*/test*' --exclude '__pycache__' "/usr/local/lib/python3.6" "${MODS_VENV_PATH}/lib"

ENV LD_LIBRARY_PATH="${MODS_BIN_DIR}:${MODS_VENV_PATH}:${LD_LIBRARY_PATH}"
ENV PATH="${MODS_BIN_DIR}:${PATH}"

ARG CMCL_LICENSE_PATH=6200@host.docker.internal
ENV CMCL_LICENSE_PATH="${CMCL_LICENSE_PATH}"
ENV JAVA_OPTS=${JAVA_OPTS}

COPY ./entrypoint.sh ./entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]