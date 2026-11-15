
-- 1. TWO-TABLE JOIN
-- Retrieve pending friendship requests
-- along with the requester's username

SELECT
    F.Friendship_id, U.Username AS Requester, F.Receiver_ID, F.Date_Requested
FROM FRIENDSHIP F JOIN USER U 
    ON F.Requester_ID = U.UserID
WHERE F.Status = 'Pending';
----------------------------------------------------


-- 2. THREE-TABLE JOIN
-- Retrieve public posts for a specific moment

SELECT
    P.PostID, U.Username, P.FrontCam, P.BackCam, P.Caption, P.HoursLate, P.PostedAt
FROM USER U JOIN POST P
    ON U.UserID = P.UserID
JOIN MOMENT M
    ON P.MomentID = M.MomentID
WHERE M.MomentID = 123 AND M.TimeZone = 'Europe' AND P.HoursLate < 10.0 / 60 
    AND U.Account_Type = 'Public';