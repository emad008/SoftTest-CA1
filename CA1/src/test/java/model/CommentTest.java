package model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class CommentTest {
    private Comment comment;

    @BeforeEach
    void setUp() {
        comment = new Comment(1, "user@example.com", "testUser", 123, "This is a test comment.");
    }

    @Test
    void testGetCurrentDate() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = comment.getCurrentDate();
        Date currentDate = new Date();
        assertEquals(dateFormat.parse(formattedDate).getTime(), currentDate.getTime(), 1000);
    }

    @Test
    void testAddUserVote() {
        comment.addUserVote("user1", "like");
        comment.addUserVote("user2", "dislike");
        comment.addUserVote("user3", "like");

        assertEquals(2, comment.getLike());
        assertEquals(1, comment.getDislike());
        assertTrue(comment.getUserVote().containsKey("user1"));
        assertTrue(comment.getUserVote().containsKey("user2"));
        assertTrue(comment.getUserVote().containsKey("user3"));
    }

    @Test
    void testAddUserVoteOnlyDislike() {
        comment.addUserVote("user1", "dislike");
        comment.addUserVote("user2", "dislike");

        assertEquals(0, comment.getLike());
        assertEquals(2, comment.getDislike());
    }

    @Test
    void testAddUserVoteOnlyLike() {
        comment.addUserVote("user1", "like");
        comment.addUserVote("user2", "like");

        assertEquals(2, comment.getLike());
        assertEquals(0, comment.getDislike());
    }
}
