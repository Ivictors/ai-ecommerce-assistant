INSERT INTO users (name, email)
VALUES
    ('Victor Marques', 'victor@example.com'),
    ('Ana Silva', 'ana@example.com'),
    ('Carlos Oliveira', 'carlos@example.com');


INSERT INTO products (
    name,
    description,
    price,
    stock,
    active
)
VALUES
    (
        'Notebook Pro 14',
        'Notebook com processador Intel Core i7, 16GB de RAM e SSD de 512GB.',
        6499.90,
        10,
        TRUE
    ),
    (
        'Mouse Sem Fio MX',
        'Mouse sem fio ergonômico com conexão Bluetooth e USB.',
        249.90,
        35,
        TRUE
    ),
    (
        'Teclado Mecânico RGB',
        'Teclado mecânico com switches brown e iluminação RGB.',
        399.90,
        20,
        TRUE
    ),
    (
        'Monitor 27 144Hz',
        'Monitor de 27 polegadas com resolução QHD e taxa de atualização de 144Hz.',
        1899.90,
        8,
        TRUE
    ),
    (
        'Headset Gamer Pro',
        'Headset com áudio surround, microfone removível e conexão USB.',
        549.90,
        15,
        TRUE
    ),
    (
        'Webcam Full HD',
        'Webcam Full HD 1080p para reuniões, aulas e streaming.',
        299.90,
        25,
        TRUE
    ),
    (
        'Notebook Legacy',
        'Produto descontinuado utilizado para testar produtos inativos.',
        2999.90,
        0,
        FALSE
    );

INSERT INTO orders (
    user_id,
    status,
    total
)
VALUES
    (
        (SELECT id FROM users WHERE email = 'victor@example.com'),
        'PAID',
        6899.80
    ),
    (
        (SELECT id FROM users WHERE email = 'ana@example.com'),
        'PENDING',
        249.90
    ),
    (
        (SELECT id FROM users WHERE email = 'carlos@example.com'),
        'CANCELLED',
        1899.90
    );

INSERT INTO order_items (
    order_id,
    product_id,
    quantity,
    unit_price
)
VALUES
    (
        (
            SELECT o.id
            FROM orders o
                     JOIN users u ON u.id = o.user_id
            WHERE u.email = 'victor@example.com'
              AND o.status = 'PAID'
        ),
        (SELECT id FROM products WHERE name = 'Notebook Pro 14'),
        1,
        6499.90
    ),
    (
        (
            SELECT o.id
            FROM orders o
                     JOIN users u ON u.id = o.user_id
            WHERE u.email = 'victor@example.com'
              AND o.status = 'PAID'
        ),
        (SELECT id FROM products WHERE name = 'Mouse Sem Fio MX'),
        1,
        249.90
    ),
    (
        (
            SELECT o.id
            FROM orders o
                     JOIN users u ON u.id = o.user_id
            WHERE u.email = 'victor@example.com'
              AND o.status = 'PAID'
        ),
        (SELECT id FROM products WHERE name = 'Teclado Mecânico RGB'),
        1,
        399.90
    ),
    (
        (
            SELECT o.id
            FROM orders o
                     JOIN users u ON u.id = o.user_id
            WHERE u.email = 'ana@example.com'
              AND o.status = 'PENDING'
        ),
        (SELECT id FROM products WHERE name = 'Mouse Sem Fio MX'),
        1,
        249.90
    ),
    (
        (
            SELECT o.id
            FROM orders o
                     JOIN users u ON u.id = o.user_id
            WHERE u.email = 'carlos@example.com'
              AND o.status = 'CANCELLED'
        ),
        (SELECT id FROM products WHERE name = 'Monitor 27 144Hz'),
        1,
        1899.90
    );

INSERT INTO conversations (user_id)
VALUES
    (
        (SELECT id FROM users WHERE email = 'victor@example.com')
    ),
    (
        (SELECT id FROM users WHERE email = 'ana@example.com')
    );