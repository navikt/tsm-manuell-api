DO
$$
BEGIN
    -- Only create extension if it's available
    IF EXISTS (SELECT 1 FROM pg_available_extensions WHERE name = 'pgaudit') THEN
        CREATE EXTENSION IF NOT EXISTS pgaudit;
        
        -- Configure the user if it exists
        IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'tsm-manuell-api-instance') THEN
            ALTER USER "tsm-manuell-api-instance" IN DATABASE "tsm-manuell-api" SET pgaudit.log TO 'none';
        END IF;
    END IF;
END
$$;
