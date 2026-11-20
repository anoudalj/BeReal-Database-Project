-- COMMENT operations (post 2 already has comment number 1 from user 3)

-- 1- INSERT: add a comment to post 2
INSERT INTO COMMENT
(PostID, CommentNumber, Content, TimePosted, UserID)
VALUES
(2, 2, 'Nice BeReal!', NOW(), 2);

-- 2. RETRIEVE: view the comments on a post
SELECT CommentNumber, Content, TimePosted, UserID
FROM COMMENT
WHERE PostID = 2
ORDER BY TimePosted ASC;

-- 3. DELETE: remove a comment (only by its user)
DELETE FROM COMMENT
WHERE PostID = 2
  AND CommentNumber = 2
  AND UserID = 2;
