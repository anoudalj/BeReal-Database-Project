-- Retrieve all posts from the POST table
SELECT * FROM POST;


-- Update the caption of the post 
UPDATE POST
SET Caption = 'Updated BeReal caption'
WHERE PostID = 1;



-- Insert a new post into the POST table
INSERT INTO POST
(FrontCam, BackCam, Caption, RetakeCount, HoursLate, PostedAt, UserID, MomentID)
VALUES
('front2.jpg', 'back2.jpg', 'Second BeReal', 0, 0.00, '2026-11-12 21:35:00', 2, 1);


-- Delete a post
DELETE FROM POST
WHERE PostID = 2;
