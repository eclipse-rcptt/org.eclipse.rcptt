# syntax=docker/dockerfile:1
FROM eclipse-temurin:21

ENV DEBIAN_FRONTEND=noninteractive

ARG TZ=UTC

RUN echo "UTC" > /etc/timezone
RUN <<EOT debconf-set-selections
tzdata tzdata/Areas select Etc
tzdata tzdata/Zones/Etc select UTC
locales locales/locales_to_be_generated multiselect en_US.UTF-8 UTF-8
locales locales/default_environment_locale select en_US.UTF-8
EOT

COPY custom.apt.conf /etc/apt/apt.conf.d/custom

RUN apt-get update \
 && apt-get install \
      libwebkit2gtk-4.1 \
	  ca-certificates \
	  gnupg2 \
	  tzdata \
	  language-pack-en \
	  locales \
	  tightvncserver\
	  dwm \
	  xfonts-base \
	  xfonts-cyrillic \
	  xfonts-scalable \
	  fonts-liberation \
	  fonts-ipafont-gothic \
	  fonts-wqy-zenhei \
	  fonts-tlwg-loma-otf \
	  wget \
    && rm -rf /var/lib/apt/lists/*
	

## Install a maven
ARG MAVEN_VERSION=3.9.9
ARG BASE_URL=https://archive.apache.org/dist/maven/maven-3
ENV MAVEN_HOME=/usr/share/maven

RUN wget --no-verbose --output-document=/tmp/apache-maven.tar.gz ${BASE_URL}/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  && mkdir ${MAVEN_HOME} \
  && tar -xzf /tmp/apache-maven.tar.gz -C ${MAVEN_HOME} --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s ${MAVEN_HOME}/bin/mvn /usr/bin/mvn
