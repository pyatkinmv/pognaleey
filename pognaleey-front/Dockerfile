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

# Копируем файлы сборки в указанную папку (на хосте она будет замонтирована)
#CMD ["cp", "-r", "/usr/src/app/build", "/usr/src/app/mnt"]
CMD ["cp", "-r", "/usr/src/app/build", "/mnt"]
