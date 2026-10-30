FROM maven:3.9.9-eclipse-temurin-17 AS buildtools
FROM docker:28.0.4-cli AS dockercli
FROM hashicorp/terraform:1.17.1 AS terraformtools
FROM jenkins/jenkins:lts-jdk21
USER root
COPY --from=buildtools /opt/java/openjdk /opt/java17
COPY --from=buildtools /usr/share/maven /opt/maven
COPY --from=dockercli /usr/local/bin/docker /usr/local/bin/docker
COPY --from=dockercli /usr/local/libexec/docker/cli-plugins/docker-buildx /usr/local/libexec/docker/cli-plugins/docker-buildx
COPY --from=terraformtools /bin/terraform /usr/local/bin/terraform
ENV MAVEN_HOME=/opt/maven
ENV PATH=/opt/maven/bin:$PATH
USER jenkins
RUN git config --global --add safe.directory /git-src
RUN jenkins-plugin-cli --plugins workflow-aggregator git credentials-binding junit sonar pipeline-stage-view
COPY --chown=jenkins:jenkins bootstrap.groovy /usr/share/jenkins/ref/init.groovy.d/bootstrap.groovy
USER root
ADD --chmod=0755 --checksum=sha256:29818b6729dc9262b692307470d0558d027725e738be5dd47c760261868a2634 https://dl.k8s.io/release/v1.36.6/bin/linux/amd64/kubectl /usr/local/bin/kubectl
USER jenkins
