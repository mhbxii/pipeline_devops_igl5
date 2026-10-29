FROM maven:3.9-eclipse-temurin-17 AS tools
FROM docker:28-cli AS dockercli
FROM hashicorp/terraform:1.13.5 AS terraform
FROM jenkins/jenkins:lts-jdk21
USER root
COPY --from=tools /usr/share/maven /opt/maven
COPY --from=tools /opt/java/openjdk /opt/jdk17
COPY --from=dockercli /usr/local/bin/docker /usr/local/bin/docker
COPY --from=dockercli /usr/local/libexec/docker/cli-plugins /usr/local/libexec/docker/cli-plugins
COPY --from=terraform /bin/terraform /usr/local/bin/terraform
RUN apt-get update && apt-get install -y --no-install-recommends curl ca-certificates python3 && \
    rm -rf /var/lib/apt/lists/* && \
    curl -fsSLo /usr/local/bin/kubectl https://dl.k8s.io/release/v1.34.1/bin/linux/amd64/kubectl && \
    curl -fsSLo /tmp/kubectl.sha256 https://dl.k8s.io/release/v1.34.1/bin/linux/amd64/kubectl.sha256 && \
    echo "$(cat /tmp/kubectl.sha256)  /usr/local/bin/kubectl" | sha256sum -c - && \
    chmod +x /usr/local/bin/kubectl && rm /tmp/kubectl.sha256
ENV PATH="/opt/maven/bin:${PATH}"
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt
COPY init.groovy /usr/share/jenkins/ref/init.groovy.d/create-job.groovy
USER jenkins
