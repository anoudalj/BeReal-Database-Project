-- ADMIN operations (same operations as "AS ADMIN" in the Phase 2 report)
-- Run top to bottom on the data in Bereal_database.sql.
-- Part 0 creates the demo data the administrator works on; the last part cleans it up.

-- 0. SETUP: a reported user with a post, a RealMoji and a reaction
INSERT INTO `USER`
(Email, Username, Password, Bio, DOB, Account_Type, UserType, JoinDate, ProfilePicture, AccountStatus)
VALUES
('spam@example.com', 'spammer99', 'DemoPassword000', 'Follow me', '1985-03-10', 'Public', 'Regular', CURDATE(), NULL, 'UnderReview');

INSERT INTO POST
(FrontCam, BackCam, Caption, RetakeCount, HoursLate, PostedAt, UserID, MomentID)
SELECT 'spam_front.jpg', 'spam_back.jpg', 'Buy followers now', 0, 0.00, '2026-11-13 22:05:00', UserID, 2
FROM `USER` WHERE Username = 'spammer99';

INSERT INTO REALMOJIS (Emoji, Image, UserID)
SELECT 'Spam', 'spam.jpg', UserID
FROM `USER` WHERE Username = 'spammer99';

INSERT INTO REACTS_WITH (UserID, PostID, RealMojiID)
SELECT 2, 2, RealMojiID
FROM REALMOJIS
WHERE Emoji = 'Spam'
  AND UserID = (SELECT UserID FROM `USER` WHERE Username = 'spammer99');


-- ===================== INSERT =====================

-- Insert 1: add a BeReal notification time for a specific time zone
INSERT INTO MOMENT
(MomentID, Time, TimeZone, Date)
VALUES
(124, '14:30:00', 'Europe', '2026-11-20');

-- Insert 2: restore a tagging record that was removed by mistake
INSERT INTO TAGGED_ON
(UserID, PostID)
VALUES
(2, 2);


-- ===================== RETRIEVE (ONE TABLE) =====================

-- Retrieve 1: all accounts currently marked under review
SELECT UserID, Username, Email, AccountStatus
FROM `USER`
WHERE AccountStatus = 'UnderReview';

-- Retrieve 2: ID, username, email and date of birth of users over 35
SELECT UserID, Username, Email, DOB
FROM `USER`
WHERE TIMESTAMPDIFF(YEAR, DOB, CURDATE()) > 35;


-- ===================== RETRIEVE (JOINS) =====================

-- Join (two tables): pending friendship requests with the requester's username
SELECT F.Friendship_id, U.Username AS Requester, F.Receiver_ID, F.Date_Requested
FROM FRIENDSHIP F
JOIN `USER` U
    ON F.Requester_ID = U.UserID
WHERE F.Status = 'Pending';

-- Join (three tables): reactions made with RealMojis designed by the reported user
SELECT RW.UserID AS Reactor, RW.PostID, P.PostedAt, R.Emoji
FROM REACTS_WITH RW
JOIN POST P
    ON RW.PostID = P.PostID
JOIN REALMOJIS R
    ON RW.RealMojiID = R.RealMojiID
WHERE R.UserID = (SELECT UserID FROM `USER` WHERE Username = 'spammer99');


-- ===================== UPDATE =====================

-- Update 1: suspend the reported user's account
UPDATE `USER`
SET AccountStatus = 'Suspended'
WHERE Username = 'spammer99';

-- Update 2: correct the time of a moment
UPDATE MOMENT
SET Time = '15:00:00'
WHERE MomentID = 124
  AND TimeZone = 'Europe';


-- ===================== DELETE =====================

-- Delete 1: remove a post that violates community guidelines
-- (its comments, reactions and tags are deleted with it)
DELETE FROM POST
WHERE Caption = 'Buy followers now'
  AND UserID = (SELECT UserID FROM `USER` WHERE Username = 'spammer99');

-- Delete 2: remove a flagged RealMoji (its reaction records are removed first)
DELETE FROM REACTS_WITH
WHERE RealMojiID IN (SELECT RealMojiID FROM REALMOJIS WHERE Emoji = 'Spam');

DELETE FROM REALMOJIS
WHERE Emoji = 'Spam'
  AND UserID = (SELECT UserID FROM `USER` WHERE Username = 'spammer99');


-- ===================== CLEANUP (so the script can be run again) =====================
DELETE FROM TAGGED_ON WHERE UserID = 2 AND PostID = 2;
DELETE FROM MOMENT WHERE MomentID = 124;
DELETE FROM `USER` WHERE Username = 'spammer99';
