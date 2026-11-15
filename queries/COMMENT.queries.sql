-- comment operations
-- 1 insert: add a comment to post 
INSERT INTO COMMENT
(PostID, CommentNumber, Content, TimePosted, UserID)
VALUES
(2, 1, 'Nice BeReal!', NOW(), 2);

-- 2 delete: remove a comment 
DELETE FROM COMMENT
WHERE PostID = 2
  AND CommentNumber = 1
  AND UserID = 2;
-- 3 retrive comments on a post

SELECT CommentNumber, Content, TimePosted, UserID
FROM COMMENT
WHERE PostID = 2
ORDER BY TimePosted ASC;
  
  
