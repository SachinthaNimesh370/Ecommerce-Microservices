package com.ecommerce.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@SpringBootApplication
public class OrderServiceApplication {

	public static void main(String[] args) {
		createDatabaseIfNotExists();
		SpringApplication.run(OrderServiceApplication.class, args);
	}

	private static void createDatabaseIfNotExists() {
		String url = "jdbc:postgresql://localhost:5432/postgres";
		String user = "postgres";
		String password = "123123";

		try (Connection connection = DriverManager.getConnection(url, user, password);
			 Statement statement = connection.createStatement()) {

			ResultSet resultSet = statement.executeQuery("SELECT 1 FROM pg_database WHERE datname = 'order_db'");
			if (!resultSet.next()) {
				statement.executeUpdate("CREATE DATABASE order_db");
				System.out.println("Database 'order_db' created successfully.");
			} else {
				System.out.println("Database 'order_db' already exists.");
			}
		} catch (Exception e) {
			System.err.println("Error creating database: " + e.getMessage());
		}
	}
}
