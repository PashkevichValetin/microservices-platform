-- Create tables for user_db
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
    );

-- Insert sample data
INSERT INTO users (name, email) VALUES
                                    ('John Doe', 'john.doe@example.com'),
                                    ('Jane Smith', 'jane.smith@example.com'),
                                    ('Bob Johnson', 'bob.johnson@example.com')
    ON CONFLICT (email) DO NOTHING;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_registration_date ON users(registration_date);

-- Create additional test users
INSERT INTO users (name, email) VALUES
                                    ('Alice Brown', 'alice.brown@example.com'),
                                    ('Charlie Wilson', 'charlie.wilson@example.com'),
                                    ('Diana Prince', 'diana.prince@example.com'),
                                    ('Evan Wright', 'evan.wright@example.com'),
                                    ('Fiona Green', 'fiona.green@example.com')
    ON CONFLICT (email) DO NOTHING;