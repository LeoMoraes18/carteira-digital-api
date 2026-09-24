CREATE TABLE usuarios (
                          id              BIGSERIAL PRIMARY KEY,
                          nome_completo   VARCHAR(150) NOT NULL,
                          documento       VARCHAR(20)  NOT NULL UNIQUE,
                          email           VARCHAR(150) NOT NULL UNIQUE,
                          tipo            VARCHAR(10)  NOT NULL,
                          saldo           NUMERIC(15, 2) NOT NULL DEFAULT 0,
                          CONSTRAINT chk_tipo CHECK (tipo IN ('COMUM', 'LOJISTA')),
                          CONSTRAINT chk_saldo_nao_negativo CHECK (saldo >= 0)
);

CREATE TABLE notificacoes_pendentes (
                                        id                BIGSERIAL PRIMARY KEY,
                                        destinatario_id   BIGINT NOT NULL REFERENCES usuarios (id),
                                        email             VARCHAR(150) NOT NULL,
                                        valor             NUMERIC(15, 2) NOT NULL,
                                        criada_em         TIMESTAMP NOT NULL DEFAULT now()
);