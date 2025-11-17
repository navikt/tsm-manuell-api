-- Remove oppgaveid column and index
DROP INDEX IF EXISTS manuelloppgave_oppgaveid_idx;
ALTER TABLE manuelloppgave DROP COLUMN oppgaveid;
