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

# Pass the environment variable to React at build time
ARG REACT_APP_API_URL
ENV REACT_APP_API_URL=${REACT_APP_API_URL}
ARG REACT_APP_NAME
RUN sed -i "s|%%REACT_APP_NAME%%|${REACT_APP_NAME}|g" public/index.html

# Build the React app
RUN npm run build

# Copy build files to mounted volume
CMD ["cp", "-r", "/usr/src/app/build", "/mnt"]
