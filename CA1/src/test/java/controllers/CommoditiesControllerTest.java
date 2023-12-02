package controllers;

import exceptions.NotExistentCommodity;
import exceptions.NotExistentUser;
import model.Comment;
import model.Commodity;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import service.Baloot;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CommoditiesControllerTest {
    @InjectMocks
    private CommoditiesController commoditiesController;

    private AutoCloseable mockCloseable;
    @Mock
    private Baloot baloot;
    @Mock
    private Commodity mockCommodity;

    @BeforeEach
    public void setUp() {
        mockCloseable = MockitoAnnotations.openMocks(this);
        commoditiesController.setBaloot(baloot);
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockCloseable.close();
    }

    @Test
    public void testGetCommodities() {
        var commodity1 = new Commodity();
        commodity1.setId("1");
        var commodity2 = new Commodity();
        commodity2.setId("2");
        ArrayList<Commodity> commodityList = new ArrayList<>(List.of(commodity1, commodity2));
        doReturn(commodityList).when(baloot).getCommodities();

        ResponseEntity<ArrayList<Commodity>> response = commoditiesController.getCommodities();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals(commodity1.getId(), response.getBody().get(0).getId());
        assertEquals(commodity2.getId(), response.getBody().get(1).getId());
    }

    @Test
    public void testGetCommoditySuccess() throws NotExistentCommodity {
        var commodity = new Commodity();
        commodity.setId("1");
        doReturn(commodity).when(baloot).getCommodityById(commodity.getId());

        ResponseEntity<Commodity> response = commoditiesController.getCommodity(commodity.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(commodity.getId(), Objects.requireNonNull(response.getBody()).getId());
    }

    @Test
    public void testGetCommodityNotExistent() throws NotExistentCommodity {
        String commodityId = "1";
        doThrow(NotExistentCommodity.class).when(baloot).getCommodityById(commodityId);

        ResponseEntity<Commodity> response = commoditiesController.getCommodity(commodityId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testRateCommoditySuccess() throws NotExistentCommodity {
        var commodityId = "1";
        var username = "testUser";
        var rate = "5";
        doReturn(mockCommodity).when(baloot).getCommodityById(commodityId);
        Map<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("rate", rate);

        ResponseEntity<String> response = commoditiesController.rateCommodity(commodityId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("rate added successfully!", response.getBody());
        verify(mockCommodity, times(1)).addRate(username, Integer.parseInt(rate));
    }

    @Test
    public void testRateCommodityNotExistent() throws NotExistentCommodity {
        String commodityId = "1";
        doThrow(NotExistentCommodity.class).when(baloot).getCommodityById(commodityId);
        Map<String, String> request = new HashMap<>();
        request.put("username", "testUser");
        request.put("rate", "5");

        ResponseEntity<String> response = commoditiesController.rateCommodity(commodityId, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testRateCommodityInvalidRate() throws NotExistentCommodity {
        doReturn(new Commodity()).when(baloot).getCommodityById(anyString());

        Map<String, String> request = new HashMap<>();
        request.put("username", "testUser");
        request.put("rate", "invalidRate");

        ResponseEntity<String> response = commoditiesController.rateCommodity("1", request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testAddCommodityCommentSuccess() throws NotExistentUser, NotExistentCommodity {
        var userName = "testUser";
        var commodity = new Commodity();
        commodity.setId("1");
        when(baloot.generateCommentId()).thenReturn(1);
        doReturn(commodity).when(baloot).getCommodityById(commodity.getId());
        doReturn(
                new User(userName,
                        "testPassword",
                        "user@test.com",
                        "testBirthDate",
                        "testAddress")
        ).when(baloot).getUserById(userName);
        Map<String, String> request = new HashMap<>();
        request.put("username", userName);
        request.put("comment", "Test comment");

        ResponseEntity<String> response = commoditiesController.addCommodityComment(commodity.getId(), request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("comment added successfully!", response.getBody());
    }

    @Test
    public void testAddCommodityCommentWithNonExistentUser() throws NotExistentUser {
        var userName = "testUser";
        when(baloot.generateCommentId()).thenReturn(1);
        doThrow(NotExistentUser.class).when(baloot).getUserById(userName);
        Map<String, String> request = new HashMap<>();
        request.put("username", userName);
        request.put("comment", "Test comment");

        ResponseEntity<String> response = commoditiesController.addCommodityComment("1", request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testAddCommodityCommentToNonExistentCommodiity() throws NotExistentUser, NotExistentCommodity {
        var userName = "testUser";
        when(baloot.generateCommentId()).thenReturn(1);
        var commodityId = "1";
        doReturn(
                new User(userName,
                        "testPassword",
                        "user@test.com",
                        "testBirthDate",
                        "testAddress")
        ).when(baloot).getUserById(userName);
        doThrow(NotExistentCommodity.class).when(baloot).getCommodityById(commodityId);
        Map<String, String> request = new HashMap<>();
        request.put("username", userName);
        request.put("comment", "Test comment");

        ResponseEntity<String> response = commoditiesController.addCommodityComment(commodityId, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetCommodityComment() throws NotExistentCommodity {
        var comment1 = new Comment();
        comment1.setId(1);
        var comment2 = new Comment();
        comment2.setId(2);
        var commodity = new Commodity();
        commodity.setId("1");
        ArrayList<Comment> commentArrayList = new ArrayList<>(List.of(comment1, comment2));
        doReturn(commodity).when(baloot).getCommodityById(commodity.getId());
        doReturn(commentArrayList).when(baloot).getCommentsForCommodity(Integer.parseInt(commodity.getId()));

        ResponseEntity<ArrayList<Comment>> response = commoditiesController.getCommodityComment(commodity.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals(comment1.getId(), response.getBody().get(0).getId());
        assertEquals(comment2.getId(), response.getBody().get(1).getId());
    }

    @Test
    public void testGetCommodityCommentFromNonExistentCommodity() throws NotExistentCommodity {
        var commodityId = "1";
        doThrow(NotExistentCommodity.class).when(baloot).getCommodityById(commodityId);

        ResponseEntity<ArrayList<Comment>> response = commoditiesController.getCommodityComment(commodityId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testSearchCommoditiesByName() {
        var commodityName = "testName";
        var commodity1 = new Commodity();
        commodity1.setId("1");
        commodity1.setName(commodityName);
        var commodity2 = new Commodity();
        commodity2.setId("2");
        commodity2.setName(commodityName);
        ArrayList<Commodity> commodityList = new ArrayList<>(List.of(commodity1, commodity2));
        doReturn(commodityList).when(baloot).filterCommoditiesByName(commodityName);

        Map<String, String> request = new HashMap<>();
        request.put("searchOption", "name");
        request.put("searchValue", commodityName);

        ResponseEntity<ArrayList<Commodity>> response = commoditiesController.searchCommodities(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals(commodity1.getId(), response.getBody().get(0).getId());
        assertEquals(commodity2.getId(), response.getBody().get(1).getId());
        assertEquals(commodity1.getName(), commodityName);
        assertEquals(commodity2.getName(), commodityName);
    }

    @Test
    public void testSearchCommoditiesByProviderName() {
        var providerName = "testProvider";
        var providerId = "2";
        var commodity1 = new Commodity();
        commodity1.setId("1");
        commodity1.setProviderId(providerId);
        var commodity2 = new Commodity();
        commodity1.setId("2");
        commodity2.setProviderId(providerId);
        ArrayList<Commodity> commodityList = new ArrayList<>(List.of(commodity1, commodity2));
        doReturn(commodityList).when(baloot).filterCommoditiesByProviderName(providerName);

        Map<String, String> request = new HashMap<>();
        request.put("searchOption", "provider");
        request.put("searchValue", providerName);

        ResponseEntity<ArrayList<Commodity>> response = commoditiesController.searchCommodities(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals(commodity1.getId(), response.getBody().get(0).getId());
        assertEquals(commodity2.getId(), response.getBody().get(1).getId());
        assertEquals(commodity1.getProviderId(), providerId);
        assertEquals(commodity2.getProviderId(), providerId);
    }

    @Test
    public void testSearchCommoditiesByCategory() {
        var category = "testCategory";
        var commodity1 = new Commodity();
        commodity1.setId("1");
        commodity1.setCategories(new ArrayList<>(List.of(category)));
        var commodity2 = new Commodity();
        commodity1.setId("2");
        commodity2.setCategories(new ArrayList<>(List.of(category)));
        ArrayList<Commodity> commodityList = new ArrayList<>(List.of(commodity1, commodity2));
        doReturn(commodityList).when(baloot).filterCommoditiesByCategory(category);

        Map<String, String> request = new HashMap<>();
        request.put("searchOption", "category");
        request.put("searchValue", category);

        ResponseEntity<ArrayList<Commodity>> response = commoditiesController.searchCommodities(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals(commodity1.getId(), response.getBody().get(0).getId());
        assertEquals(commodity2.getId(), response.getBody().get(1).getId());
        assertEquals(commodity1.getCategories().get(0), category);
        assertEquals(commodity2.getCategories().get(0), category);
    }

    @Test
    public void testSearchCommoditiesByInvalidSearchOption() {
        Map<String, String> request = new HashMap<>();
        request.put("searchOption", "invalidSearchOption");
        request.put("searchValue", "testValue");

        ResponseEntity<ArrayList<Commodity>> response = commoditiesController.searchCommodities(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    public class CommodityMatcher implements ArgumentMatcher<Commodity> {
        private final String id;

        public CommodityMatcher(String id) {
            this.id = id;
        }

        @Override
        public boolean matches(Commodity commodity) {
            return Objects.requireNonNull(commodity.getId()).equals(id);
        }
    }

    @Test
    public void testGetSuggestedCommodities() throws NotExistentCommodity {
        var commodity1 = new Commodity();
        commodity1.setId("1");
        var commodity2 = new Commodity();
        commodity2.setId("2");
        ArrayList<Commodity> commodityList = new ArrayList<>(List.of(commodity2));
        doReturn(commodity1).when(baloot).getCommodityById(commodity1.getId());
        doReturn(commodityList).when(baloot).suggestSimilarCommodities(argThat(new CommodityMatcher(commodity1.getId())));

        ResponseEntity<ArrayList<Commodity>> response = commoditiesController.getSuggestedCommodities(commodity1.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, Objects.requireNonNull(response.getBody()).size());
        assertEquals(commodity2.getId(), response.getBody().get(0).getId());
    }
}
