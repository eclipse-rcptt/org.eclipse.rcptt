# syntax=docker/dockerfile:1
# This Dockerfile always tracks the *latest* supported build environment.
# It is published manually as a new basilevs/ubuntu-rcptt:<version> tag; the
# stable CI lanes stay pinned to an older, proven tag while the test_latest
# lane uses the newest one. See README.md for the versioning/publishing process.
FROM maven:3.9-eclipse-temurin-26

ENV DEBIAN_FRONTEND=noninteractive

ARG TZ=UTC

RUN echo "UTC" > /etc/timezone
RUN <<EOT debconf-set-selections
tzdata tzdata/Areas select Etc
tzdata tzdata/Zones/Etc select UTC
locales locales/locales_to_be_generated multiselect en_US.UTF-8 UTF-8
locales locales/default_environment_locale select en_US.UTF-8
EOT

COPY files/ /

RUN apt-get update \
 && apt-get install \
    libwebkit2gtk-4.1 \
    ca-certificates \
    gnupg2 \
    tzdata \
    language-pack-en \
    locales \
    tigervnc-standalone-server \
    tigervnc-tools \
    dwm \
    xfonts-base \
    xfonts-cyrillic \
    xfonts-scalable \
    fonts-liberation \
    fonts-ipafont-gothic \
    fonts-wqy-zenhei \
    fonts-tlwg-loma-otf \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

CMD ["jshell"]
