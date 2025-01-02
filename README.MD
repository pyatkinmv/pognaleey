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
   GPT_FOLDER_ID=
   GPT_API_KEY=
   IMAGE_SEARCH_API_KEY=
   IMAGE_SEARCH_FOLDER_ID=
   ```

3. Add a `.env` file in the `pognaleey-front/` directory with the following content:

   ```properties
   REACT_APP_API_URL=http://localhost:8080/api/v1
   ```

4. Set up the run configuration for `pognaleey-back` in IntelliJ IDEA:
   - Go to `Edit Configurations` -> Add new configuration -> `Spring Boot`
   - Set `Spring Boot Class` to `ru.pyatkinmv.pognaleey.PognaleeyApp`
   - Set `Environment variables` to the absolute path of `[your-project-path]/pognaleey/.env`

5. Set up the run configuration for `pognaleey-front` in IntelliJ IDEA:
   - Go to `Edit Configurations` -> Add new configuration -> `npm`
   - Set `package.json` to `[your-project-path]/pognaleey/pognaleey-front/package.json`
   - Set the command to `start`

6. Run the following command to start the database:
   ```bash
   docker-compose up dbpostgresql --build
   ```

7. Launch `pognaleey-back` from IntelliJ IDEA.

8. Launch `pognaleey-front` from IntelliJ IDEA.

The application will be available at:  
`http://localhost:3000/`

---

### On a Server

1. Clone the [repository](https://github.com/pyatkinmv/pognaleey).

2. Configure SSL certificates using `certbot` (TBD – detailed instructions will follow).

3. Add a `.env` file to the root directory of the project, similar to step 2 in the local setup.

4. Run the following command to build and launch the application:
   ```bash
   docker-compose up --build
   ```