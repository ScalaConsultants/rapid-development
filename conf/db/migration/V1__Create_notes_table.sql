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
