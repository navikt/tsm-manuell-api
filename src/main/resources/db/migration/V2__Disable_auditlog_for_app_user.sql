DO
$$
BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'tsm-manuell-api-instance')
        THEN
            BEGIN
                ALTER USER "tsm-manuell-api-instance" IN DATABASE "tsm-manuell-api" SET pgaudit.log TO 'none';
                RAISE NOTICE 'Successfully disabled pgaudit for tsm-manuell-api-instance';
            EXCEPTION
                WHEN insufficient_privilege THEN
                    RAISE WARNING 'Cannot set pgaudit.log: insufficient privileges. This is expected in dev/test environments.';
                WHEN OTHERS THEN
                    RAISE WARNING 'Cannot set pgaudit.log: % (SQLSTATE: %). This is expected if pgaudit extension is not installed.', SQLERRM, SQLSTATE;
            END;
END IF;
END
$$;
