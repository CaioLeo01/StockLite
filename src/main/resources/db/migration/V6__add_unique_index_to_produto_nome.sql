CREATE UNIQUE INDEX uq_produto_nome_normalizado
ON stocklite.produto (LOWER(BTRIM(nome)));
