ALTER TABLE pedidos
DROP CHECK chk_pedidos_estado,
ADD CONSTRAINT chk_pedidos_estado
CHECK (
    estado IN (
        'pendiente',
        'pagado',
        'enviado',
        'entregado',
        'cancelado'
    )
);

CREATE INDEX idx_pedidos_usuario_estado
ON pedidos (usuario_id, estado);