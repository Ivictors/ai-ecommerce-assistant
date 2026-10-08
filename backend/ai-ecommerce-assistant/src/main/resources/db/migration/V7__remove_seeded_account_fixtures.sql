DELETE FROM conversations
WHERE user_id IN (
    SELECT id
    FROM users
    WHERE email IN (
        'victor@example.com',
        'ana@example.com',
        'carlos@example.com'
    )
);

DELETE FROM order_items oi
USING orders o
JOIN users u ON u.id = o.user_id
WHERE oi.order_id = o.id
  AND u.email IN (
      'victor@example.com',
      'ana@example.com',
      'carlos@example.com'
  );

DELETE FROM orders o
USING users u
WHERE o.user_id = u.id
  AND u.email IN (
      'victor@example.com',
      'ana@example.com',
      'carlos@example.com'
  );

DELETE FROM users
WHERE email IN (
    'victor@example.com',
    'ana@example.com',
    'carlos@example.com'
);
