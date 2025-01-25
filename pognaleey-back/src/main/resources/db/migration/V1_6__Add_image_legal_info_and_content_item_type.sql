ALTER TABLE images
    ADD COLUMN owner_name VARCHAR;

ALTER TABLE images
    ADD COLUMN owner_url VARCHAR;

ALTER TABLE images
    ADD COLUMN licence_url VARCHAR;

ALTER TABLE travel_guide_content_items
    ADD COLUMN type VARCHAR;

UPDATE travel_guide_content_items
SET type = 'MARKDOWN'
WHERE type IS NULL;

ALTER TABLE travel_guide_content_items
    ALTER COLUMN type SET NOT NULL;