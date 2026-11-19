-- ADMIN operations: moderating a spam account
-- Run top to bottom. Part 0 creates the demo data that the admin works on.

-- 0. SETUP: a spam user with one post and one comment
INSERT INTO `USER`
(Email, Username, Password, Bio, DOB, Account_Type, UserType, JoinDate, ProfilePicture, AccountStatus)
VALUES
('spam@example.com', 'spammer99', 'DemoPassword000', 'Follow me', '2002-02-02', 'Public', 'Regular', CURDATE(), NULL, 'Active');

INSERT INTO POST
(FrontCam, BackCam, Caption, RetakeCount, HoursLate, PostedAt, UserID, MomentID)
SELECT 'spam_front.jpg', 'spam_back.jpg', 'Buy followers now', 0, 0.00, '2026-11-13 22:05:00', UserID, 2
FROM `USER` WHERE Username = 'spammer99';

INSERT INTO COMMENT
(PostID, CommentNumber, Content, TimePosted, UserID)
SELECT 2, 50, 'Check my profile for free followers', NOW(), UserID
FROM `USER` WHERE Username = 'spammer99';


-- 1. UPDATE: suspend the spam account
UPDATE `USER`
SET AccountStatus = 'Suspended'
WHERE Username = 'spammer99';

-- 2. UPDATE: hide the spam caption
UPDATE POST
SET Caption = '[Removed by admin]'
WHERE Caption = 'Buy followers now';

-- 3. SINGLE-TABLE RETRIEVE: list all suspended accounts
SELECT UserID, Username, Email, AccountStatus
FROM `USER`
WHERE AccountStatus = 'Suspended';

-- 4. TWO-TABLE JOIN: posts written by suspended accounts
SELECT U.Username, P.PostID, P.Caption, P.PostedAt
FROM `USER` U
JOIN POST P
    ON U.UserID = P.UserID
WHERE U.AccountStatus = 'Suspended';

-- 5. THREE-TABLE JOIN: comments by suspended accounts, with the post owner
SELECT C.PostID, C.CommentNumber, C.Content,
       U.Username AS Commenter, PO.Username AS PostOwner
FROM COMMENT C
JOIN `USER` U
    ON C.UserID = U.UserID
JOIN POST P
    ON C.PostID = P.PostID
JOIN `USER` PO
    ON P.UserID = PO.UserID
WHERE U.AccountStatus = 'Suspended';

-- 6. DELETE: remove the spam comment
DELETE FROM COMMENT
WHERE UserID = (SELECT UserID FROM `USER` WHERE Username = 'spammer99');

-- 7. DELETE: remove the spam post
DELETE FROM POST
WHERE UserID = (SELECT UserID FROM `USER` WHERE Username = 'spammer99');

-- 8. CLEANUP (optional, so the script can be run again): remove the spam user
DELETE FROM `USER`
WHERE Username = 'spammer99';
