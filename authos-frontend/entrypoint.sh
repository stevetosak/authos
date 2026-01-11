#!/bin/sh
set -e

envsubst < /usr/share/nginx/html/config.template.js \
         > /usr/share/nginx/html/config/config.js

exec nginx -g 'daemon off;'
