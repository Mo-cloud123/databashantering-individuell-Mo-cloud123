
CREATE DATABASE IF NOT EXISTS moshop;
USE moshop;


CREATE TABLE IF NOT EXISTS categories (
 CategoryId INT PRIMARY KEY AUTO_INCREMENT,
 Name VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS products (
    ProductId INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(255) NOT NULL,
    Price DECIMAL(10,2) NOT NULL,
    VatRate DECIMAL(5,2) NOT NULL,
    CategoryId INT,
    FOREIGN KEY (CategoryId) REFERENCES categories(CategoryId)
    );

CREATE TABLE IF NOT EXISTS orders (
    OrderId INT PRIMARY KEY AUTO_INCREMENT,
    OrderDate DATETIME NOT NULL,
    TotalAmount DECIMAL(10,2) NOT NULL,
    TotalVat DECIMAL(10,2) NOT NULL,
    ReceiptNumber VARCHAR(50) NOT NULL
    );

CREATE TABLE IF NOT EXISTS orderdetails (
    OrderDetailId INT PRIMARY KEY AUTO_INCREMENT,
    OrderId INT NOT NULL,
    ProductId INT NOT NULL,
    Quantity INT NOT NULL,
    UnitPrice DECIMAL(10,2) NOT NULL,
    VatRate DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (OrderId) REFERENCES orders(OrderId),
    FOREIGN KEY (ProductId) REFERENCES products(ProductId)
    );

-- Infoga kategorier om de inte redan finns
INSERT IGNORE INTO categories (CategoryId, Name) VALUES
(1, 'Dryck'),
(2, 'Snacks'),
(3, 'Mat');

-- Infoga produkter om de inte redan finns
INSERT IGNORE INTO products (ProductId, Name, Price, VatRate, CategoryId) VALUES
(1, 'Kaffe', 25, 12, 1),
(2, 'Te', 20, 12, 1),
(3, 'Vatten', 15, 12, 1),
(4, 'Chockladkaka', 30, 25, 2),
(5, 'Smörgås', 45, 12, 3),
(6, 'Kanelbulle', 30, 12, 2),
(7, 'Korv', 18, 12, 3),;