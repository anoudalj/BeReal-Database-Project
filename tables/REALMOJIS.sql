-- REALMOJIS operations (RealMojis 18 and 19 already exist in the dump)

-- 1. INSERT: add a new RealMoji
INSERT INTO REALMOJIS
(Emoji, Image, UserID)
VALUES
('Wow', 'wow.jpg', 2);

-- 2. RETRIEVE: view the RealMojis made by a user
SELECT RealMojiID, Emoji, Image, UserID
FROM REALMOJIS
WHERE UserID = 2
ORDER BY RealMojiID ASC;

-- 3. UPDATE: rename an existing RealMoji
UPDATE REALMOJIS
SET Emoji = 'Loving'
WHERE RealMojiID = 19
  AND UserID = 2;

-- 4. DELETE: remove the RealMoji inserted in step 1
DELETE FROM REALMOJIS
WHERE Emoji = 'Wow'
  AND UserID = 2;
