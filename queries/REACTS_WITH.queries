-- REACTS_WITH queries
-- 1 insert: add a new reaction on a post 
INSERT INTO REACTS_WITH
(UserID, PostID, RealMojiID)
VALUES
(2, 2, 18);

-- 2 retrieve: view reactions on post 2
SELECT UserID, PostID, RealMojiID
FROM REACTS_WITH
WHERE PostID = 2;

--3 delete a reaction on a post
DELETE FROM REACTS_WITH
WHERE UserID = 2
  AND PostID = 2
  AND RealMojiID = 18;
