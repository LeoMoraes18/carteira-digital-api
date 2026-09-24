INSERT INTO usuarios (nome_completo, documento, email, tipo, saldo) VALUES
    ('Ana Souza', '11111111111', 'ana@email.com', 'COMUM', 100.00),
    ('Loja do João', '22222222222', 'joao@email.com', 'LOJISTA', 0.00)
    ON CONFLICT (documento) DO NOTHING;