-- ============================================================
-- PlatePath seed data  (INSERT IGNORE — safe to re-run)
-- Password for all accounts: pass123
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL57Ti2
-- ============================================================

-- Users
INSERT IGNORE INTO users (id, name, email, password, phone, address, role, created_at, updated_at)
VALUES
  (1, 'Super Admin',   'admin@platepath.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL57Ti2', '9000000001', '100 Admin Plaza',        'ADMIN',             NOW(), NOW()),
  (2, 'Ravi Sharma',   'owner@spicegarden.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL57Ti2', '9000000002', '12 MG Road, Bangalore',  'RESTAURANT_OWNER',  NOW(), NOW()),
  (3, 'Lin Wei',       'owner@dragonwok.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL57Ti2', '9000000003', '45 Koramangala, Bangalore','RESTAURANT_OWNER', NOW(), NOW()),
  (4, 'Ananya Reddy',  'ananya@example.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL57Ti2', '9000000004', '7 Indiranagar, Bangalore','CUSTOMER',         NOW(), NOW()),
  (5, 'Kiran Patel',   'kiran@example.com',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL57Ti2', '9000000005', '23 Whitefield, Bangalore','CUSTOMER',         NOW(), NOW()),
  (6, 'Arjun Kumar',   'arjun@delivery.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL57Ti2', '9000000006', '56 Jayanagar, Bangalore', 'DELIVERY_BOY',     NOW(), NOW()),
  (7, 'Priya Singh',   'priya@delivery.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL57Ti2', '9000000007', '89 BTM Layout, Bangalore','DELIVERY_BOY',     NOW(), NOW());

-- Restaurants
INSERT IGNORE INTO restaurants (id, name, address, cuisine_type, rating, is_open, owner_id, latitude, longitude, image_url, delivery_radius_km, avg_prep_time_minutes, created_at)
VALUES
  (1, 'Spice Garden', '12 MG Road, Bangalore', 'INDIAN',   4.5, true, 2, 12.9716, 77.5946, 'https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=400', 10.0, 25, NOW()),
  (2, 'Dragon Wok',   '45 Koramangala, Bangalore', 'CHINESE', 4.2, true, 3, 12.9352, 77.6245, 'https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400', 8.0, 20, NOW());

-- Menu Items — Spice Garden (restaurant_id=1)
INSERT IGNORE INTO menu_items (id, name, description, price, category, is_available, image_url, is_vegetarian, restaurant_id)
VALUES
  (1,  'Paneer Butter Masala',  'Cottage cheese in rich tomato-butter gravy',    280.00, 'Main Course', true, 'https://images.unsplash.com/photo-1631452180519-c014fe946bc7?w=300', true,  1),
  (2,  'Chicken Biryani',       'Fragrant basmati rice with spiced chicken',      320.00, 'Main Course', true, 'https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=300', false, 1),
  (3,  'Dal Tadka',             'Yellow lentils with smoky tempering',            160.00, 'Main Course', true, 'https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=300', true,  1),
  (4,  'Garlic Naan',           'Soft bread baked in tandoor with garlic',         50.00, 'Bread',       true, 'https://images.unsplash.com/photo-1601050690597-df0568f70950?w=300', true,  1),
  (5,  'Mango Lassi',           'Chilled yoghurt drink with Alphonso mango',       90.00, 'Beverage',    true, 'https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=300', true,  1);

-- Menu Items — Dragon Wok (restaurant_id=2)
INSERT IGNORE INTO menu_items (id, name, description, price, category, is_available, image_url, is_vegetarian, restaurant_id)
VALUES
  (6,  'Veg Fried Rice',        'Wok-tossed rice with fresh vegetables',          180.00, 'Main Course', true, 'https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=300', true,  2),
  (7,  'Chicken Manchurian',    'Crispy chicken in sweet-spicy Manchurian sauce', 250.00, 'Starter',     true, 'https://images.unsplash.com/photo-1626200419199-391ae4be7a41?w=300', false, 2),
  (8,  'Hakka Noodles',         'Stir-fried egg noodles with vegetables',         200.00, 'Main Course', true, 'https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d?w=300', true,  2),
  (9,  'Spring Rolls (4 pcs)',  'Crispy rolls stuffed with vegetables',           140.00, 'Starter',     true, 'https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?w=300', true,  2),
  (10, 'Hot & Sour Soup',       'Tangy soup with mushrooms and tofu',             120.00, 'Soup',        true, 'https://images.unsplash.com/photo-1547592166-23ac45744acd?w=300', true,  2);

-- Orders
INSERT IGNORE INTO orders (id, customer_id, restaurant_id, total_amount, status, delivery_address, delivery_latitude, delivery_longitude, delivery_boy_id, estimated_delivery_time, estimated_minutes, special_instructions, placed_at, updated_at)
VALUES
  (1, 4, 1, 650.00,  'DELIVERED',        '7 Indiranagar, Bangalore',  12.9784, 77.6408, 6, DATE_ADD(NOW(), INTERVAL -30 MINUTE), 40, 'Less spicy please', DATE_ADD(NOW(), INTERVAL -2 HOUR), DATE_ADD(NOW(), INTERVAL -30 MINUTE)),
  (2, 5, 2, 530.00,  'OUT_FOR_DELIVERY', '23 Whitefield, Bangalore',  12.9698, 77.7499, 7, DATE_ADD(NOW(), INTERVAL 25 MINUTE),  35, NULL,               DATE_ADD(NOW(), INTERVAL -45 MINUTE), NOW()),
  (3, 4, 2, 320.00,  'PLACED',           '7 Indiranagar, Bangalore',  12.9784, 77.6408, NULL, DATE_ADD(NOW(), INTERVAL 40 MINUTE), 40, 'Extra sauce',      NOW(), NOW());

-- Order Items
INSERT IGNORE INTO order_items (id, order_id, menu_item_id, quantity, unit_price)
VALUES
  (1, 1, 2, 1, 320.00),
  (2, 1, 1, 1, 280.00),
  (3, 1, 4, 1,  50.00),
  (4, 2, 7, 1, 250.00),
  (5, 2, 8, 1, 200.00),
  (6, 2, 9, 1, 140.00),
  (7, 3, 7, 1, 250.00),
  (8, 3, 6, 1, 180.00);

-- Tracking Updates
INSERT IGNORE INTO tracking_updates (id, order_id, status, message, latitude, longitude, timestamp)
VALUES
  (1,  1, 'PLACED',            'Order placed successfully',           12.9716, 77.5946, DATE_ADD(NOW(), INTERVAL -2 HOUR)),
  (2,  1, 'CONFIRMED',         'Restaurant confirmed your order',     12.9716, 77.5946, DATE_ADD(NOW(), INTERVAL -115 MINUTE)),
  (3,  1, 'PREPARING',         'Chef is preparing your food',         12.9716, 77.5946, DATE_ADD(NOW(), INTERVAL -100 MINUTE)),
  (4,  1, 'OUT_FOR_DELIVERY',  'Arjun picked up your order',          12.9716, 77.5946, DATE_ADD(NOW(), INTERVAL -60 MINUTE)),
  (5,  1, 'DELIVERED',         'Order delivered. Enjoy your meal!',   12.9784, 77.6408, DATE_ADD(NOW(), INTERVAL -30 MINUTE)),
  (6,  2, 'PLACED',            'Order placed successfully',           12.9352, 77.6245, DATE_ADD(NOW(), INTERVAL -45 MINUTE)),
  (7,  2, 'CONFIRMED',         'Restaurant confirmed your order',     12.9352, 77.6245, DATE_ADD(NOW(), INTERVAL -40 MINUTE)),
  (8,  2, 'PREPARING',         'Chef is preparing your food',         12.9352, 77.6245, DATE_ADD(NOW(), INTERVAL -30 MINUTE)),
  (9,  2, 'OUT_FOR_DELIVERY',  'Priya is on the way',                 12.9500, 77.6900, DATE_ADD(NOW(), INTERVAL -10 MINUTE)),
  (10, 3, 'PLACED',            'Order placed successfully',           12.9352, 77.6245, NOW());
