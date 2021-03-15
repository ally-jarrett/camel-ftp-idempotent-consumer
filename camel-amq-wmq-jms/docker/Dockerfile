FROM ibmcom/mq
USER root
RUN useradd redhat -G mqm && \
    echo redhat:passw0rd | chpasswd
COPY 20-config.mqsc /etc/mqm/
