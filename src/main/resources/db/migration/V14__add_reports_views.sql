CREATE OR REPLACE VIEW v_dashboard_stats AS
SELECT
    1 AS id,
    -- Solicitudes pendientes
    (SELECT COUNT(*)
     FROM reservations
     WHERE status = 'PENDING') AS pending_requests,

    (SELECT COUNT(*)
     FROM reservations
     WHERE status = 'PENDING'
       AND DATE(created_at) = CURRENT_DATE) AS pending_requests_today,

    COALESCE(ROUND(
                     (SELECT COUNT(*) FROM reservations WHERE status = 'PENDING')::numeric /
                     NULLIF((SELECT COUNT(*) FROM reservations), 0) * 100
                 , 1), 0) AS pending_requests_percentage,

    -- Diferencia pendientes vs ayer
    (SELECT COUNT(*) FROM reservations WHERE status = 'PENDING' AND DATE(created_at) = CURRENT_DATE) -
    (SELECT COUNT(*) FROM reservations WHERE status = 'PENDING' AND DATE(created_at) = CURRENT_DATE - INTERVAL '1 day')
        AS pending_requests_diff_yesterday,

    -- Espacios disponibles
    (SELECT COUNT(*)
     FROM spaces s
              JOIN reservables r ON s.id = r.id
     WHERE r.status = 'AVAILABLE'
       AND r.deleted_at IS NULL) AS available_spaces,

    (SELECT COUNT(*)
     FROM spaces s
              JOIN reservables r ON s.id = r.id
     WHERE r.deleted_at IS NULL) AS total_spaces,

    COALESCE(ROUND(
                     (SELECT COUNT(*) FROM spaces s JOIN reservables r ON s.id = r.id WHERE r.status = 'AVAILABLE' AND r.deleted_at IS NULL)::numeric /
                     NULLIF((SELECT COUNT(*) FROM spaces s JOIN reservables r ON s.id = r.id WHERE r.deleted_at IS NULL), 0) * 100
                 , 1), 0) AS available_spaces_percentage,

    -- Diferencia espacios disponibles vs ayer (via reservas aprobadas)
    (SELECT COUNT(*) FROM reservations WHERE status = 'APPROVED' AND "date" = CURRENT_DATE - INTERVAL '1 day') -
    (SELECT COUNT(*) FROM reservations WHERE status = 'APPROVED' AND "date" = CURRENT_DATE)
        AS available_spaces_diff_yesterday,

    -- Equipos en uso
    (SELECT COUNT(*)
     FROM equipments e
              JOIN reservables r ON e.id = r.id
     WHERE r.status = 'LOANED'
       AND r.deleted_at IS NULL) AS in_use_equipments,

    (SELECT COUNT(*)
     FROM equipments e
              JOIN reservables r ON e.id = r.id
     WHERE r.deleted_at IS NULL) AS total_equipments,

    COALESCE(ROUND(
                     (SELECT COUNT(*) FROM equipments e JOIN reservables r ON e.id = r.id WHERE r.status = 'LOANED' AND r.deleted_at IS NULL)::numeric /
                     NULLIF((SELECT COUNT(*) FROM equipments e JOIN reservables r ON e.id = r.id WHERE r.deleted_at IS NULL), 0) * 100
                 , 1), 0) AS in_use_equipments_percentage,

    -- Diferencia equipos en uso vs ayer
    (SELECT COUNT(*) FROM reservations r2
                              JOIN equipments e ON r2.reservable_id = e.id
     WHERE r2.status = 'LOANED' AND "date" = CURRENT_DATE) -
    (SELECT COUNT(*) FROM reservations r2
                              JOIN equipments e ON r2.reservable_id = e.id
     WHERE r2.status = 'LOANED' AND "date" = CURRENT_DATE - INTERVAL '1 day')
        AS in_use_equipments_diff_yesterday,

    -- Reservas hoy
    (SELECT COUNT(*)
     FROM reservations
     WHERE "date" = CURRENT_DATE) AS today_reservations,

    -- Promedio diario últimos 30 días
    COALESCE((
                 SELECT ROUND(AVG(daily_count))
                 FROM (
                          SELECT COUNT(*) AS daily_count
                          FROM reservations
                          WHERE "date" >= CURRENT_DATE - INTERVAL '30 days'
                          GROUP BY "date"
                      ) daily
             ), 0) AS avg_daily_reservations_30d,

    -- Diferencia reservas hoy vs promedio
    (SELECT COUNT(*) FROM reservations WHERE "date" = CURRENT_DATE) -
    COALESCE((
                 SELECT ROUND(AVG(daily_count))
                 FROM (
                          SELECT COUNT(*) AS daily_count
                          FROM reservations
                          WHERE "date" >= CURRENT_DATE - INTERVAL '30 days'
                          GROUP BY "date"
                      ) daily
             ), 0) AS today_reservations_diff_avg,

    -- Reservas este mes
    (SELECT COUNT(*)
     FROM reservations
     WHERE EXTRACT(YEAR FROM "date") = EXTRACT(YEAR FROM CURRENT_DATE)
       AND EXTRACT(MONTH FROM "date") = EXTRACT(MONTH FROM CURRENT_DATE)
    ) AS reservations_this_month;



CREATE OR REPLACE VIEW v_resource_stats AS
SELECT
    r.id     AS reservable_id,
    r.name   AS resource_name,
    r.status AS resource_status,
    CASE WHEN s.id IS NOT NULL THEN 'SPACE' ELSE 'EQUIPMENT' END AS resource_type,

    COUNT(res.id) AS total_reservations,

    COUNT(res.id) FILTER (
        WHERE EXTRACT(YEAR  FROM res."date") = EXTRACT(YEAR  FROM CURRENT_DATE)
            AND EXTRACT(MONTH FROM res."date") = EXTRACT(MONTH FROM CURRENT_DATE)
        ) AS reservations_this_month,

    COALESCE(ROUND(
                             COUNT(res.id) FILTER (WHERE res.status = 'APPROVED')::numeric /
                             NULLIF(COUNT(res.id), 0) * 100
                 , 1), 0) AS occupancy_rate,

    ROUND(
            EXTRACT(EPOCH FROM (MAX(res.start_time) - MIN(res.start_time))) /
            NULLIF(COUNT(res.id) - 1, 0) / 86400
        , 1) AS avg_days_between_reservations

FROM reservables r
         LEFT JOIN spaces s ON s.id = r.id
         LEFT JOIN equipments e ON e.id = r.id
         LEFT JOIN reservations res ON res.reservable_id = r.id
WHERE r.deleted_at IS NULL
GROUP BY r.id, r.name, r.status, s.id;