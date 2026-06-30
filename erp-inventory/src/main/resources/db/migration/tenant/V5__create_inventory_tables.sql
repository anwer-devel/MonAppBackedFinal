-- Migration V5: Create inventory tables in tenant schema

CREATE TABLE IF NOT EXISTS stock_entries (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  product_id    UUID NOT NULL,
  local_id      UUID NOT NULL,
  quantity      NUMERIC(14,3) NOT NULL DEFAULT 0,
  reserved_qty  NUMERIC(14,3) NOT NULL DEFAULT 0,
  avg_unit_cost NUMERIC(14,4) NOT NULL DEFAULT 0,
  last_movement_at TIMESTAMP,
  created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
  is_deleted    BOOLEAN   NOT NULL DEFAULT FALSE,
  UNIQUE(product_id, local_id)
);

CREATE TABLE IF NOT EXISTS stock_movements (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  product_id      UUID NOT NULL,
  local_id        UUID NOT NULL,
  type            VARCHAR(20) NOT NULL,
  quantity        NUMERIC(14,3) NOT NULL,
  unit_cost       NUMERIC(14,4),
  balance_after   NUMERIC(14,3) NOT NULL,
  reason          VARCHAR(255),
  reference_type  VARCHAR(50),
  reference_id    UUID,
  target_local_id UUID,
  lot_id          UUID,
  created_by      UUID,
  created_by_name VARCHAR(200),
  created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
  is_deleted      BOOLEAN   NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS stock_lots (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  product_id       UUID NOT NULL,
  local_id         UUID NOT NULL,
  lot_number       VARCHAR(100) NOT NULL,
  quantity         NUMERIC(14,3) NOT NULL DEFAULT 0,
  expiry_date      DATE,
  manufacture_date DATE,
  supplier_ref     VARCHAR(100),
  unit_cost        NUMERIC(14,4),
  created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
  is_deleted       BOOLEAN   NOT NULL DEFAULT FALSE,
  UNIQUE(product_id, local_id, lot_number)
);

CREATE TABLE IF NOT EXISTS stock_alerts (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  product_id      UUID NOT NULL,
  local_id        UUID NOT NULL,
  type            VARCHAR(20) NOT NULL,
  severity        VARCHAR(20) NOT NULL,
  current_stock   NUMERIC(14,3),
  threshold       NUMERIC(14,3),
  expiry_date     DATE,
  lot_id          UUID,
  is_resolved     BOOLEAN NOT NULL DEFAULT FALSE,
  resolved_at     TIMESTAMP,
  created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
  is_deleted      BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS physical_inventories (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  local_id      UUID NOT NULL,
  status        VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
  type          VARCHAR(20) NOT NULL DEFAULT 'PARTIAL',
  started_at    TIMESTAMP NOT NULL DEFAULT NOW(),
  completed_at  TIMESTAMP,
  created_by    UUID,
  total_items   INT NOT NULL DEFAULT 0,
  total_variance_value NUMERIC(14,2) DEFAULT 0,
  created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
  is_deleted    BOOLEAN   NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS physical_inventory_lines (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  inventory_id     UUID NOT NULL
    REFERENCES physical_inventories(id) ON DELETE CASCADE,
  product_id       UUID NOT NULL,
  expected_qty     NUMERIC(14,3) NOT NULL,
  counted_qty      NUMERIC(14,3),
  variance_qty     NUMERIC(14,3),
  is_counted       BOOLEAN NOT NULL DEFAULT FALSE,
  created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
  is_deleted       BOOLEAN   NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_stock_entries_product
  ON stock_entries(product_id);
CREATE INDEX IF NOT EXISTS idx_stock_entries_local
  ON stock_entries(local_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_product_local
  ON stock_movements(product_id, local_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_created
  ON stock_movements(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_stock_movements_reference
  ON stock_movements(reference_type, reference_id);
CREATE INDEX IF NOT EXISTS idx_stock_lots_expiry
  ON stock_lots(expiry_date) WHERE expiry_date IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_stock_alerts_unresolved
  ON stock_alerts(is_resolved) WHERE is_resolved = FALSE;
CREATE INDEX IF NOT EXISTS idx_inventory_lines_inventory
  ON physical_inventory_lines(inventory_id);
