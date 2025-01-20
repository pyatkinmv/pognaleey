ALTER TABLE travel_recommendations
    RENAME COLUMN short_description to image_search_phrase;

ALTER TABLE travel_recommendations
    ADD COLUMN status VARCHAR;

UPDATE travel_recommendations
set status = CASE
                 WHEN details IS NOT NULL AND image_url IS NOT NULL THEN 'READY'
                 ELSE 'FAILED'
    END;

ALTER TABLE travel_recommendations
    ALTER COLUMN status SET NOT NULL;