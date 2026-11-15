-- REALMOJIS operations

-- 1 insert a new real moji
INSERT INTO REALMOJIS
(Emoji, Image, UserID)
VALUES
('Happy', 'happy.jpg', 2);
INSERT INTO REALMOJIS
(Emoji, Image, UserID)
VALUES
('Hearts', 'hearts.jpg', 2);

-- 2 retrieve realmojis made by user
SELECT RealMojiID, Emoji, Image, UserID
FROM REALMOJIS
WHERE UserID = 2
ORDER BY RealMojiID ASC;

-- 3 update realmojis
UPDATE REALMOJIS
SET Emoji = 'Laughing'
WHERE RealMojiID = 1
  AND UserID = 2;
  
  -- 4 delete a realmoji
DELETE FROM REALMOJIS
WHERE RealMojiID = 1
  AND UserID = 2;
