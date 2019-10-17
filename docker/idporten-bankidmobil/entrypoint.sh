#!/usr/bin/env bash
. /usr/local/webapps/application.conf  # sourcer application.conf slik at RUN_ARGS slik at den kan brukast ved oppstart av app
LOG_ENV=$(cut -d'=' -f2 /log_config)||:  #setter LOG_ENV frå docker-config viss den fins
/usr/share/filebeat/bin/filebeat -c /etc/filebeat/filebeat.yml -path.home /usr/share/filebeat -path.config /etc/filebeat -path.data /var/lib/filebeat -path.logs /var/log/filebeat -E LOG_ENV=$LOG_ENV &   #starter filebeat
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar /usr/local/webapps/application.war $RUN_ARGS
sleep 5000   #Hvis applikasjonen krasjer ved startup kan man legge til sleep for å undersøke kva som gjekk feil