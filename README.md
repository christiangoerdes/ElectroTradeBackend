# ElectroTrade Backend

This repository contains the backend code for the ElectroTrade project. The backend of the project is built using the Java Spring Framework and uses PostgreSQL as the database.

## Running the backend 
To run the Spring Boot backend follow these steps:
**Note**: Before proceeding, ensure that you have Java and Maven installed on your system and ensure that Port 8080 is available.

1. **Clone the Repository**: First, clone the backend repository to your local machine.
   
2. **Navigate to the Backend Directory**: Open your terminal and navigate to the root directory of the backend project.

3. **Set Up the Database**: Make sure you have PostgreSQL installed and running on your system.
Configure the database connection settings in the application.yml file located in the backend project.

4. **Build the Backend**: Build the Spring Boot backend using Maven. Run the following command in the backend project's root directory:
```
mvn clean install
```

5. **Run the Backend**: Once the project is built successfully, you can run it using the following command:
```
mvn spring-boot:run
```

6. **Access the Backend**: The Spring Boot backend will now be running on port 8080 by default. You can access it in your web browser or use it for API requests by navigating to http://localhost:8080.
