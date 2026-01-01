# Data Unifier

A Spring Boot application that demonstrates the Adapter Pattern by unifying data from multiple databases (PostgreSQL, MySQL) using Kafka message broker.

## Features

- PostgreSQL Adapter - for user data management
- MySQL Adapter - for order data processing
- Kafka Integration - event-driven data synchronization
- Unified REST API - single interface for client applications
- Docker Containerization - easy deployment and development

## Tech Stack

- Java 21 - Core programming language
- Spring Boot 3 - Application framework
- PostgreSQL - Relational database for user data
- MySQL - Relational database for order data
- Apache Kafka - Message broker for data events
- Docker & Docker Compose - Containerization
- Gradle - Build tool
- Spring Data JPA - Database access

## Prerequisites

- Java 21 or higher
- Docker and Docker Compose
- Git

## Quick Start

1. Clone the repository
   ```bash
   git clone https://github.com/YOUR_USERNAME/data-unifier.git
   cd data-unifier