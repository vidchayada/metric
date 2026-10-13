CREATE TABLE users (
  user_id SERIAL PRIMARY KEY,
  full_name VARCHAR(150) NOT NULL,
  username VARCHAR(100) NOT NULL UNIQUE,
  email VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  phone VARCHAR(20),
  country_code VARCHAR(5),
  date_of_birth DATE,
  address TEXT,
  profile_image VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  role VARCHAR(10) NOT NULL DEFAULT 'user'
    CHECK (role IN ('user', 'admin'))
);

CREATE TABLE categories (
  category_id SERIAL PRIMARY KEY,
  category_name VARCHAR(100) NOT NULL,
  slug VARCHAR(100) NOT NULL UNIQUE,
  visibility VARCHAR(10) NOT NULL DEFAULT 'show'
    CHECK (visibility IN ('show', 'hidden')),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
  product_id SERIAL PRIMARY KEY,
  category_id INT,
  product_name VARCHAR(200) NOT NULL,
  description TEXT,
  base_price DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (category_id) REFERENCES categories(category_id)
    ON DELETE SET NULL
);

CREATE TABLE product_images (
  image_id SERIAL PRIMARY KEY,
  product_id INT NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  is_primary BOOLEAN DEFAULT FALSE,
  sort_order INT DEFAULT 0,
  FOREIGN KEY (product_id) REFERENCES products(product_id)
    ON DELETE CASCADE
);

CREATE TABLE product_attributes (
  attribute_id SERIAL PRIMARY KEY,
  product_id INT NOT NULL,
  size VARCHAR(10),
  color VARCHAR(50),
  stock_qty INT DEFAULT 0,
  FOREIGN KEY (product_id) REFERENCES products(product_id)
    ON DELETE CASCADE
);

CREATE TABLE cart_items (
  cart_item_id SERIAL PRIMARY KEY,
  user_id INT NULL,
  session_id VARCHAR(255),
  product_id INT NOT NULL,
  attribute_id INT,
  quantity INT NOT NULL DEFAULT 1,
  added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON DELETE SET NULL,
  FOREIGN KEY (product_id) REFERENCES products(product_id)
    ON DELETE CASCADE,
  FOREIGN KEY (attribute_id) REFERENCES product_attributes(attribute_id)
    ON DELETE SET NULL
);

CREATE TABLE orders (
  order_id SERIAL PRIMARY KEY,
  order_number VARCHAR(20) NOT NULL UNIQUE,
  user_id INT,
  order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  shipping_address TEXT NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(12) NOT NULL DEFAULT 'pending'
    CHECK (status IN ('pending','processing','shipped','completed','cancelled')),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON DELETE SET NULL
);

CREATE TABLE order_items (
  item_id SERIAL PRIMARY KEY,
  order_id INT NOT NULL,
  product_id INT NOT NULL,
  attribute_id INT,
  quantity INT NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,

  FOREIGN KEY (order_id) REFERENCES orders(order_id)
    ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(product_id),
  FOREIGN KEY (attribute_id) REFERENCES product_attributes(attribute_id)
    ON DELETE SET NULL
);

CREATE TABLE subscribers (
  id SERIAL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);