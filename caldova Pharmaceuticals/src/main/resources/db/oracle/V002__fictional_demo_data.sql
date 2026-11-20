DECLARE
    TYPE texts IS TABLE OF VARCHAR2(2000);
    plant_codes texts := texts('NB', 'WH', 'EM', 'SW');
    plant_names texts := texts('Northbridge', 'Westhaven', 'Eastmere', 'Southwick');
    processes texts := texts('Tablet packaging', 'Blister packing', 'Bottle labelling', 'Secondary packaging', 'Case packing');
    signal_names texts := texts('Carton-present sensor', 'Infeed speed', 'Guard circuit');
    check_names texts := texts('Material availability', 'Line clearance', 'Start permission');
    check_results texts := texts('Recorded ready', 'Recorded complete', 'Not recorded');
    manual_refs texts := texts('CP-12 / 4.2', 'CP-12 / 2.1', 'CP-12 / 1.1');
    manual_titles texts := texts('Carton feed alarm', 'Signal history', 'Demo limitations');
    manual_text texts := texts(
        'ALM-204 records that the carton-present signal was not detected during the recorded feed cycle.',
        'Signal values describe the last stored sample. They do not confirm the current equipment condition.',
        'Fictional reference text for this software demonstration only. Not an equipment operating or safety procedure.');
    v_id NUMBER;
    v_child NUMBER;
    v_code VARCHAR2(12);
    v_status VARCHAR2(12);
    v_process VARCHAR2(80);
    v_issue VARCHAR2(200);
    v_alarm VARCHAR2(20);
    v_message VARCHAR2(200);
    v_severity VARCHAR2(20);
    v_signal VARCHAR2(80);
    v_unit VARCHAR2(30);
    v_time TIMESTAMP;
