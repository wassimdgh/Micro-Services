-- Create databases for microservices
CREATE DATABASE IF NOT EXISTS auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS meteo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS arrosage_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create users for microservices (optional - for production)
-- CREATE USER 'auth_user'@'%' IDENTIFIED BY 'auth_pass';
-- CREATE USER 'meteo_user'@'%' IDENTIFIED BY 'meteo_pass';
-- CREATE USER 'arrosage_user'@'%' IDENTIFIED BY 'arrosage_pass';

-- Grant privileges (optional - for production)
-- GRANT ALL PRIVILEGES ON auth_db.* TO 'auth_user'@'%';
-- GRANT ALL PRIVILEGES ON meteo_db.* TO 'meteo_user'@'%';
-- GRANT ALL PRIVILEGES ON arrosage_db.* TO 'arrosage_user'@'%';
-- FLUSH PRIVILEGES;

-- Show created databases
SHOW DATABASES LIKE '%_db';
