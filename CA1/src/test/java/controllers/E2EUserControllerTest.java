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
class E2EUserControllerTest {
    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeAll
    public static void setup(){

    }

    @AfterAll
    public static void teardown() {
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @AfterEach
    public void tearDown() {
    }

    public void setUpUsers(ArrayList<User> users) {
        Database.getInstance().setUsers(users);
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

    private User getUserByUsername(String username) throws Exception {
        String response = mockMvc.perform(
                        get(String.format("/users/%s", username))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return new Gson().fromJson(response, new TypeToken<User>(){}.getType());
    }

    public static Stream<Arguments> usersFixture() {
        return getUserFixtures().stream().map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("usersFixture")
    void testGetUserSuccess(ArrayList<User> users) throws Exception {
        // given
        this.setUpUsers(users);

        // when
        User user = getUserByUsername(users.get(0).getUsername());

        // then
        assertThat(user)
                .usingRecursiveComparison()
                .isEqualTo(users.get(0));
    }

    @ParameterizedTest
    @MethodSource("usersFixture")
    void testGetNonExistentUser(ArrayList<User> users) throws Exception {
        // given
        this.setUpUsers(users);

        // when
        mockMvc.perform(
                        get(String.format("/users/%s", "NonExistentId"))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
                .andReturn()
                .getResponse()
                .getContentAsString();;
    }

    public static Stream<Arguments> addCreditFixtures() {
        var users = getUserFixtures();
        var addCredits = new ArrayList<>(
                List.of(
                        new ArrayList(
                                List.of(
                                        new Pair<>("user1", 100F),
                                        new Pair<>("user2", 120F),
                                        new Pair<>("user2", 10F)
                                )
                        )
                )
        );
        return Streams.zip(
                users.stream(),
                addCredits.stream(),
                Arguments::of
        );
    }

    @ParameterizedTest
    @MethodSource("addCreditFixtures")
    void testAddCreditSuccess(ArrayList<User> users, ArrayList<Pair<String, Float>> addCredits) throws Exception {
        // given
        this.setUpUsers(users);


        // when
        for (var addCredit: addCredits) {
            var beforeCredit = getUserByUsername(addCredit.getValue0()).getCredit();
            Map<String, String> request = new HashMap<>();
            request.put("credit", addCredit.getValue1().toString());
            var response = mockMvc.perform(
                post(String.format("/users/%s/credit", addCredit.getValue0()))
                        .content(new Gson().toJson(request))
                        .contentType(MediaType.APPLICATION_JSON)
            )
//            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

            // then
            var afterCredit = getUserByUsername(addCredit.getValue0()).getCredit();
            assertEquals(response, "credit added successfully!");
            assertEquals( addCredit.getValue1(), afterCredit - beforeCredit);
        }
    }

    @ParameterizedTest
    @MethodSource("usersFixture")
    void testNonPositiveAddCredit(ArrayList<User> users) throws Exception {
        // given
        this.setUpUsers(users);

        // when
        Map<String, String> request = new HashMap<>();
        request.put("credit", "-1");
        var response = mockMvc.perform(
                        post(String.format("/users/%s/credit", users.get(0).getUsername()))
                                .content(new Gson().toJson(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
            .andExpect(status().is4xxClientError())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertEquals(response, "Credit value must be a positive float");
    }

    @ParameterizedTest
    @MethodSource("usersFixture")
    void testNonNumberAddCredit(ArrayList<User> users) throws Exception {
        // given
        this.setUpUsers(users);

        // when
        Map<String, String> request = new HashMap<>();
        request.put("credit", "noWay");
        var response = mockMvc.perform(
                        post(String.format("/users/%s/credit", users.get(0).getUsername()))
                                .content(new Gson().toJson(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(response, "Please enter a valid number for the credit amount.");
    }

    @ParameterizedTest
    @MethodSource("usersFixture")
    void testNonExistentUserAddCredit(ArrayList<User> users) throws Exception {
        // given
        this.setUpUsers(users);

        // when
        Map<String, String> request = new HashMap<>();
        request.put("credit", "100");
        var response = mockMvc.perform(
                        post(String.format("/users/%s/credit", "NonExistentUser"))
                                .content(new Gson().toJson(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(response, "User does not exist.");
    }
}
