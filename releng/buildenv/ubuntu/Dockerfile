FROM consol/ubuntu-xfce-vnc:latest

# Switch to root user to install additional software
USER 0

COPY openjdk-r-ubuntu-ppa-xenial.list /etc/apt/sources.list.d/
COPY openjdk-r_ubuntu_ppa.gpg /etc/apt/trusted.gpg.d/
## Install a java 11, a webkit and a javafx
RUN apt-get -y update \
 && apt-get install -y \
      openjdk-17-jdk \
      libwebkit2gtk-4.0 \
      openjfx \
    && apt-get clean

## Install a maven
ARG MAVEN_VERSION=3.9.9
ARG BASE_URL=https://archive.apache.org/dist/maven/maven-3/
ARG MAVEN_HOME=/usr/share/maven

RUN wget -O /tmp/apache-maven.tar.gz ${BASE_URL}/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  && mkdir ${MAVEN_HOME} \
  && tar -xzf /tmp/apache-maven.tar.gz -C ${MAVEN_HOME} --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s ${MAVEN_HOME}/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME=${MAVEN_HOME}

## switch back to default user
USER 1000
