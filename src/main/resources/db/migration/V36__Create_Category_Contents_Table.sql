-- Create category_contents table
CREATE TABLE category_contents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID NOT NULL,
    content_type VARCHAR(20) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    correct_answer TEXT,
    options JSONB,
    points INTEGER NOT NULL DEFAULT 10,
    time_limit INTEGER,
    difficulty VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    content_order INTEGER NOT NULL DEFAULT 0,
    metadata JSONB,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_content FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Create indexes
CREATE INDEX idx_content_category ON category_contents(category_id);
CREATE INDEX idx_content_type ON category_contents(content_type);
CREATE INDEX idx_content_order ON category_contents(category_id, content_order);
CREATE INDEX idx_content_active ON category_contents(is_active);

-- Create trigger for category_contents
CREATE TRIGGER category_contents_update_updated_at BEFORE UPDATE ON category_contents
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

