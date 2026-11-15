-- TAGGED_ON queries

--  1 insert a new tag on a post; Tag a user in a post
INSERT INTO TAGGED_ON
(UserID, PostID)
VALUES
(2, 1);


-- 2 Delete a tag
DELETE FROM TAGGED_ON
WHERE UserID = 2
  AND PostID = 1;


-- 3 retrive tagged users on a post
SELECT UserID, PostID
FROM TAGGED_ON
WHERE PostID = 1
ORDER BY UserID ASC;
