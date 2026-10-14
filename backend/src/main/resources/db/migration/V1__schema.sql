CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    country_code VARCHAR(5),
    date_of_birth DATE,
    address TEXT,
    profile_image VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    role VARCHAR(10) NOT NULL DEFAULT 'USER'
       CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE categories (
  category_id BIGSERIAL PRIMARY KEY,
  category_name VARCHAR(100) NOT NULL,
  slug VARCHAR(100) NOT NULL UNIQUE,
  visibility VARCHAR(10) NOT NULL DEFAULT 'show'
    CHECK (visibility IN ('show', 'hidden')),
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
  product_id BIGSERIAL PRIMARY KEY,
  category_id BIGINT,
  product_name VARCHAR(200) NOT NULL,
  description TEXT,
  base_price DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_products_category
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
      ON DELETE SET NULL
);

CREATE TABLE product_images (
  image_id BIGSERIAL PRIMARY KEY,
  product_id BIGINT NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  is_primary BOOLEAN DEFAULT FALSE,
  sort_order INT DEFAULT 0,
  CONSTRAINT fk_images_product
    FOREIGN KEY (product_id) REFERENCES products(product_id)
      ON DELETE CASCADE
);

CREATE TABLE product_attributes (
  attribute_id BIGSERIAL PRIMARY KEY,
  product_id BIGINT NOT NULL,
  size VARCHAR(10),
  color VARCHAR(50),
  stock_qty INT DEFAULT 0,
  CONSTRAINT fk_attributes_product
    FOREIGN KEY (product_id) REFERENCES products(product_id)
      ON DELETE CASCADE
);

CREATE TABLE cart_items (
  cart_item_id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NULL,
  session_id VARCHAR(255),
  product_id BIGINT NOT NULL,
  attribute_id BIGINT,
  quantity INT NOT NULL DEFAULT 1,
  added_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_cart_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
      ON DELETE SET NULL,
  CONSTRAINT fk_cart_product
    FOREIGN KEY (product_id) REFERENCES products(product_id)
      ON DELETE CASCADE,
  CONSTRAINT fk_cart_attribute
    FOREIGN KEY (attribute_id) REFERENCES product_attributes(attribute_id)
      ON DELETE SET NULL
);

CREATE TABLE orders (
  order_id BIGSERIAL PRIMARY KEY,
  order_number VARCHAR(20) NOT NULL UNIQUE,
  user_id BIGINT,
  order_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
  shipping_address TEXT NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(12) NOT NULL DEFAULT 'PENDING'
    CHECK (status IN ('PENDING','PROCESSING','SHIPPED','COMPLETED','CANCELLED')),
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_orders_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
      ON DELETE SET NULL
);

CREATE TABLE order_items (
  item_id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  attribute_id BIGINT,
  quantity INT NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,

  CONSTRAINT fk_order_items_order
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
      ON DELETE CASCADE,
  CONSTRAINT fk_order_items_product
    FOREIGN KEY (product_id) REFERENCES products(product_id),
 CONSTRAINT fk_order_items_attribute
    FOREIGN KEY (attribute_id) REFERENCES product_attributes(attribute_id)
      ON DELETE SET NULL
);

CREATE TABLE subscribers (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);