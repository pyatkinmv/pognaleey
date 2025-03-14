# Pognaleey App

## Build and Run Instructions

### Locally

1. Add the file `pognaleey-back/src/main/resources/application-dev.yaml` with the following content:

   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/pognaleey
   cors:
     allowed-origins: http://localhost:3000
   ```

2. Create a `.env` file in the root directory of the project and specify the following values:

   ```properties
   POSTGRES_USER=
   POSTGRES_PASSWORD=
   POSTGRES_DB=
   CHAT_GPT_API_KEY=
   JWT_SECRET=
   DOMAIN=
   REACT_APP_API_URL=
   KANDINSKY_API_KEY=
   KANDINSKY_API_SECRET=
   ```
3. Set up the run configuration for `pognaleey-back` in IntelliJ IDEA:
   - Go to `Edit Configurations` -> Add new configuration -> `Spring Boot`
   - Set `Spring Boot Class` to `ru.pyatkinmv.pognaleey.PognaleeyApp`
   - Set `Environment variables` to the absolute path of `[your-project-path]/pognaleey/.env`

4. Set up the run configuration for `pognaleey-front` in IntelliJ IDEA:
   - Go to `Edit Configurations` -> Add new configuration -> `npm`
   - Set `package.json` to `[your-project-path]/pognaleey/pognaleey-front/package.json`
   - Set the command to `start`

5. Run the following command to start the database:
   ```bash
   docker-compose up dbpostgresql --build
   ```

6. Launch `pognaleey-back` from IntelliJ IDEA.

7. Launch `pognaleey-front` from IntelliJ IDEA.

The application will be available at:  
`http://localhost:3000/`

---

### **On a Server**

1. **Clone the repository:**
   ```bash
   git clone https://github.com/pyatkinmv/pognaleey.git && cd pognaleey
   ```

2. **Obtain SSL Certificates using Certbot:**
   ```bash
   sudo apt update && sudo apt install certbot
   sudo docker-compose down  # Stop Nginx to free port 80
   sudo certbot certonly --standalone -d your-host-name --email your-email@example.com --agree-tos --non-interactive
   ```

3. **Copy SSL certificates to `./certs/` and create symlinks:**
   ```bash
   sudo mkdir -p certs/live/roamleey.com certs/archive/roamleey.com
   sudo cp -R /etc/letsencrypt/archive/your-host-name/ certs/archive/
   cd certs/live/your-host-name/
   sudo ln -s ../../archive/your-host-name/cert1.pem cert.pem
   sudo ln -s ../../archive/your-host-name/chain1.pem chain.pem
   sudo ln -s ../../archive/your-host-name/fullchain1.pem fullchain.pem
   sudo ln -s ../../archive/your-host-name/privkey1.pem privkey.pem
   ```

4. **Add a `.env` file in the project root (same as local setup).**

5. **Build and launch the application:**
   ```bash
   sudo docker-compose up -d --build
   ```

6. **Verify HTTPS is working:**
   ```bash
   curl -I https://your-host-name
   ```

#### **🚀 Now your server is running with HTTPS enabled!**