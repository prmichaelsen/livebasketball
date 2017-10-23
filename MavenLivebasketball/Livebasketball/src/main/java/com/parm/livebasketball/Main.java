package com.parm.livebasketball;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.parm.livebasketball.core.*;
import de.bytefish.fcmjava.client.FcmClient;
import de.bytefish.fcmjava.client.settings.PropertiesBasedSettings;
import de.bytefish.fcmjava.model.options.FcmMessageOptions;
import de.bytefish.fcmjava.model.topics.Topic;
import de.bytefish.fcmjava.requests.topic.TopicUnicastMessage;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;

import java.io.*;
import java.lang.InterruptedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Main {
    static WebDriver driver;
    static List<WebElement> rows;
    static List<WebElement> tables;
    static Hashtable<String,Game> games;
    static String sport;
    static String stage;
    static boolean Windows, Linux, Mac = false;
    static boolean run = true;
    static FirebaseService firebaseService;
    static DatabaseReference db;
    static FcmClient fcmMessenger;

    public static void main(String args[]){
        Properties fcmjavaProps = getProperties("fcmjava.properties");
        // Creates the Client using the default settings location, which is System.getProperty("user.home") + "/.fcmjava/fcmjava.properties":
        fcmMessenger = new FcmClient(PropertiesBasedSettings.createFromProperties(fcmjavaProps));
        sendNotifications("hello", "world");

        //initialize retrofit
        firebaseService = (new Retrofit.Builder()
                .baseUrl("https://livebasketball-prod.firebaseio.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build())
                .create(FirebaseService.class);

        try {
            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("LiveBasketball-prod-4b2b17eef509.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .setDatabaseUrl("https://livebasketball-prod.firebaseio.com/")
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            e.printStackTrace();
        }

        db = FirebaseDatabase.getInstance().getReference();
        if(db == null){
            throw new Error("Could not get firebase database reference");
        }

        //initialize program options
        sport = Constants.Sport.BASKETBALL;
        stage = Constants.Stage.LIVE;

        //get os
        String OS_name = System.getProperty("os.name");
        if(OS_name.startsWith("Windows")){
            Windows = true;
        }else if (OS_name.startsWith("Mac")){
            Mac = true;
            System.exit(0);
        }else if(OS_name.startsWith("Linux")){
            Linux = true;
        }

        //add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.out.println("Shutting down!");
                if(driver != null){
                    driver.quit();
                }
                System.out.println("Main thread stopped!");
                System.exit(128);
            }
        }));


        //load resources
        PhantomJsDriverManager.getInstance().setup();

        //set up driver
        System.out.println("Initializing...");
        driver = new PhantomJSDriver();
        driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
        //start driver
        System.out.println("Initialized.");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        System.out.println(dtf.format(LocalDateTime.now()) + ": Driver starting...");
        System.out.println("Running...");

        //games are initialized once
        Hashtable<String,Game> games = new Hashtable<String,Game>();
        //timestamp league last forever
        while(run){
            // just send the time to firebase to verify scraper is running
            try {
                Response res = firebaseService.putServerStatus(new ServerStatus(System.currentTimeMillis()/1000L)).execute();
                if(!res.isSuccessful()){
                    System.err.println(res.errorBody().string());
                }
            } catch(IOException e){
                e.printStackTrace();
            }

            //ensure driver is connected
            if(driver == null){
                System.err.println("No driver found. Exiting...");
                System.exit(0);
            }

            driver.get("http://www.flashscore.com/"+sport+"/");

            //set the timezone
            WebElement tzDropdownDOM = null;
            try{
                tzDropdownDOM = driver.findElement(By.cssSelector("#tzactual"));
                if(tzDropdownDOM != null){
                    tzDropdownDOM.click();
                }
            }
            catch(NoSuchElementException | StaleElementReferenceException e){
                e.printStackTrace();
            }
            try{
                TimeUnit.SECONDS.sleep(3);
            }catch(InterruptedException e){
                e.printStackTrace();
            };
            WebElement tzDOM = null;
            try{
                tzDOM = driver.findElement(By.cssSelector("#tzcontent > li:nth-child(14) > a"));
                if(tzDOM != null){
                    tzDOM.click();
                }
            }
            catch(NoSuchElementException | StaleElementReferenceException e){
                e.printStackTrace();
            }

            //get the league tables scheduled for today
            try {
                tables = driver.findElements(By.cssSelector(".fs-table>.table-main>."+sport));
            } catch(Exception e){
                e.printStackTrace();
            }

            //leagues are initialized every loop
            List<League> flashscoreLeagues = new ArrayList<>();
            //read leagues from db
            Leagues firebaseLeagues = new Leagues();
            try {
                Response res = firebaseService.getLeagues().execute();
                if(res.isSuccessful()){
                    firebaseLeagues.setLeagues((LinkedTreeMap<String,League>)res.body());
                } else {
                    System.err.println(res.errorBody().string());
                }
            } catch(IOException e){
                e.printStackTrace();
            }

            for(WebElement table : tables){
                //get the league for this table
                League league = getLeague(table);
                flashscoreLeagues.add(league);

                //get the match rows in this league table
                try {
                    rows = table.findElements(By.cssSelector("tbody>tr." + stage));
                } catch(NoSuchElementException | StaleElementReferenceException e){
                    e.printStackTrace();
                }

                //get games
                for(WebElement row : rows){
                    Game game = getMatch(row, league, games);
                    games.put(game.getId(), game);
                }
            }

            for(League flashscoreLeague: flashscoreLeagues){
                //post the league if not already set
                boolean leagueIsNew = true;
                for(League firebaseLeague: firebaseLeagues.getLeagues().values()){
                    if(flashscoreLeague.compareTo(firebaseLeague) == 0) {
                        leagueIsNew = false;
                    }
                }
                if(leagueIsNew){
                    try {
                        Response res = firebaseService.postLeague(flashscoreLeague).execute();
                        if(res.isSuccessful()) {
                        } else {
                            System.err.println(res.errorBody().string());
                        }
                    } catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }

            for(LinkedTreeMap.Entry<String, League> firebaseLeagueEntry: firebaseLeagues.getLeagues().entrySet()){
                // delete any leagues that are no longer on the flashscores website
                boolean leagueIsOutdated = true;
                for(League flashscoreLeague: flashscoreLeagues){
                    if(flashscoreLeague.compareTo(firebaseLeagueEntry.getValue()) == 0) {
                        leagueIsOutdated = false;
                    }
                }
                if(leagueIsOutdated){
                    try {
                        Response res = firebaseService.deleteLeague(firebaseLeagueEntry.getKey()).execute();
                        if(!res.isSuccessful()){
                            System.err.println(res.errorBody().string());
                        }
                    } catch(IOException e){
                        e.printStackTrace();
                    }
                } else {
                    //patchLeagueSetting("2", firebaseLeagueEntry);
                }
            }

            //send out notifications
            Iterator<Game> it = games.values().iterator();
            while(it.hasNext()){
                Game game = it.next();
                //TODO this is how notifs will be sent once both front-ends are updated
                //String title = league.getCountry() + ": " + league.getName();
                //String body = game.getCondition() + ": " + game.getMatchName();
                //String topic = leagueId;
                //sendNotification(topic, title, body);
                System.out.println(game);
                boolean notificationsEnabled = false;
                League league = firebaseLeagues.get(game.getLeagueId());
                if(league != null){
                    //notificationsEnabled = league.getEnabled();
                }
                if(notificationsEnabled){
                    if(game.doesMeetConditionOne() || game.doesMeetConditionTwo()){
                        System.out.println( "------\n------\n MATCH\n------\n------\n");
                        String title = league.getCountry() + ": " + league.getName();
                        String body = game.getCondition() + ": " + game.getMatchName();
                        sendNotifications(title, body);
                    }
                }
                //remove a game if it is more than 4 hours old
                if(game.getLastUpdated() < ( System.currentTimeMillis() - 1000*60*60*4) ){
                    it.remove();
                }
            }
            try{
                TimeUnit.SECONDS.sleep(5);
            }catch(InterruptedException e){
                e.printStackTrace();
            };

            System.out.println(dtf.format(LocalDateTime.now()) + ": Refreshing webpage...");
        }
        System.out.println("ScoreChecker thread stopped!");
    }

    public static void sendNotifications(String title, String body){

        // Message Options:
        FcmMessageOptions options = FcmMessageOptions.builder()
                .build();

        // Send a Message:
        Map<String, String> reqBody = new HashMap<>();
        reqBody.put("title", title);
        reqBody.put("body", body);
        fcmMessenger.send(new TopicUnicastMessage(options, new Topic("live_basketball"), reqBody));
    }

    public static League getLeague(WebElement table){
        List<WebElement> leaguesDOM = null;
        League league = new League();

        //get league info from DOM
        try {
            leaguesDOM = table.findElements(By.cssSelector("thead > tr > td.head_ab > span.country.left > span.name"));
        }
        catch(NoSuchElementException | StaleElementReferenceException e){
            e.printStackTrace();
        }

        if(leaguesDOM == null){
            return null;
        }

        for(WebElement leagueDOM : leaguesDOM){
            WebElement countryDOM = null;
            WebElement nameDOM = null;
            try{
                countryDOM = leagueDOM.findElement(By.cssSelector("span.country_part"));
                if(countryDOM != null){
                    league.setCountry(countryDOM.getAttribute("innerHTML"));
                }
            }
            catch(NoSuchElementException | StaleElementReferenceException e){
                e.printStackTrace();
            }
            try{
                nameDOM = leagueDOM.findElement(By.cssSelector("span.tournament_part"));
                if(nameDOM != null){
                    league.setName(nameDOM.getAttribute("innerHTML"));
                }
            }
            catch(NoSuchElementException | StaleElementReferenceException e){
                e.printStackTrace();
            }
        }

        return league;
    }

    // extracts data from the row and pairs it
    // with a corresponding match found in the
    // games hashtable
    public static Game getMatch(WebElement row, League league, Hashtable<String, Game> games){
        boolean isHomeTeam = false;
        boolean isAwayTeam = false;
        String row_id = null;
        try{
            row_id = row.getAttribute("id");
        } catch(StaleElementReferenceException e){
            e.printStackTrace();
        }
        String match_id = null;
        String team = null;
        if(row_id != null){
            team = row_id.substring(0,1); //first character indicates home/away team
            match_id = row_id.substring(2); //remaining str is game id
        }
        if(team != null){
            if(team.equals("g")){
                isHomeTeam = true; isAwayTeam = false;
            }
            else if (team.equals("x")){
                isAwayTeam = true; isHomeTeam = false;
            }
        }
        if(match_id == null){
            return null;
        }
        Game game = games.get(match_id);
        if(game == null){
            game = new Game(match_id, league.getId());
        }
        game.setLastUpdated(System.currentTimeMillis());
        WebElement homeTeamNameDOM = null;
        WebElement awayTeamNameDOM = null;
        WebElement roundStatusDOM = null;
        String homeTeamName = null;
        String awayTeamName = null;
        String roundStatus = null;
        if(isHomeTeam){
            try{
                roundStatusDOM = row.findElement(By.cssSelector("td.timer>span"));
                if(roundStatusDOM != null){
                    roundStatus = (roundStatusDOM.getAttribute("innerHTML"));
                    game.setRoundStatus(roundStatus);
                }
            } catch(NoSuchElementException | StaleElementReferenceException e){
                e.printStackTrace();
            }
        }

        if(isHomeTeam && game.getHomeTeam().isEmpty()){
            try{
                homeTeamNameDOM = row.findElement(By.cssSelector("td.team-home>span"));
                if(homeTeamNameDOM != null){
                    homeTeamName = (homeTeamNameDOM.getAttribute("innerHTML"));
                    game.setHomeTeam(homeTeamName);
                }
            } catch(NoSuchElementException | StaleElementReferenceException e){
                e.printStackTrace();
            }
        }
        if(isAwayTeam && game.getAwayTeam().isEmpty()){
            try{
                awayTeamNameDOM = row.findElement(By.cssSelector("td.team-away>span"));
                if(awayTeamNameDOM != null){
                    awayTeamName = (awayTeamNameDOM.getAttribute("innerHTML"));
                    game.setAwayTeam(awayTeamName);
                }
            } catch(NoSuchElementException | StaleElementReferenceException e){
                e.printStackTrace();
            }
        }
        List<WebElement> scoreDOMs = new ArrayList<WebElement>();
        List<Integer> scores = new ArrayList<Integer>();
        if(isHomeTeam){
            try{
                scoreDOMs = row.findElements(By.cssSelector("td.part-bottom"));
            } catch(NoSuchElementException | StaleElementReferenceException e){
                e.printStackTrace();
            }
        }
        if(isAwayTeam){
            try{
                scoreDOMs =row.findElements(By.cssSelector("td.part-top"));
            } catch(NoSuchElementException | StaleElementReferenceException e){
                e.printStackTrace();
            }
        }

        for(WebElement scoreTd : scoreDOMs ){
            try{
                int score = Integer.parseInt(scoreTd.getAttribute("innerHTML"));
                scores.add(score);
            } catch(NoSuchElementException |
                    StaleElementReferenceException |
                    NumberFormatException
                    e) {
                e.printStackTrace();
            }
        }
        if(isHomeTeam){
            game.setHomeScores(scores);
        }else if(isAwayTeam){
            game.setAwayScores(scores);
        }
        return game;
    }

    /**
     *  this is a helper function that won't likely be in use anywhere.
     * However, it is a nice example of how to use retrofit + firebase REST api
     * @param userId the user id to update
     * @param firebaseLeagueEntry a Map.Entry from a leagueId to a League
     */
    public static void patchLeagueSetting(String userId, LinkedTreeMap.Entry<String, League> firebaseLeagueEntry){
        try {
            HashMap<String, Boolean> body = new HashMap<String, Boolean>();
            body.put(firebaseLeagueEntry.getKey(), false);
            Response res = firebaseService.patchUserLeagueSetting(userId, body).execute();
            if(!res.isSuccessful()){
                System.err.println(res.errorBody().string());
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public static Properties getProperties(String fileName){
        Properties properties = new Properties();
        InputStream input = null;

        try {

            input = Main.class.getClassLoader().getResourceAsStream("config/"+fileName);
            properties.load(input);
            return properties;

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