BEGIN
    FOR p IN 1..4 LOOP
        MERGE INTO CALDOVA_OWNER.plant d USING (SELECT p id FROM dual) s ON (d.plant_id = s.id)
        WHEN MATCHED THEN UPDATE SET plant_code = plant_codes(p), plant_name = plant_names(p)
        WHEN NOT MATCHED THEN INSERT (plant_id, plant_code, plant_name)
        VALUES (p, plant_codes(p), plant_names(p));

        FOR n IN 1..40 LOOP
            v_id := (p - 1) * 40 + n;
            v_code := plant_codes(p) || '-' || TO_CHAR(n, 'FM000');
            v_process := processes(MOD(n + p - 2, 5) + 1);
            IF n >= 39 OR (n = 1 AND p IN (1, 4)) OR (n = 38 AND p IN (2, 3)) THEN
                v_status := 'STOPPED';
            ELSIF n BETWEEN 32 AND 37 OR (p = 2 AND n = 1) THEN
                v_status := 'ATTENTION';
            ELSE
                v_status := 'RUNNING';
            END IF;
            IF p = 2 AND n = 37 THEN v_status := 'RUNNING'; END IF;
            IF p = 4 AND n = 1 THEN v_process := 'Case packing'; END IF;
            IF p = 2 AND n = 1 THEN v_process := 'Bottle labelling'; END IF;
            IF p = 3 AND n = 1 THEN v_process := 'Secondary packaging'; END IF;
            v_issue := CASE v_status
                WHEN 'RUNNING' THEN 'No active alarm recorded'
                WHEN 'ATTENTION' THEN 'ALM-118: Label registration variation'
                ELSE 'ALM-204: Carton feed not detected' END;
            IF p = 4 AND n = 1 THEN v_issue := 'ALM-307: Outfeed accumulation'; END IF;
            MERGE INTO CALDOVA_OWNER.production_line d USING (SELECT v_id id FROM dual) s ON (d.line_id=s.id)
            WHEN MATCHED THEN UPDATE SET plant_id=p, line_code=v_code, process_name=v_process,
                equipment_ref='EQ-' || v_code, line_status=v_status,
                snapshot_time=TIMESTAMP '2026-11-19 14:20:00', shifts_per_day=2, last_issue=v_issue
            WHEN NOT MATCHED THEN INSERT
                (line_id,plant_id,line_code,process_name,equipment_ref,line_status,snapshot_time,shifts_per_day,last_issue)
                VALUES (v_id,p,v_code,v_process,'EQ-' || v_code,v_status,TIMESTAMP '2026-11-19 14:20:00',2,v_issue);
            FOR k IN 1..3 LOOP
                v_child := v_id * 10 + k;
                v_time := CASE k WHEN 1 THEN TIMESTAMP '2026-11-19 14:18:50'
                    WHEN 2 THEN TIMESTAMP '2026-11-19 14:18:12' ELSE TIMESTAMP '2026-11-19 13:47:03' END;
                v_alarm := CASE WHEN k=2 THEN 'ALM-091' WHEN v_status='ATTENTION' THEN 'ALM-118'
                    WHEN v_id=121 THEN 'ALM-307' ELSE 'ALM-204' END;
                v_message := CASE v_alarm WHEN 'ALM-091' THEN 'Infeed spacing variation'
                    WHEN 'ALM-118' THEN 'Label registration variation'
                    WHEN 'ALM-307' THEN 'Outfeed accumulation' ELSE 'Carton feed not detected' END;
                v_severity := CASE WHEN k=2 OR v_status='ATTENTION' THEN 'Warning' ELSE 'Stop' END;
                MERGE INTO CALDOVA_OWNER.alarm d USING (SELECT v_child id FROM dual) s ON (d.alarm_id=s.id)
                WHEN MATCHED THEN UPDATE SET recorded_time=v_time, alarm_code=v_alarm,
                    severity=v_severity,alarm_message=v_message
                WHEN NOT MATCHED THEN INSERT VALUES (v_child,v_id,v_time,v_alarm,v_severity,v_message);

                v_signal := CASE k WHEN 1 THEN CASE WHEN v_status='STOPPED' THEN 'Not detected' ELSE 'Detected' END
                    WHEN 2 THEN CASE WHEN v_status='STOPPED' THEN '0' ELSE '120' END ELSE 'Closed' END;
                v_unit := CASE WHEN k=2 THEN 'cartons/min' ELSE NULL END;
                MERGE INTO CALDOVA_OWNER.equipment_signal d USING (SELECT v_child id FROM dual) s ON (d.signal_id=s.id)
                WHEN MATCHED THEN UPDATE SET signal_name=signal_names(k),recorded_value=v_signal,unit=v_unit,
                    recorded_time=TIMESTAMP '2026-11-19 14:18:50'
                WHEN NOT MATCHED THEN INSERT VALUES
                    (v_child,v_id,signal_names(k),v_signal,v_unit,TIMESTAMP '2026-11-19 14:18:50');
                MERGE INTO CALDOVA_OWNER.readiness_check d USING (SELECT v_child id FROM dual) s ON (d.check_id=s.id)
                WHEN MATCHED THEN UPDATE SET check_name=check_names(k),recorded_result=check_results(k),
                    recorded_time=CASE WHEN k=3 THEN TIMESTAMP '2026-11-19 14:18:50' ELSE TIMESTAMP '2026-11-19 14:00:00' END
                WHEN NOT MATCHED THEN INSERT VALUES (v_child,v_id,check_names(k),check_results(k),
                    CASE WHEN k=3 THEN TIMESTAMP '2026-11-19 14:18:50' ELSE TIMESTAMP '2026-11-19 14:00:00' END);
                MERGE INTO CALDOVA_OWNER.manual_excerpt d USING (SELECT v_child id FROM dual) s ON (d.excerpt_id=s.id)
                WHEN MATCHED THEN UPDATE SET document_ref=manual_refs(k),title=manual_titles(k),
                    revision='Demo A',excerpt=manual_text(k)
                WHEN NOT MATCHED THEN INSERT VALUES (v_child,v_id,manual_refs(k),manual_titles(k),'Demo A',manual_text(k));
            END LOOP;
        END LOOP;
    END LOOP;
END;
/
COMMIT;
