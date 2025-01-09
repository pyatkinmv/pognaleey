ALTER TABLE travel_inquiries
    ADD COLUMN user_id BIGINT REFERENCES users (id);

CREATE TABLE travel_guides
(
    id                INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    recommendation_id BIGINT    NOT NULL REFERENCES travel_recommendations (id),
    user_id           BIGINT REFERENCES users (id),
    title             VARCHAR   NOT NULL,
    details           TEXT      NOT NULL,
    image_url         VARCHAR   NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT recommendation_user UNIQUE (user_id, recommendation_id)
);

CREATE TABLE travel_guides_likes
(
    id         INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    guide_id   BIGINT    NOT NULL REFERENCES travel_guides (id) ON DELETE CASCADE,
    user_id    BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT user_guide UNIQUE (user_id, guide_id)
);
