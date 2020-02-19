FROM openjdk:11

COPY build/install/snailmail /opt/snailmail

EXPOSE 9999

VOLUME ["/opt/snailmail/data"]

CMD ["/opt/snailmail/bin/snailmail"]
