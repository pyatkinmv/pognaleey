ALTER TABLE images
    ALTER COLUMN url DROP NOT NULL,
    ALTER COLUMN thumbnail_url DROP NOT NULL;

UPDATE travel_guide_content_items
SET content = jsonb_set(
        content::jsonb,
        '{imageId}',
        to_jsonb((content::jsonb ->> 'id')::integer)
              )
WHERE type = 'IMAGE'
  AND content::jsonb ->> 'id' IS NOT NULL;