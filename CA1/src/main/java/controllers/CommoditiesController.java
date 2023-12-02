package controllers;

import exceptions.InvalidSearchOption;
import service.Baloot;
import model.Comment;
import model.Commodity;
import model.User;
import exceptions.NotExistentCommodity;
import exceptions.NotExistentUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
public class CommoditiesController {

    private Baloot baloot = Baloot.getInstance();

    public void setBaloot(Baloot baloot) {
        this.baloot = baloot;
    }
    @GetMapping(value = "/commodities")
    public ResponseEntity<ArrayList<Commodity>> getCommodities() {
        return new ResponseEntity<>(baloot.getCommodities(), HttpStatus.OK);
    }

    @GetMapping(value = "/commodities/{id}")
    public ResponseEntity<Commodity> getCommodity(@PathVariable String id) {
        try {
            Commodity commodity = baloot.getCommodityById(id);
            return new ResponseEntity<>(commodity, HttpStatus.OK);
        } catch (NotExistentCommodity e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/commodities/{id}/rate")
    public ResponseEntity<String> rateCommodity(@PathVariable String id, @RequestBody Map<String, String> input) {
        try {
            int rate = Integer.parseInt(input.get("rate"));
            String username = input.get("username");
            Commodity commodity = baloot.getCommodityById(id);
            commodity.addRate(username, rate);
            return new ResponseEntity<>("rate added successfully!", HttpStatus.OK);
        } catch (NotExistentCommodity e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/commodities/{id}/comment")
    public ResponseEntity<String> addCommodityComment(@PathVariable String id, @RequestBody Map<String, String> input) {
        int commentId = baloot.generateCommentId();
        String username = input.get("username");
        String commentText = input.get("comment");

        User user;
        try {
            user = baloot.getUserById(username);
        } catch (NotExistentUser e) {
            // Changed: This exception should not be ignored
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        Commodity commodity;
        try {
            commodity = baloot.getCommodityById(id);
        } catch (NotExistentCommodity e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

        Comment comment = new Comment(commentId, user.getEmail(), user.getUsername(), Integer.parseInt(commodity.getId()), commentText);
        baloot.addComment(comment);

        return new ResponseEntity<>("comment added successfully!", HttpStatus.OK);
    }

    @GetMapping(value = "/commodities/{id}/comment")
    public ResponseEntity<ArrayList<Comment>> getCommodityComment(@PathVariable String id) {
        try {
            System.out.println("FOKE " + id);
            ArrayList<Comment> comments = baloot.getCommentsForCommodity(
                    Integer.parseInt(baloot.getCommodityById(id).getId())
            );
            return new ResponseEntity<>(comments, HttpStatus.OK);
        } catch (NotExistentCommodity e) {
            // We should check for commodity existent
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/commodities/search")
    public ResponseEntity<ArrayList<Commodity>> searchCommodities(@RequestBody Map<String, String> input) {
        String searchOption = input.get("searchOption");
        String searchValue = input.get("searchValue");

        try {
            ArrayList<Commodity> commodities = switch (searchOption) {
                case "name" -> baloot.filterCommoditiesByName(searchValue);
                case "category" -> baloot.filterCommoditiesByCategory(searchValue);
                case "provider" -> baloot.filterCommoditiesByProviderName(searchValue);
                default -> throw new InvalidSearchOption();
            };
            System.out.println("HELL NA");
            return new ResponseEntity<>(commodities, HttpStatus.OK);
        } catch (InvalidSearchOption e) {
            System.out.println("FOKE: " + e.getMessage());
            // We should check for commodity existent
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/commodities/{id}/suggested")
    public ResponseEntity<ArrayList<Commodity>> getSuggestedCommodities(@PathVariable String id) {
        try {
            Commodity commodity = baloot.getCommodityById(id);
            ArrayList<Commodity> suggestedCommodities = baloot.suggestSimilarCommodities(commodity);
            return new ResponseEntity<>(suggestedCommodities, HttpStatus.OK);
        } catch (NotExistentCommodity ignored) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
        }
    }
}
