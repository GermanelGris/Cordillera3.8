# Build
FROM node:20-alpine AS build
WORKDIR /app

# Copiar package files
COPY package*.json ./

# Instalar dependencias (incluye dev)
RUN npm ci --legacy-peer-deps

# Copiar código fuente
COPY . .

# Construir
RUN npm run build

# Serve with Nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]