-- POST operations

-- 1. INSERT : add a new post (user 2, moment 2)
INSERT INTO POST
(FrontCam, BackCam, Caption, RetakeCount, HoursLate, PostedAt, UserID, MomentID)
VALUES
('front3.jpg', 'back3.jpg', 'Third BeReal', 0, 0.00, '2026-11-13 22:03:00', 2, 2);

-- 2. UPDATE: change the caption of  existing post
UPDATE POST
SET Caption = 'Updated BeReal caption'
WHERE PostID = 2;

-- 3. RETRIEVE: view all posts
SELECT * FROM POST;

-- 4. RETREVE : retake count of a specific post belonging to a specific user
SELECT RetakeCount
FROM POST
WHERE UserID = 3
  AND PostID = 2;

-- 5 DELETE: remove the post inserted in step 1
DELETE FROM POST
WHERE Caption = 'Third BeReal'
  AND UserID = 2;
