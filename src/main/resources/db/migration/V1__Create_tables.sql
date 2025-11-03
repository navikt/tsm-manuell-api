-- MANUELLOPPGAVE table
CREATE TABLE manuelloppgave (
    id VARCHAR PRIMARY KEY,
    sykmelding JSONB NOT NULL,
    pasientIdent VARCHAR NOT NULL,
    ferdigstilt BOOLEAN NOT NULL,
    oppgaveid INTEGER,
    status VARCHAR,
    status_timestamp TIMESTAMP WITH TIME ZONE
);

-- Indexes for MANUELLOPPGAVE
CREATE INDEX manuelloppgave_oppgaveid_idx ON manuelloppgave (oppgaveid);
CREATE INDEX manuelloppgave_sykmelding_id_index ON manuelloppgave ((sykmelding->>'id'));
