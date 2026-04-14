-- Create category_tags table for tags association
CREATE TABLE category_tags (
    category_id UUID NOT NULL,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (category_id, tag),
    CONSTRAINT fk_category_tags FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Create index for tags search
CREATE INDEX idx_category_tag ON category_tags(tag);

