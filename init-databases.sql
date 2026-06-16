CREATE DATABASE listas_espera_db;
CREATE DATABASE portal_paciente_db;
CREATE DATABASE reasignacion_db;
CREATE DATABASE usuarios_db;
CREATE DATABASE notificaciones_db;
CREATE DATABASE auditoria_db;

\c auditoria_db;

CREATE TABLE atenciones (
    id SERIAL PRIMARY KEY,
    estado VARCHAR(50),
    prioridad INTEGER
);

CREATE OR REPLACE FUNCTION sp_calcular_estadisticas_espera()
RETURNS TABLE(prioridad INT, cantidad BIGINT)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT a.prioridad, COUNT(*)
    FROM atenciones a
    WHERE a.estado = 'EN_ESPERA'
    GROUP BY a.prioridad;
END;
$$;
