CREATE DATABASE retail_store

USE  RETAIL_STORE


CREATE TABLE users (
  id int NOT NULL AUTO_INCREMENT,
  name varchar(250) DEFAULT NULL,
  email varchar(255) NOT NULL,
  password_hash text NOT NULL,
  api_key varchar(32) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY email (email)
  );
  

CREATE TABLE locations (
    location_id int NOT NULL  AUTO_INCREMENT,
    location_name varchar(100) NOT NULL,
    CONSTRAINT pk_location PRIMARY KEY (location_id, location_name)
); 


CREATE TABLE departments(
    dept_id int NOT NULL  AUTO_INCREMENT,
    dept_name varchar(100) NOT NULL,
    CONSTRAINT pk_dept PRIMARY KEY (dept_id,  dept_name)
); 

CREATE TABLE categories (
    category_id int NOT NULL  AUTO_INCREMENT,
    category_name varchar(100) NOT NULL,
    dept_id int NOT NULL ,
    CONSTRAINT pk_category PRIMARY KEY (category_id,  category_name, dept_id),
    CONSTRAINT fk_category FOREIGN KEY (dept_id)  REFERENCES departments(dept_id)
); 

CREATE TABLE sub_categories (
    sub_category_id int NOT NULL  AUTO_INCREMENT,
    sub_category_name varchar(100) NOT NULL,
    category_id int NOT NULL,
    CONSTRAINT pk_sub_category PRIMARY KEY (sub_category_id,  sub_category_name, category_id),
    CONSTRAINT fk_sub_category FOREIGN KEY (category_id)  REFERENCES categories(category_id)
); 


CREATE TABLE sku (
    sku_id int NOT NULL  AUTO_INCREMENT,
    sku_name varchar(100) NOT NULL,
    location_id int NOT NULL,
    meta_info_id int NOT NULL,
    CONSTRAINT pk_sku PRIMARY KEY (sku_id, sku_name,  location_id, meta_info_id),
    CONSTRAINT fk_sku FOREIGN KEY (meta_info_id)  REFERENCES sub_categories(sub_category_id)
); 


/* ===================================================================================== */
/*                               Test Data                                               */
/* ===================================================================================== */
INSERT INTO locations (location_name) VALUES ("Perimeter"); 
INSERT INTO locations (location_name) VALUES ("Center"); 

INSERT INTO departments (dept_name) VALUES ("Bakery"); 
INSERT INTO departments (dept_name) VALUES ("Deli and Foodservice"); 
INSERT INTO departments (dept_name) VALUES ("Floral"); 
INSERT INTO departments (dept_name) VALUES ("Frozen"); 
INSERT INTO departments (dept_name) VALUES ("Grocery"); 
INSERT INTO departments (dept_name) VALUES ("GM"); 
INSERT INTO departments (dept_name) VALUES ("Seafood");

INSERT INTO categories (category_name, dept_id ) VALUES ("Bakery Bread", 1); 
INSERT INTO categories (category_name, dept_id ) VALUES ("Self Service Deli Cold", 2); 
INSERT INTO categories (category_name, dept_id ) VALUES ("Service Deli", 2); 
INSERT INTO categories (category_name, dept_id ) VALUES ("Bouquets and Cut Flowers", 3); 
INSERT INTO categories (category_name, dept_id ) VALUES ("Frozen Bake", 4); 
INSERT INTO categories (category_name, dept_id ) VALUES ("Audio Video", 6); 
INSERT INTO categories (category_name, dept_id ) VALUES ("Housewares", 6); 
INSERT INTO categories (category_name, dept_id ) VALUES ("Crackers", 5); 
INSERT INTO categories (category_name, dept_id ) VALUES ("Frozen Shellfish", 7); 
INSERT INTO categories (category_name, dept_id ) VALUES ("Other Seafood", 7); 

INSERT INTO sub_categories (sub_category_name, category_id ) VALUES ("Bagels", 1); 
INSERT INTO sub_categories (sub_category_name, category_id ) VALUES ("Beverages",  2); 
INSERT INTO sub_categories (sub_category_name, category_id ) VALUES ("All Other ",  3); 
INSERT INTO sub_categories (sub_category_name, category_id ) VALUES ("Bouquets and Cut Flowers",  4); 
INSERT INTO sub_categories (sub_category_name, category_id ) VALUES ("Bread or Dough Products Frozen",  5); 
INSERT INTO sub_categories (sub_category_name, category_id ) VALUES ("Audio",  6); 
INSERT INTO sub_categories (sub_category_name, category_id ) VALUES ("Video DVD",  6); 
INSERT INTO sub_categories (sub_category_name, category_id ) VALUES ("Beeding",  7); 
INSERT INTO sub_categories (sub_category_name, category_id ) VALUES ("Rice Cakes",  8); 
INSERT INTO sub_categories (sub_category_name, category_id ) VALUES ("Frozen Shellfish",  9); 
INSERT INTO sub_categories (sub_category_name, category_id ) VALUES ("All Other Seafood", 10 ); 
INSERT INTO sub_categories (sub_category_name, category_id ) VALUES ("Salads",  10); 
INSERT INTO sub_categories (sub_category_name, category_id ) VALUES ("Prepared Seafood Entrees",  10); 

INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC1",  1, 1); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC2",  1, 2); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC3",  1, 4); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC4",  2, 3); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC5",  1, 6); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC6",  2, 5); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC7",  1, 7); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC8",  1, 9); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC9",  2, 8); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC10",  1, 10); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC11",  2, 12); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC12",  1, 11); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC13",  1, 13); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC14",  2, 12); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC15",  1, 13); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC16",  2, 11); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC17",  1, 10); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC18",  2, 9); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC19",  1, 8); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC20",  1, 7); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC21",  2, 6); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC22",  1, 5); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC23",  2, 4); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC24",  1, 3); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC25",  1, 2); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC26",  2, 1); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC27",  1, 5); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC28",  2, 7); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC29",  1, 8); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC30",  1, 9); 
INSERT INTO sku(sku_name, location_id, meta_info_id) VALUES ("SKUDESC31",  2, 12); 
