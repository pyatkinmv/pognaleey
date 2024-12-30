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