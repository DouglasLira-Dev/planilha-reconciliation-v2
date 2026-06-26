-- Criação da tabela de usuários
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'OPERATOR')),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Criação da tabela de auditoria
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Criação da tabela de histórico de reconciliações
CREATE TABLE IF NOT EXISTS reconciliation_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    executed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    filename_financeiro VARCHAR(255),
    filename_cadastro VARCHAR(255),
    total_conformes INTEGER DEFAULT 0,
    total_faltantes_cadastro INTEGER DEFAULT 0,
    total_excedentes_cadastro INTEGER DEFAULT 0,
    total_divergencias INTEGER DEFAULT 0,
    total_conflitos_cpf INTEGER DEFAULT 0,
    total_abreviacoes INTEGER DEFAULT 0,
    total_cancelados INTEGER DEFAULT 0,
    report_path VARCHAR(500)
);