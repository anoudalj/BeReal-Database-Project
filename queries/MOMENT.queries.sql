-- Retrieve all moments
SELECT * FROM MOMENT;

-- Update a moment time
UPDATE MOMENT
SET Time = '21:15:00'
WHERE MomentID = 1;

-- Insert a new moment
INSERT INTO MOMENT
(MomentID, Time, TimeZone, Date)
VALUES
(2, '22:00:00', 'Asia/Riyadh', '2026-11-13');

-- Delete a moment
DELETE FROM MOMENT
WHERE MomentID = 2;
