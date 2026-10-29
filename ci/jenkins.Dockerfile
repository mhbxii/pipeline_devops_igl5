FROM eclipse-temurin:17-jdk-jammy AS java17
FROM jenkins/jenkins:lts-jdk21
USER root
COPY --from=java17 /opt/java/openjdk /opt/java17
RUN apt-get update && apt-get install -y --no-install-recommends maven docker.io curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*
ARG KUBECTL_VERSION=v1.32.2
RUN curl -fsSLo /usr/local/bin/kubectl https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/amd64/kubectl \
    && curl -fsSLo /tmp/kubectl.sha256 https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/amd64/kubectl.sha256 \
    && echo "$(cat /tmp/kubectl.sha256)  /usr/local/bin/kubectl" | sha256sum -c - \
    && chmod +x /usr/local/bin/kubectl
USER jenkins
RUN jenkins-plugin-cli --plugins workflow-aggregator git credentials-binding junit sonar pipeline-stage-view
