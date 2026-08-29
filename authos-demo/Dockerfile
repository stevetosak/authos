# Mirrors authos-frontend: CI runs `npm ci && npm run build` (it needs to resolve
# @authoss/duster-react from npm), this image only serves the result. `config.template.js`
# ships in dist/ via public/; entrypoint.sh renders it to config/config.js at container start.
# Build context is this directory.
FROM nginx:1.29.4-alpine-slim
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY ./dist /usr/share/nginx/html
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh && mkdir -p /usr/share/nginx/html/config
ENTRYPOINT ["/entrypoint.sh"]
