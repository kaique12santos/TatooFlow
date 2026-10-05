ALTER TABLE usuarios ADD COLUMN aparelho_id VARCHAR(255);
ALTER TABLE usuarios ADD CONSTRAINT uk_usuarios_aparelho_id UNIQUE (aparelho_id);
