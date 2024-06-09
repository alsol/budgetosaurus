FROM eclipse-temurin:21-jdk
COPY ./python python
RUN apt-get update  \
    && apt-get install -y python3.10-venv  \
    && python3 -m venv ./pyenv  \
    && ./pyenv/bin/pip3 install -r ./python/requirements.txt