# Use Node.js for building the React app
FROM node:20-alpine AS build
# Set working directory
WORKDIR /usr/src/app
# Copy package.json and package-lock.json
COPY package.json package-lock.json ./
# Install dependencies
RUN npm install
# Copy the rest of the application
COPY . .
# Build the React app
RUN npm run build

# Use Nginx to serve the static files
FROM nginx:1.25-alpine
# Копируем собственный nginx.conf в контейнер
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Копируем файлы сборки React
COPY --from=build /usr/src/app/build /usr/share/nginx/html
# Открываем порт 80
EXPOSE 80
# Запуск Nginx
CMD ["nginx", "-g", "daemon off;"]
