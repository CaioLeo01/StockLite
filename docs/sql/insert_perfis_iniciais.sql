INSERT INTO stocklite.perfil (nome, descricao)
VALUES
    ('ADMIN', 'Perfil com acesso completo ao sistema.'),
    ('ESTOQUISTA', 'Perfil responsavel pelo controle e movimentacao de estoque.'),
    ('VISUALIZADOR', 'Perfil com permissao apenas para consulta.')
ON CONFLICT DO NOTHING;
