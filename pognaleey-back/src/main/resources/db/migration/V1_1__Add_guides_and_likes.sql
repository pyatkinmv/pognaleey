ALTER TABLE travel_inquiries
    ADD COLUMN user_id BIGINT REFERENCES users (id);

CREATE TABLE travel_guides
(
    id                INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    recommendation_id BIGINT    NOT NULL UNIQUE REFERENCES travel_recommendations (id),
    user_id           BIGINT REFERENCES users (id),
    title   VARCHAR,
    details TEXT,
    image_url         VARCHAR   NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_travel_guides_user_id ON travel_guides (user_id);

CREATE TABLE travel_guides_likes
(
    id         INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    guide_id   BIGINT    NOT NULL REFERENCES travel_guides (id) ON DELETE CASCADE,
    user_id    BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT user_guide UNIQUE (user_id, guide_id)
);
