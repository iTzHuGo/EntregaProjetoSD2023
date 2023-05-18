package com.example.servingwebcontent;

import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.print.event.PrintEvent;
import javax.servlet.http.HttpSession;

import com.example.meta1.SearchModuleInterface;
import com.example.servingwebcontent.beans.Number;
import com.example.servingwebcontent.forms.Project;
import com.example.servingwebcontent.forms.SearchTerms;
import com.example.servingwebcontent.thedata.Employee;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

@Controller
public class GreetingController {
    private List<String> searchResults;
    private int currentIndex;
    private String HackerNewsLink = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";

    @Resource(name = "requestScopedNumberGenerator")
    private Number nRequest;

    @Resource(name = "sessionScopedNumberGenerator")
    private Number nSession;

    @Resource(name = "applicationScopedNumberGenerator")
    private Number nApplication;

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Number requestScopedNumberGenerator() {
        return new Number();
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Number sessionScopedNumberGenerator() {
        return new Number();
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Number applicationScopedNumberGenerator() {
        return new Number();
    }

    @GetMapping("/")
    public String redirect() {
        return "redirect:/login";
    }

    @GetMapping("/menu")
    public String menu(Model model) {
        return "menu";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(name = "username", required = true) String username,
            @RequestParam(name = "password", required = true) String password, Model model) {
        if (username.equals("user") && password.equals("pass")) {
            model.addAttribute("username", username);
            return "menu";
        } else {
            model.addAttribute("username", username);
            return "error";
        }
    }

    @PostMapping("/index-new-url")
    public String saveProjectSubmission(@RequestParam(name = "url", required = true) String url, Model model)
            throws Exception {
        SearchModuleInterface searchModule = (SearchModuleInterface) LocateRegistry.getRegistry(5000)
                .lookup("SearchModule");

        searchModule.addURL(url);

        return "menu";
    }

    @GetMapping("/index-url")
    public String indexUrl(Model model) {
        return "index-url";
    }

    @GetMapping("/search-form")
    public String searchForm(Model model) {
        return "search-form";
    }

    @PostMapping("/search-form")
    public String searchForm(@RequestParam(name = "terms", required = true) String terms, Model model)
            throws Exception {
        SearchModuleInterface searchModule = (SearchModuleInterface) LocateRegistry.getRegistry(5000)
                .lookup("SearchModule");

        String res = searchModule.search(terms);
        searchResults = new ArrayList<>(Arrays.asList(res.split("\n\n")));
        currentIndex = 0;

        model.addAttribute("searchTerms", terms);
        model.addAttribute("results", getNextResults());
        return "search-results";
    }

    private List<String> getNextResults() {
        int endIndex = Math.min(currentIndex + 10, searchResults.size());
        List<String> results = searchResults.subList(currentIndex, endIndex);
        currentIndex = endIndex;
        return results;
    }

    @GetMapping("/next")
    public String getNextResults(Model model) {
        model.addAttribute("results", getNextResults());
        return "search-results";
    }

    @PostMapping("/search-results")
    public String searchTerms(@RequestParam(name = "terms", required = true) String terms, Model model)
            throws Exception {
        SearchModuleInterface searchModule = (SearchModuleInterface) LocateRegistry.getRegistry(5000)
                .lookup("SearchModule");

        String res = searchModule.search(terms);
        searchResults = new ArrayList<>(Arrays.asList(res.split("\n\n")));
        currentIndex = 0;

        model.addAttribute("results", getNextResults());
        return "search-results";
    }

    @GetMapping("/search-urls")
    public String search2(Model model) {
        return "search-urls";
    }

    @PostMapping("/search-urls")
    public String searchUrl(@RequestParam(name = "url", required = true) String url, Model model)
            throws Exception {
        SearchModuleInterface searchModule = (SearchModuleInterface) LocateRegistry.getRegistry(5000)
                .lookup("SearchModule");

        String res = searchModule.search2(url);

        List<String> searchResultssss = new ArrayList<>(Arrays.asList(res.split("\n")));

        model.addAttribute("searchURL", url);
        model.addAttribute("resultsURL", searchResultssss);

        return "results-urls";
    }

    @GetMapping("/search-urls-hacker")
    public String searchHacker(Model model) {
        return "search-urls-hacker";
    }

    @PostMapping("/search-form-hacker")
    public String searchHacker(@RequestParam(name = "terms", required = true) String terms, Model model)
            throws Exception {

        SearchModuleInterface searchModule = (SearchModuleInterface) LocateRegistry.getRegistry(5000)
                .lookup("SearchModule");

        HashMap<String, ArrayList<String>> res = hackerNewsTopStories(terms);

        for (String key : res.keySet()) {
            searchModule.addURL(key);
        }

        model.addAttribute("hackernewslist", res);

        return "results-urls-hacker";
    }

    public HashMap<String, ArrayList<String>> hackerNewsTopStories(String search) {
        RestTemplate restTemplate = new RestTemplate();
        List topStories = restTemplate.getForObject(HackerNewsLink, List.class);

        // System.out.println(topStories);

        assert topStories != null;
        HashMap<String, ArrayList<String>> hackerNewsItemRecords = new HashMap<>();

        // add the link has the key in the Hashmap and the title as the object to the
        // hashmap.
        // if the link is already in the hashmap, append the title to the list of titles


        // topStories.size() takes way too long so 20 it is
        for (int i = 0; i < 20; i++) {
            Integer storyId = (Integer) topStories.get(i);

            String storyItemDetailsEndpoint = String
                    .format("https://hacker-news.firebaseio.com/v0/item/%s.json?print=pretty", storyId);

            // System.out.println(storyItemDetailsEndpoint);
            HackerNewsItemRecord hackerNewsItemRecord = restTemplate.getForObject(storyItemDetailsEndpoint,
                    HackerNewsItemRecord.class);
            if (search != null) {
                List<String> searchTermsList = List.of(search.toLowerCase().split(" "));
                if (searchTermsList.stream().anyMatch(hackerNewsItemRecord.title().toLowerCase()::contains)) {
                    if (hackerNewsItemRecords.containsKey(hackerNewsItemRecord.url())) {
                        hackerNewsItemRecords.get(hackerNewsItemRecord.url()).add(hackerNewsItemRecord.title());
                    } else {
                        ArrayList<String> titles = new ArrayList<>();
                        titles.add(hackerNewsItemRecord.title());
                        hackerNewsItemRecords.put(hackerNewsItemRecord.url(), titles);
                    }
                }
            } else {
                if (hackerNewsItemRecords.containsKey(hackerNewsItemRecord.url())) {
                    hackerNewsItemRecords.get(hackerNewsItemRecord.url()).add(hackerNewsItemRecord.title());
                } else {
                    ArrayList<String> titles = new ArrayList<>();
                    titles.add(hackerNewsItemRecord.title());
                    hackerNewsItemRecords.put(hackerNewsItemRecord.url(), titles);
                }
            }
        }
        return hackerNewsItemRecords;
    }

    @GetMapping("/search-users-hacker")
    public String searchHackerUsers(Model model) {
        return "search-users-hacker";
    }

    @PostMapping("/search-users-hacker")
    public String searchHackerUsers(@RequestParam(name = "terms", required = true) String terms, Model model)
            throws Exception {

        SearchModuleInterface searchModule = (SearchModuleInterface) LocateRegistry.getRegistry(5000)
                .lookup("SearchModule");

        ArrayList<HackerNewsItemRecord> res = hackerNewsUser(terms);

        for (HackerNewsItemRecord record : res) {
            System.out.println(record.title());
        }

        model.addAttribute("searchTerms", terms);
        model.addAttribute("hackernewsuserlist", res);

        return "results-users-hacker";
    }

    public ArrayList<HackerNewsItemRecord> hackerNewsUser(String user) {
        String userEndpoins = "https://hacker-news.firebaseio.com/v0/user/" + user + ".json?print=pretty";
        ArrayList<HackerNewsItemRecord> hackerNewsUserRecords = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        HackerNewsUserRecord hackerNewsUserRecord = restTemplate.getForObject(userEndpoins, HackerNewsUserRecord.class);

        // for each story id, get the story details
        // https://hacker-news.firebaseio.com/v0/item/<number>.json?print=pretty
        assert hackerNewsUserRecord != null;
        assert hackerNewsUserRecord.submitted() != null;
        if (hackerNewsUserRecord == null) {
            return hackerNewsUserRecords;
        } else if (hackerNewsUserRecord.submitted() == null) {
            return hackerNewsUserRecords;
        }

        for (Object storyId : hackerNewsUserRecord.submitted()) {
            String storyItemDetailsEndpoint = String
                    .format("https://hacker-news.firebaseio.com/v0/item/%s.json?print=pretty", storyId);
            HackerNewsItemRecord hackerNewsItemRecord = restTemplate.getForObject(storyItemDetailsEndpoint,
                    HackerNewsItemRecord.class);

            // filter out the stories that don't have a url or title
            if (hackerNewsItemRecord == null || hackerNewsItemRecord.url() == null
                    || hackerNewsItemRecord.title() == null) {
                continue;
            }

            hackerNewsUserRecords.add(hackerNewsItemRecord);
        }

        return hackerNewsUserRecords;
    }
}