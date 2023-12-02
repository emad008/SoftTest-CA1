package controllers;

import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import database.Database;
import model.Comment;
import model.Commodity;
import model.Provider;
import model.User;
import org.javatuples.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
class E2ECommoditiesControllerTest {
    @InjectMocks
    private CommoditiesController commoditiesController;

    private MockMvc mockMvc;

    @BeforeAll
    public static void setup(){

    }

    @AfterAll
    public static void teardown() {
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commoditiesController).build();
    }

    @AfterEach
    public void tearDown() {
    }

    public void setUpUsers(ArrayList<User> users) {
        Database.getInstance().setUsers(users);
    }

    public void setUpCommodities(ArrayList<Commodity> commodities) {
        Database.getInstance().setCommodities(commodities);
    }

    public void setUpProviders(ArrayList<Provider> providers) {
        Database.getInstance().setProviders(providers);
    }

    public static ArrayList<ArrayList<User>> getUserFixtures() {
        return new ArrayList<>(
                List.of(
                        new ArrayList<>(
                                List.of(
                                        new User(
                                                "user1",
                                                "password1",
                                                "emamemad@gmail.com",
                                                "1379/9/9",
                                                "tehran"
                                        ),
                                        new User(
                                                "user2",
                                                "password2",
                                                "emademami8@gmail.com",
                                                "1379/9/9",
                                                "iran"
                                        )
                                )
                        )
                )
        );
    }
    public static ArrayList<ArrayList<Commodity>> getCommoditiesFixtures() {
        return new ArrayList<>(
            List.of(
                    new ArrayList<>(
                            List.of(
                                    new Commodity(
                                            "1",
                                            "apple",
                                            "provider1",
                                            5,
                                            new ArrayList<>(List.of("fruits")),
                                            (float) 3.5,
                                            100
                                    )
                            )
                    )
            )
        );
    }

    public static ArrayList<ArrayList<Provider>> getProvidersFixtures() {
        return new ArrayList<>(
                List.of(
                        new ArrayList<>(
                                List.of(
                                        new Provider(
                                                "provider1",
                                                "miveforoshi",
                                                "1379/9/7",
                                                "image.jpg"
                                        )
                                )
                        )
                )
        );
    }

    public static Stream<Arguments> commoditiesFixtures() {
        return getCommoditiesFixtures().stream().map(Arguments::of);
    }

    private Commodity getCommodityById(String commodityId) throws Exception {
        String response = mockMvc.perform(
            get(String.format("/commodities/%s", commodityId)).contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
        return new Gson().fromJson(response, new TypeToken<Commodity>(){}.getType());
    }

    private ArrayList<Comment> getCommodityComments(String commodityId) throws Exception {
        String response = mockMvc.perform(
                        get(String.format("/commodities/%s/comment", commodityId)).contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return new Gson().fromJson(response, new TypeToken<ArrayList<Comment>>(){}.getType());
    }

    @ParameterizedTest
    @MethodSource("commoditiesFixtures")
    void testGetCommodities(ArrayList<Commodity> commodities) throws Exception {
        // given
        this.setUpCommodities(commodities);

        // when
        String response = mockMvc.perform(
                        get("/commodities").contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ArrayList<Commodity> responseCommodities = new Gson().fromJson(response, new TypeToken<ArrayList<Commodity>>(){}.getType());

        // then
        Assertions.assertEquals(commodities.size(), responseCommodities.size());
        for (int i = 0; i < commodities.size(); i++)
            assertThat(commodities.get(i))
                    .usingRecursiveComparison()
                    .isEqualTo(responseCommodities.get(i));
    }

    @ParameterizedTest
    @MethodSource("commoditiesFixtures")
    public void testGetCommoditySuccess(ArrayList<Commodity> commodities) throws Exception {
        // given
        int toBeFetchedIndex = 0;
        this.setUpCommodities(commodities);

        // when
        Commodity responseCommodity = getCommodityById(commodities.get(toBeFetchedIndex).getId());

        // then
        assertThat(commodities.get(toBeFetchedIndex))
                .usingRecursiveComparison()
                .isEqualTo(responseCommodity);
    }

    @ParameterizedTest
    @MethodSource("commoditiesFixtures")
    public void testGetCommodityNotExistent(ArrayList<Commodity> commodities) throws Exception {
        // given
        this.setUpCommodities(commodities);
        String nonExistentCommodityId = "nonExistentCommodityId";

        // when
        mockMvc.perform(
            get(String.format("/commodities/%s", nonExistentCommodityId)).contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().is4xxClientError());
    }

    public static Stream<Arguments> rateCommoditiesFixtures() {
        var commodities = getCommoditiesFixtures();
        var ratings = new ArrayList<>(
                List.of(
                        new ArrayList(
                                List.of(
                                        new Quartet<>("1", "user1", 4, 3.75F)
                                )
                        )
                )
        );
        return Streams.zip(commodities.stream(), ratings.stream(), Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("rateCommoditiesFixtures")
    public void testRateCommoditySuccess(ArrayList<Commodity> commodities, ArrayList<Quartet<String, String, Integer, Float>> ratings) throws Exception {
        // given
        this.setUpCommodities(commodities);

        // when
        for (var rating: ratings) {
            Map<String, String> request = new HashMap<>();
            request.put("username", rating.getValue1());
            request.put("rate", rating.getValue2().toString());
            var responseMsg = mockMvc.perform(
                post(String.format("/commodities/%s/rate", rating.getValue0())).content(new Gson().toJson(request)).contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

            // then
            Commodity responseCommodity = getCommodityById(rating.getValue0());

            assertEquals("rate added successfully!", responseMsg);
            assertEquals(responseCommodity.getRating(), rating.getValue3());
        }
    }

    @ParameterizedTest
    @MethodSource("commoditiesFixtures")
    public void testRateCommodityNotExistent(ArrayList<Commodity> commodities) throws Exception {
        // given
        this.setUpCommodities(commodities);

        // when
        String commodityId = "NonExistentCommodityId";
        Map<String, String> request = new HashMap<>();
        request.put("username", "NonExistentUsername");
        request.put("rate", "5");
        var failMsg = mockMvc.perform(
                post(String.format("/commodities/%s/rate", commodityId)).content(new Gson().toJson(request)).contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().is4xxClientError())
        .andReturn()
        .getResponse()
        .getContentAsString();

        assertEquals("Commodity does not exist.", failMsg);
    }

    @ParameterizedTest
    @MethodSource("commoditiesFixtures")
    public void testRateCommodityInvalidRate(ArrayList<Commodity> commodities) throws Exception {
        // given
        this.setUpCommodities(commodities);

        // when
        String commodityId = commodities.get(0).getId();
        Map<String, String> request = new HashMap<>();
        request.put("username", "NonExistentUsername");
        request.put("rate", "invalidRate");
        var failMsg = mockMvc.perform(
            post(String.format("/commodities/%s/rate", commodityId)).content(new Gson().toJson(request)).contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().is4xxClientError())
        .andReturn()
        .getResponse()
        .getContentAsString();
    }

    public static Stream<Arguments> addCommentCommoditiesFixtures() {
        var commodities = getCommoditiesFixtures();
        var users = getUserFixtures();
        var ratings = new ArrayList<>(
                List.of(
                        new ArrayList(
                                List.of(
                                        new Comment(1, "emamiemad8@gmail.com", "user1", 1, "very bad"),
                                        new Comment(2, "emademami8@gmail.com", "user2", 1, "very good")
                                )
                        )
                )
        );
        return Streams.zip(
                Streams.zip(
                        users.stream(),
                        commodities.stream(),
                        Pair::new
                ),
                ratings.stream(),
                (commoditiesAndUsers, ratingsList) -> Arguments.of(
                        commoditiesAndUsers.getValue0(),
                        commoditiesAndUsers.getValue1(),
                        ratingsList
                )
        );
    }

    @ParameterizedTest
    @MethodSource("addCommentCommoditiesFixtures")
    public void testAddCommodityCommentSuccess(ArrayList<User> users, ArrayList<Commodity> commodities, ArrayList<Comment> comments) throws Exception {
        // given
        this.setUpUsers(users);
        this.setUpCommodities(commodities);

        // when
        for (var comment: comments) {
            Map<String, String> request = new HashMap<>();
            request.put("username", comment.getUsername());
            request.put("comment", comment.getText());
            var responseMsg = mockMvc.perform(
                            post(String.format("/commodities/%s/comment", comment.getCommodityId()))
                                    .content(new Gson().toJson(request)).contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals("comment added successfully!", responseMsg);

            var fetchedComments = getCommodityComments(((Integer)comment.getCommodityId()).toString());
            assertTrue(fetchedComments.stream().anyMatch(fetchedComment -> fetchedComment.getText().equals(comment.getText())));
        }
    }

    @ParameterizedTest
    @MethodSource("commoditiesFixtures")
    public void testAddCommodityCommentWithNonExistentUser(ArrayList<Commodity> commodities) throws Exception {
        // given
        this.setUpCommodities(commodities);
        Map<String, String> request = new HashMap<>();
        request.put("username", "NonExistentUsername");
        request.put("comment", "Test comment");

        // when
        var responseMsg = mockMvc.perform(
                        post(String.format("/commodities/%s/comment", commodities.get(0).getId()))
                                .content(new Gson().toJson(request)).contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals("User does not exist.", responseMsg);
    }

    @ParameterizedTest
    @MethodSource("addCommentCommoditiesFixtures")
    public void testAddCommodityCommentToNonExistentCommodity(ArrayList<User> users, ArrayList<Commodity> commodities, ArrayList<Comment> comments) throws Exception {
        // given
        this.setUpUsers(users);
        this.setUpCommodities(commodities);

        // when
        Map<String, String> request = new HashMap<>();
        request.put("username", users.get(0).getUsername());
        request.put("comment", "Test comment");

        var responseMsg = mockMvc.perform(
                        post(String.format("/commodities/%s/comment", "nonExistentCommodity"))
                                .content(new Gson().toJson(request)).contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals("Commodity does not exist.", responseMsg);
    }

    @ParameterizedTest
    @MethodSource("commoditiesFixtures")
    public void testGetCommodityCommentFromNonExistentCommodity(ArrayList<Commodity> commodities) throws Exception {
        // given
        this.setUpCommodities(commodities);

        // when
        mockMvc.perform(
                get(String.format("/commodities/%s/comment", "nonExistentCommodity"))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @ParameterizedTest
    @MethodSource("commoditiesFixtures")
    public void testSearchCommoditiesByName(ArrayList<Commodity> commodities) throws Exception {
        // given
        this.setUpCommodities(commodities);

        // when
        Map<String, String> request = new HashMap<>();
        request.put("searchOption", "name");
        request.put("searchValue", commodities.get(0).getName());

        var response = mockMvc.perform(
            post("/commodities/search")
                .content(new Gson().toJson(request))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

        ArrayList<Commodity> matchedCommodities = new Gson().fromJson(response, new TypeToken<ArrayList<Commodity>>(){}.getType());
        assertEquals(matchedCommodities.size(), 1);
        assertThat(matchedCommodities.get(0))
                .usingRecursiveComparison()
                .isEqualTo(commodities.get(0));
    }

    public static Stream<Arguments> searchCommoditiesByProviderNameFixture() {
        var commodities = getCommoditiesFixtures();
        var providers = getProvidersFixtures();
        var providerNames = new ArrayList<>(
                List.of(
                    "miveforoshi"
                )
        );
        return Streams.zip(
                Streams.zip(
                        providers.stream(),
                        commodities.stream(),
                        Pair::new
                ),
                providerNames.stream(),
                (providersAndCommodities, ratingsList) -> Arguments.of(
                        providersAndCommodities.getValue0(),
                        providersAndCommodities.getValue1(),
                        ratingsList
                )
        );
    }

    @ParameterizedTest
    @MethodSource("searchCommoditiesByProviderNameFixture")
    public void testSearchCommoditiesByProviderName(ArrayList<Provider> providers, ArrayList<Commodity> commodities, String providerName) throws Exception {
        // given
        this.setUpProviders(providers);
        this.setUpCommodities(commodities);

        // when
        Map<String, String> request = new HashMap<>();
        request.put("searchOption", "provider");
        request.put("searchValue", providerName);
        var response = mockMvc.perform(
                        post("/commodities/search")
                                .content(new Gson().toJson(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ArrayList<Commodity> matchedCommodities = new Gson().fromJson(response, new TypeToken<ArrayList<Commodity>>(){}.getType());
        assertEquals(matchedCommodities.size(), 1);
    }

    @ParameterizedTest
    @MethodSource("commoditiesFixtures")
    public void testSearchCommoditiesByCategory(ArrayList<Commodity> commodities) throws Exception {
        // given
        this.setUpCommodities(commodities);

        // when
        Map<String, String> request = new HashMap<>();
        request.put("searchOption", "category");
        request.put("searchValue", commodities.get(0).getCategories().get(0));
        var response = mockMvc.perform(
                        post("/commodities/search")
                                .content(new Gson().toJson(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ArrayList<Commodity> matchedCommodities = new Gson().fromJson(response, new TypeToken<ArrayList<Commodity>>(){}.getType());
        assertEquals(matchedCommodities.size(), 1);
    }

    @Test
    public void testSearchCommoditiesByInvalidSearchOption() throws Exception {
        // given
        // when
        Map<String, String> request = new HashMap<>();
        request.put("searchOption", "invalidSearchOption");
        request.put("searchValue", "1");
        var response = mockMvc.perform(
                        post("/commodities/search")
                                .content(new Gson().toJson(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ArrayList<Commodity> matchedCommodities = new Gson().fromJson(response, new TypeToken<ArrayList<Commodity>>(){}.getType());
        assertEquals(matchedCommodities.size(), 0);
    }

    public static Stream<Arguments> getSuggestedCommoditiesFixture() {
        var commodities = getCommoditiesFixtures();
        var suggestedCommodities = new ArrayList<>(
                List.of(
                        new ArrayList()
                )
        );
        return Streams.zip(
                commodities.stream(),
                suggestedCommodities.stream(),
                Arguments::of
        );
    }

    @ParameterizedTest
    @MethodSource("getSuggestedCommoditiesFixture")
    public void testGetSuggestedCommodities(ArrayList<Commodity> commodities, ArrayList<Commodity> toBeSuggestedCommodities) throws Exception {
        // given
        this.setUpCommodities(commodities);

        // when
        var response = mockMvc.perform(
            get(String.format("/commodities/%s/suggested", commodities.get(0).getId()))
                    .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

        // then
        ArrayList<Commodity> suggestedCommodities = new Gson().fromJson(response, new TypeToken<ArrayList<Commodity>>(){}.getType());
        assertThat(suggestedCommodities)
                .usingRecursiveComparison()
                .isEqualTo(toBeSuggestedCommodities);
    }
}
