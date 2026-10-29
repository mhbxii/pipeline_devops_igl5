FROM maven:3.9.9-eclipse-temurin-17 AS buildtools
FROM docker:28.0.4-cli AS dockercli
FROM kindest/node:v1.32.2 AS kubetools
FROM jenkins/jenkins:lts-jdk21
USER root
COPY --from=buildtools /opt/java/openjdk /opt/java17
COPY --from=buildtools /usr/share/maven /opt/maven
COPY --from=dockercli /usr/local/bin/docker /usr/local/bin/docker
COPY --from=dockercli /usr/local/libexec/docker/cli-plugins/docker-buildx /usr/local/libexec/docker/cli-plugins/docker-buildx
COPY --from=kubetools /usr/bin/kubectl /usr/local/bin/kubectl
ENV MAVEN_HOME=/opt/maven
ENV PATH=/opt/maven/bin:$PATH
USER jenkins
RUN git config --global --add safe.directory /git-src
RUN jenkins-plugin-cli --plugins workflow-aggregator git credentials-binding junit sonar pipeline-stage-view
COPY --chown=jenkins:jenkins bootstrap.groovy /usr/share/jenkins/ref/init.groovy.d/bootstrap.groovy
USER root
ADD --chmod=0755 --checksum=sha256:aee6151561422756b764a4ae28e7f44cda5af5a9eead3cc9985112b1de8d8e0d https://kind.sigs.k8s.io/dl/v0.33.0/kind-linux-amd64 /usr/local/bin/kind
USER jenkins
