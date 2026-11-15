-- 1. TWO-TABLE JOIN
-- Retrieve pending friendship requests
-- along with the requester's username

SELECT
    F.Friendship_id,
    U.Username AS Requester,
    F.Receiver_ID,
    F.Date_Requested
FROM FRIENDSHIP F
JOIN `USER` U
    ON F.Requester_ID = U.UserID
WHERE F.Status = 'Pending';


-- -----------------------------------------------------

-- 2. THREE-TABLE JOIN
-- Retrieve public posts for a specific moment

SELECT
    P.PostID,
    U.Username,
    P.FrontCam,
    P.BackCam,
    P.Caption,
    P.HoursLate,
    P.PostedAt
FROM `USER` U
JOIN POST P
    ON U.UserID = P.UserID
JOIN MOMENT M
    ON P.MomentID = M.MomentID
WHERE M.MomentID = 2
    AND M.TimeZone = 'Asia/Riyadh'
    AND P.HoursLate < 10.0 / 60
    AND U.Account_Type = 'Public';


-- -----------------------------------------------------

-- 3. Retrieve comments with usernames

SELECT
    C.PostID,
    C.CommentNumber,
    C.Content,
    C.TimePosted,
    U.Username
FROM COMMENT C
JOIN `USER` U
    ON C.UserID = U.UserID;


-- -----------------------------------------------------

-- 4. Retrieve reactions with RealMoji details

SELECT
    RW.UserID,
    RW.PostID,
    P.PostedAt,
    R.Emoji,
    R.Image
FROM REACTS_WITH RW
JOIN POST P
    ON RW.PostID = P.PostID
JOIN REALMOJIS R
    ON RW.RealMojiID = R.RealMojiID
WHERE R.UserID = 2;


-- -----------------------------------------------------

-- 5. Retrieve tagged users with usernames

SELECT
    T.PostID,
    U.Username
FROM TAGGED_ON T
JOIN `USER` U
    ON T.UserID = U.UserID;