-- Añadimos la columna registration_status para rastrear el progreso del provisionamiento asíncrono.
-- Estados posibles: PENDING_SETUP, ACTIVE, FAILED_SETUP
ALTER TABLE admin.organizations ADD COLUMN registration_status VARCHAR(30) DEFAULT 'ACTIVE';

-- Actualizamos los registros existentes a ACTIVE
UPDATE admin.organizations SET registration_status = 'ACTIVE';
