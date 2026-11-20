-- REACTS_WITH operations

-- 1 INSERT: user 2 reacts to post 2 with RealMoji 18
INSERT INTO REACTS_WITH
(UserID, PostID, RealMojiID)
VALUES
(2, 2, 18);

-- 2 RETRIEVE: view the reactions on post 2
SELECT UserID, PostID, RealMojiID
FROM REACTS_WITH
WHERE PostID = 2;

-- 3. DELETE : remove a reaction from a post
DELETE FROM REACTS_WITH
WHERE UserID = 2
  AND PostID = 2
  AND RealMojiID = 18;
