-- TAGGED_ON operations

-- 1. INSERT: tag user 2 on post 2
INSERT INTO TAGGED_ON
(UserID, PostID)
VALUES
(2, 2);

-- 2. RETRIEVE: view the tagged users on a post
SELECT UserID, PostID
FROM TAGGED_ON
WHERE PostID = 2
ORDER BY UserID ASC;

-- 3. DELETE: remove a tag
DELETE FROM TAGGED_ON
WHERE UserID = 2
  AND PostID = 2;
