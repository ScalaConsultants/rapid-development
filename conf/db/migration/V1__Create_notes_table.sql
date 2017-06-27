CREATE TABLE notes (
  id UUID PRIMARY KEY,
  creator VARCHAR(100) NOT NULL,
  note TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  version INTEGER NOT NULL
);

CREATE INDEX idx_notes_id_u ON notes(id);
CREATE INDEX idx_notes_creator ON notes(creator);

-- Initial data
INSERT INTO notes(id, creator, note, created_at, updated_at, version) VALUES
  ('af0b3c12-205b-4142-b4a0-b0edba88abfb', 'Leszek', 'Some useful note', now(), now(), 1),
  ('5d1dabeb-6f1c-4717-b9e5-49df2651ad25', 'Grzesiek', 'Some more useful note', now(), now(), 1),
  ('381be025-213a-41b1-b313-924ff7b18c20', 'Zdzichu', 'Some even more useful note', now(), now(), 1),
  ('77ac532f-61e6-443e-a8e6-61f17595ee92', 'Rychu', 'Some useful note with emoji 🎉', now(), now(), 1),
  ('e0b84828-4c62-487c-8808-502682cd886c', 'Zenek', 'Money money money 💸', now(), now(), 1),
  ('46db0494-9df7-4c32-b67c-912200b38e14', 'Zbychu', 'Rule the world!', now(), now(), 1),
  ('e846944f-1c94-4390-9a6b-c40de5a43a78', 'Miłosz', 'I have a cool name', now(), now(), 1),
  ('8a3355ba-5fad-44bd-8eeb-e19369b652db', 'Franek', 'Do you know Grzegorz Brzęczyszczykiewicz?', now(), now(), 1);
