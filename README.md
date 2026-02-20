# Planora Travel and Booking System Application

The Travel Planning & Booking System is a Java-based backend application that allows users to create trips and book travel services such as flights and hotels. Each trip acts as a central container that organizes all related bookings.

## Technologies used

1. Java (Programming Language)
2. Spring Boot (Application Platform)
3. Spring Data JPA (Data persistence)
4. H2 (Database)
5. Thymeleaf

## Getting Started

### Prerequisites
1. Java 21
2. Maven 3.9.12
3. Git

#### Clone the repository to your local machine using Git:

```
git clone https://github.com/Vladcuhandra/Planora_TravelandBooking_System.git
```

#### Navigate to the project directory where the repository was cloned

#### Build the project by using maven to compile and package the application:
	
```
mvn clean install
```
	
#### Run the application:
	
```
mvn spring-boot:run
```
	
#### Access the Home screen

The application will be available at the URL: [Home](http://localhost:8080).

The home screen will give you relevant links to navigate.


## Database

This application is using H2 in-memory database.

While the application is running, you can access the [H2 Console](http://localhost:8080/h2-console) if you want to see the data outside the application. 

You can connect to the DB using the JDBC URL: 'jdbc:h2:file:./data/mydb;AUTO_SERVER=TRUE' and user 'sa' with NO password. 
