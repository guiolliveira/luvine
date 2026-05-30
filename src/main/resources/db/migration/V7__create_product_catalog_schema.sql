CREATE EXTENSION IF NOT EXISTS pg_trgm;

ALTER TABLE users
ADD CONSTRAINT chk_users_user_role CHECK (user_role IN ('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')),
ADD CONSTRAINT chk_users_user_provider CHECK(user_provider IN ('GOOGLE', 'LOCAL'));

CREATE TABLE categories(
    id BIGSERIAL PRIMARY KEY,
    public_id UUID UNIQUE NOT NULL,
    parent_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE SET NULL,
    category_name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(150) UNIQUE NOT NULL,
    description TEXT NOT NULL,
    image_url VARCHAR(500),
    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_categories_display_order CHECK (display_order >= 0),
    CONSTRAINT chk_categories_not_self_parent CHECK (parent_id IS NULL OR parent_id <> id)
);

CREATE TABLE products(
    id BIGSERIAL PRIMARY KEY,
    public_id UUID UNIQUE NOT NULL,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    product_name VARCHAR(255) UNIQUE NOT NULL,
    slu VARCHAR(150) UNIQUE NOT NULL,
    description TEXT NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(150) NOT NULL,
    last_updated_by VARCHAR(150) NOT NULL,

    CONSTRAINT chk_products_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DRAFT', 'ARCHIVED')),
    CONSTRAINT chk_product_base_price CHECK (base_price >= 0)
);

CREATE TABLE product_variants(
    id BIGSERIAL PRIMARY KEY,
    public_id UUID UNIQUE NOT NULL,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    version BIGINT NOT NULL,
    sku VARCHAR(50) UNIQUE NOT NULL,
    color VARCHAR(50) NOT NULL,
    size VARCHAR(20) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_product_variants_price CHECK (price >= 0),
    CONSTRAINT chk_product_variants_stock_quantity CHECK (stock_quantity >= 0),
    CONSTRAINT uq_product_variants_combination UNIQUE(product_id, color, size)
);

CREATE TABLE product_images(
    id BIGSERIAL PRIMARY KEY,
    public_id UUID UNIQUE NOT NULL,
    product_variant_id BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    display_order INTEGER NOT NULL,
    primary_image BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_product_images_display_order CHECK (display_order >= 0)
);

CREATE TABLE category_images(
    id BIGSERIAL PRIMARY KEY,
    public_id UUID UNIQUE NOT NULL,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    display_order INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_category_images_display_order CHECK (display_order >= 0)
);

CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_active ON categories(active);
CREATE INDEX idx_categories_display_order ON categories(display_order);

CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_product_name ON products USING gin (product_name gin_trgm_ops);
CREATE INDEX idx_products_base_price ON products(base_price);
CREATE INDEX idx_products_created_by ON products(created_by);
CREATE INDEX idx_products_active_status ON products(id) WHERE status = 'ACTIVE';

CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);
CREATE INDEX idx_product_variants_sku ON product_variants(sku);
CREATE INDEX idx_product_variants_active ON product_variants(product_id) WHERE active = TRUE;
CREATE INDEX idx_product_variants_color ON product_variants(color);
CREATE INDEX idx_product_variants_size ON product_variants(size);

CREATE INDEX idx_product_images_product_variant_id ON product_images(product_variant_id);
CREATE INDEX idx_product_images_primary_image ON product_images(product_variant_id) WHERE primary_image = TRUE;
CREATE INDEX idx_product_images_storage_key ON product_images(storage_key);

CREATE INDEX idx_category_images_category_id ON category_images(category_id);
CREATE INDEX idx_category_images_storage_key ON category_images(storage_key);