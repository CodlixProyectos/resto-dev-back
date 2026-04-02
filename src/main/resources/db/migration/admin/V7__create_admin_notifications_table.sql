CREATE TABLE admin.notifications (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL, -- success, warning, info, error
    related_id VARCHAR(255),
    related_type VARCHAR(50),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_notifications_org FOREIGN KEY (organization_id) REFERENCES admin.organizations(id)
);

CREATE INDEX idx_notifications_org_read ON admin.notifications(organization_id, is_read);
CREATE INDEX idx_notifications_created_at ON admin.notifications(created_at DESC);
