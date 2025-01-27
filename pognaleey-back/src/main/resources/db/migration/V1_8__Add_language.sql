ALTER TABLE travel_guides
    ADD COLUMN language VARCHAR(2);

UPDATE travel_guides
SET language = 'RU'
WHERE language IS NULL;

ALTER TABLE travel_guides
    ALTER COLUMN language SET NOT NULL;