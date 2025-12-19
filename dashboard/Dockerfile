FROM node:25-alpine

# Set working directory
WORKDIR /app

# Copy dependency definitions
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy all local files into the image
COPY . .

# Build the Next.js app for production
RUN npm run build

EXPOSE 3000

CMD ["npm", "start"]