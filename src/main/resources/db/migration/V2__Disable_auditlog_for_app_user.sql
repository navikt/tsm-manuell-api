CREATE EXTENSION IF NOT EXISTS pgaudit;
DO
$$
BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'tsm-manuell-api-instance')
        THEN
            ALTER USER "tsm-manuell-api-instance" IN DATABASE "tsm-manuell-api" SET pgaudit.log TO 'none';
END IF;
END
$$;
