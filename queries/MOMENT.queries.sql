-- MOMENT operations (moment 2 is used by post 2, so it is left untouched)

-- 1. INSERT: add a new moment
INSERT INTO MOMENT
(MomentID, Time, TimeZone, Date)
VALUES
(3, '21:00:00', 'Asia/Riyadh', '2026-11-14');

-- 2. UPDATE: change the time of the new moment
UPDATE MOMENT
SET Time = '21:15:00'
WHERE MomentID = 3;

-- 3. RETRIEVE: view all moments
SELECT * FROM MOMENT;

-- 4. DELETE: remove the new moment (no post uses it)
DELETE FROM MOMENT
WHERE MomentID = 3;
