CREATE TABLE COMMENT (
    PostID INT NOT NULL,
    CommentNumber INT NOT NULL,
    Content TEXT NOT NULL,
    TimePosted DATETIME NOT NULL,
    UserID INT NOT NULL,

    PRIMARY KEY (PostID, CommentNumber),

    FOREIGN KEY (PostID)
        REFERENCES post(PostID)
        ON DELETE CASCADE,

    FOREIGN KEY (UserID)
        REFERENCES `USER`(UserID)
);
