FROM nginx:1.29.4-alpine-slim
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY ./dist /usr/share/nginx/html
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
RUN mkdir -p /usr/share/nginx/html/config
ENTRYPOINT ["/entrypoint.sh"]