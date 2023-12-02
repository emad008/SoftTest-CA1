package controllers;

import exceptions.NotExistentComment;
import model.Comment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import service.Baloot;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CommentControllerTest {
    @Mock
    private Baloot baloot;
    @Mock
    private Comment comment;
    private AutoCloseable mockCloseable;
    @InjectMocks
    private CommentController commentController;

    @BeforeEach
    public void setUp() {
        mockCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockCloseable.close();
    }

    @Test
    public void testLikeCommentSuccess() throws NotExistentComment {
        int commentId = 1;
        var username = "testUser";
        doReturn(comment).when(baloot).getCommentById(commentId);
        Map<String, String> request = new HashMap<>();
        request.put("username", username);

        ResponseEntity<String> response = commentController.likeComment(String.valueOf(commentId), request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("The comment was successfully liked!", response.getBody());
        verify(comment, times(1)).addUserVote(username, "like");
    }

    @Test
    public void testLikeCommentNotExistentComment() throws NotExistentComment {
        int commentId = 1;
        doThrow(NotExistentComment.class).when(baloot).getCommentById(commentId);
        Map<String, String> request = new HashMap<>();
        request.put("username", "testUser");

        ResponseEntity<String> response = commentController.likeComment(String.valueOf(commentId), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDislikeCommentSuccess() throws NotExistentComment {
        int commentId = 1;
        var username = "testUser";
        doReturn(comment).when(baloot).getCommentById(commentId);
        Map<String, String> request = new HashMap<>();
        request.put("username", username);

        ResponseEntity<String> response = commentController.dislikeComment(String.valueOf(commentId), request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("The comment was successfully disliked!", response.getBody());
        verify(comment, times(1)).addUserVote(username, "dislike");
    }

    @Test
    public void testDislikeCommentNotExistentComment() throws NotExistentComment {
        int commentId = 1;
        doThrow(NotExistentComment.class).when(baloot).getCommentById(commentId);
        Map<String, String> request = new HashMap<>();
        request.put("username", "testUser");

        ResponseEntity<String> response = commentController.dislikeComment(String.valueOf(commentId), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
