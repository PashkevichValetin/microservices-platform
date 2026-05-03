-- Create tables for order_db
CREATE TABLE IF NOT EXISTS orders (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      user_id BIGINT NOT NULL,
                                      amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
    );

-- Insert sample data
INSERT INTO orders (user_id, amount, status) VALUES
                                                 (1, 100.50, 'COMPLETED'),
                                                 (1, 49.99, 'PENDING'),
                                                 (2, 199.99, 'COMPLETED'),
                                                 (3, 25.00, 'CANCELLED'),
                                                 (2, 75.50, 'PROCESSING'),
                                                 (4, 150.00, 'COMPLETED'),
                                                 (4, 29.99, 'SHIPPED'),
                                                 (5, 89.99, 'COMPLETED'),
                                                 (6, 45.50, 'PENDING'),
                                                 (7, 199.99, 'PROCESSING'),
                                                 (8, 299.99, 'COMPLETED');

-- Create additional test data
INSERT INTO orders (user_id, amount, status)
SELECT
    FLOOR(1 + RAND() * 8) as user_id,
    ROUND(RAND() * 500, 2) as amount,
    ELT(FLOOR(1 + RAND() * 5), 'PENDING', 'PROCESSING', 'COMPLETED', 'SHIPPED', 'CANCELLED') as status
FROM
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
     UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) as numbers;