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

# Используем базовый образ для минимального размера
FROM alpine:3.18

# Копируем собранное приложение
COPY --from=build /usr/src/app/build /app

# Устанавливаем минимальный веб-сервер для тестов (если нужно, например, serve)
RUN apk add --no-cache nodejs npm && npm install -g serve

# Открываем порт (для локального тестирования)
EXPOSE 3000

# Запускаем фронтенд (для тестов — глобальный Nginx раздает эти файлы в проде)
CMD ["serve", "-s", "/app"]