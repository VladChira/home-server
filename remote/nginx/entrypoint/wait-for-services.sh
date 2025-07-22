#!/bin/sh
echo "Waiting for Jellyfin to come up..."
until nc -z jellyfin 8096; do
    sleep 20
    echo "Still waiting for Jellyfin..."
done
echo "Jellyfin is up."

#echo "Waiting for Immich to come up..."
#until nc -z immich-server 2283; do
#    sleep 20
#    echo "Still waiting for Immich Server..."
#done
#echo "Immich is up"

#echo "Starting Nginx"
exec nginx -g "daemon off;"
