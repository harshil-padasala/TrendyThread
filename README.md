# Spring Boot Blog REST API 📚

A versatile RESTful API for managing a comprehensive blog application built using Spring Boot 🌱.

## Table of Contents 📋

- [Overview](#overview)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Endpoints](#endpoints)
  - [Categories](#categories)
  - [Posts](#posts)
  - [Users](#users)

## Overview 🌐

This project serves as the backend for a feature-rich blog application, providing RESTful endpoints for managing Categories, Users, and Posts. It is built using Spring Boot, which offers robust and efficient development capabilities for building web applications 🚀.

## Features 🌟

- **Categories**: Manage blog post categories, including create, retrieve, update, and delete operations 🗂️.
- **Users**: Handle user management, authentication, and authorization 🔐.
- **Posts**: Create, retrieve, update, and delete blog posts within specific categories ✏️.

## Prerequisites 📝

Before you begin, ensure you have met the following requirements:

- Java Development Kit (JDK) 8 or higher ☕
- Maven or Gradle for building the project 🛠️
- MySQL or another relational database 🗃️

## Getting Started 🚀

To get started with this project, follow these steps:

1. Clone the repository:

   ```bash
   git clone https://github.com/yourusername/spring-boot-blog-api.git


## Endpoints 🛣️

### Categories 🗂️

- **GET /api/category**: Retrieve a list of all blog categories.
- **GET /api/category/{id}**: Retrieve a specific category by ID.
- **POST /api/category**: Create a new blog category.
- **PUT /api/category/{id}**: Update an existing blog category.
- **DELETE /api/category/{id}**: Delete a blog category.

### Posts ✏️

- **GET /api/posts**: Retrieve a list of all blog posts.
- **GET /api/posts/{id}**: Retrieve a specific blog post by ID.
- **GET /api/posts/category/{categoryId}**: Retrieve a specific blog post by category.
- **GET /api/posts/user/{userId}**: Retrieve a specific blog post by user.
- **GET /api/posts/search/{keyword}**: Retrieve specific blog post by using a keyword.
- **POST /api/posts/image/upload/{postId}**: Upload an photo for a specific post.
- **POST /api/posts/users/{userID}/category/{categoryId**: Create a new blog post for speacific user and category.
- **PUT /api/posts/{id}**: Update an existing blog post.
- **DELETE /api/posts/{id}**: Delete a blog post.

### Users 🔒

- **GET /api/users**: Retrieve all the user's profile.
- **GET /api/users/{userId}**: Retrieve a specific user by ID.
- **POST /api/users**: Register a new user.
- **PUT /api/users/{userId}**: Upload a user for a specific userId.
- **DELETE /api/users/{userId}**: Delete a user.
