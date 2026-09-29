# carteira-digital-api

API REST de carteira digital com usuários comuns e lojistas e transferência de dinheiro entre eles.

Projeto de estudo em Java puro, sem frameworks, construído em etapas com TDD.

## Tecnologias

- Java 25
- Maven
- PostgreSQL 18 (JDBC puro, sem ORM)
- JUnit 6
- Docker e Docker Compose

## Como executar

Pré-requisito: Docker com Docker Compose.

```bash
docker compose up -d --build
```

A API sobe em `http://localhost:8080`. O banco é criado e populado automaticamente com dois usuários de exemplo:

| id | nome | tipo | saldo |
|----|------|------|-------|
| 1 | Ana Souza | COMUM | 100.00 |
| 2 | Loja do João | LOJISTA | 0.00 |

## Uso

```bash
curl -i -X POST http://localhost:8080/transfer \
  -H "Content-Type: application/json" \
  -d '{"value": 10.00, "payer": 1, "payee": 2}'
```

| Status | Situação |
|--------|----------|
| 201 | Transferência concluída |
| 400 | JSON inválido ou campo obrigatório ausente |
| 404 | Usuário não encontrado |
| 405 | Método diferente de POST |
| 422 | Regra de negócio violada (saldo insuficiente, lojista enviando, transferência para si mesmo ou não autorizada) |

## Testes

Parte dos testes usa o banco real, então o Postgres precisa estar rodando:

```bash
docker compose up -d postgres
mvn test
```

## Decisões técnicas

- **Sem framework.** Servidor HTTP com `com.sun.net.httpserver`, JSON com um parser próprio e injeção de dependência manual na classe `Main`.
- **Camadas.** `dominio` (regras de negócio), `aplicacao` (casos de uso e interfaces) e `infra` (banco, HTTP e integrações externas). O domínio não depende de nenhuma camada de infraestrutura.
- **Transação real.** Débito, crédito e registro da notificação acontecem na mesma transação do Postgres. Qualquer falha reverte tudo.
- **Concorrência.** A leitura das carteiras usa `SELECT ... FOR UPDATE`, então duas transferências simultâneas da mesma carteira são processadas em fila.
- **Autorizador externo em modo *fail closed*.** Se o serviço autorizador estiver fora do ar, responder erro ou demorar mais de 3 segundos, a transferência é recusada.
- **Notificação com Transactional Outbox.** A transferência só registra a notificação numa tabela, dentro da mesma transação. Um processo separado envia as pendentes a cada 10 segundos e mantém na fila as que falharem, para tentar de novo. Assim a instabilidade do serviço de notificação nunca afeta a transferência.

## Limitações conhecidas

- O cadastro de usuários não foi implementado. Os usuários vêm do script de carga inicial.
- O parser de JSON cobre só o necessário para a API: não trata escapes Unicode (`\uXXXX`) nem notação científica em números.
- As notificações pendentes são reenviadas indefinidamente, sem limite de tentativas.