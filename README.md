# Planora Travel and Booking System Application

The Travel Planning & Booking System is a Java-based backend application that allows users to create trips and book travel services such as flights and hotels. Each trip acts as a central container that organizes all related bookings.

## Technologies used

1. Java (Programming Language)
2. Spring Boot (Application Platform)
3. Spring Data JPA (Data persistence)
4. Maven
5. PostgreSQL (Database)
6. React
7. Mkcert

## Getting Started

### Prerequisites
* Java 21 (for Spring Boot)
* Maven 3.9.12 (for building and running the Spring Boot application)
* Node.js and npm (for React)
* PostgreSQL (for the database)
* Mkcert (for generating local SSL certificates)
* Git

#### Clone the repository to your local machine using Git:

```
git clone https://github.com/Vladcuhandra/Planora_TravelandBooking_System.git
```

#### Navigate to the project directory where the repository was cloned:

```
cd your-repository-folder
```

#### Inside the cloned repository, navigate to the backend directory:

```
cd backend
```

#### Make sure you have PostgreSQL installed and running. Create a database and user for your project:

```
psql -U postgres
CREATE DATABASE your_database_name;
CREATE USER your_db_user WITH PASSWORD 'your_password';
ALTER ROLE your_db_user SET client_encoding TO 'utf8';
ALTER ROLE your_db_user SET default_transaction_isolation TO 'read committed';
ALTER ROLE your_db_user SET timezone TO 'UTC';
GRANT ALL PRIVILEGES ON DATABASE your_database_name TO your_db_user;
```

#### Open application.properties or application.yml in src/main/resources/ and configure the database connection:

```
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database_name
spring.datasource.username=your_db_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
```

#### Install mkcert if you haven't already:

```
brew install mkcert  # macOS
choco install mkcert # Windows
```

#### Generate the certificates for local development:

```
mkcert -install
mkcert localhost
```

#### Move the generated certificates (localhost.pem and localhost-key.pem) into your backend directory, and configure SSL in application.properties:

```
server.port=8843
server.ssl.key-store=classpath:localhost.p12
server.ssl.key-store-password=password
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=localhost
```

#### Build the project by using maven to compile and package the application:
	
```
mvn clean install
```
	
#### Run the application:
	
```
mvn spring-boot:run
```

#### Navigate to the Frontend Folder:

```
cd ../frontend
```

#### Install the necessary npm dependencies for the React app:

```
npm install
```

#### Start the React development server:

```
npm start
```
	
#### Access the Home screen

The application will be available at the URL: [Home](http://localhost:5173).

The home screen will give you relevant links to navigate.
