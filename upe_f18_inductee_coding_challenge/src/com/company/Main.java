package com.company;
import java.io.*;
import java.net.*;
import java.util.Stack;


public class Main {

    //Instance fields for POST request made for getting the token
    public static String tokenAddr = "http://ec2-34-216-8-43.us-west-2.compute.amazonaws.com/session";
    public static String urlParam = "uid=304927387";
    public static String token;

    //Instance fields for GET request made to the game state
    public static int[] mazeSize;
    public static int[] currentLocation;
    public static String status;
    public static Integer levelsCompleted;
    public static Integer totalLevels;

    //Current maze. A 1 means visited, 0 means unvisited
    public static int[][] maze;

    //Main method.
    public static void main(String[] args) throws Exception {
        int i = 0;
        while(i != 12) {
            setToken();
            updateGameStatus();
            initializeMaze();
            solveMaze();
            i++;
        }
    }

    //Makes a GET request to the game and updates the 5 corresponding instance fields
    public static void updateGameStatus() throws Exception {
        //Acquire an Http connection and initialize POST request
        String addr = "http://ec2-34-216-8-43.us-west-2.compute.amazonaws.com/game?token=" + token;
        URL tokenURL = new URL(addr);
        HttpURLConnection tokenConnection = (HttpURLConnection)tokenURL.openConnection();
        tokenConnection.setRequestMethod("GET");
        tokenConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        int responseCode = tokenConnection.getResponseCode();
        //System.out.println("Response Code for GET: " + responseCode);

        //Reading response
        if(responseCode == HttpURLConnection.HTTP_OK)
        {
            InputStream in = tokenConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String responseLine;
            StringBuffer response = new StringBuffer();

            responseLine = reader.readLine();
            while(responseLine != null)
            {
                response.append(responseLine);
                responseLine = reader.readLine();
            }
            reader.close();
            System.out.println(response.toString());

            //Update instance fields
            parseGameState(response.toString());
        }
        else {
            System.out.println("ERROR MAKING GET REQUEST FOR GAME STATE.");
        }
    }

    //Helper function for updateGameStatus()
    private static void parseGameState(String s) {
        //Use ":" as the delimiter for parsing the response body of the game state GET request

        //Initialize array instance fields
        mazeSize = new int[2];
        currentLocation = new int [2];

        //Parse maze_size
        int colonI = s.indexOf(":");
        mazeSize[0] = Integer.parseInt(s.substring(colonI+2, s.indexOf(",", colonI)));
        mazeSize[1] = Integer.parseInt(s.substring(s.indexOf(",", colonI)+1, s.indexOf("]", colonI)));

        //Parse current_location
        colonI = s.indexOf(":", colonI+1);
        currentLocation[0] = Integer.parseInt(s.substring(colonI+2, s.indexOf(",", colonI)));
        currentLocation[1] = Integer.parseInt(s.substring(s.indexOf(",", colonI)+1, s.indexOf("]", colonI)));

        //Parse status
        colonI = s.indexOf(":", colonI+1);
        status = s.substring(colonI+2, s.indexOf("\"", colonI+2));

        //Parse levels_completed
        colonI = s.indexOf(":", colonI+1);
        levelsCompleted = Integer.parseInt(s.substring(colonI+1, s.indexOf(",", colonI)));

        //Parse total_levels
        colonI = s.indexOf(":", colonI+1);
        totalLevels = Integer.parseInt(s.substring(colonI+1,s.length()-1));

        //Sanity check to make sure everything was parsed properly
        //System.out.println("Maze size: [" + mazeSize[0] + "," + mazeSize[1] + "]");
        //System.out.println("Current Location: [" + currentLocation[0] + "," + currentLocation[1] + "]");
        //System.out.println("Status: " + status);
        //System.out.println("Levels Completed: " + levelsCompleted);
        //System.out.println("Total Levels: " + totalLevels);
    }

    //Sets token instance field
    public static void setToken() throws Exception {
        //Acquire an Http connection and initialize POST request
        URL tokenURL = new URL(tokenAddr);
        HttpURLConnection tokenConnection = (HttpURLConnection)tokenURL.openConnection();
        tokenConnection.setRequestMethod("POST");
        tokenConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        tokenConnection.setDoOutput(true);
        OutputStream os = tokenConnection.getOutputStream();
        os.write(urlParam.getBytes());
        os.flush();
        os.close();

        int responseCode = tokenConnection.getResponseCode();
        //System.out.println("Response Code for POST: " + responseCode);

        if(responseCode == HttpURLConnection.HTTP_OK)
        {
            InputStream in = tokenConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String responseLine;
            StringBuffer response = new StringBuffer();

            responseLine = reader.readLine();
            while(responseLine != null)
            {
                response.append(responseLine);
                responseLine = reader.readLine();
            }
            reader.close();
            token = response.toString();
            token = token.substring(token.indexOf(":")+2, token.length()-2);
            System.out.println("Token: " + token);
        }
        else {
            System.out.println("ERROR MAKING POST REQUEST FOR TOKEN");
        }
    }

    public static void solveMaze() throws Exception {
        //Implement DFS to solve the maze.


        //Stack of Cells representing current path
        Stack<int[]> stack = new Stack<int[]>();

        //Mark starting cell as visited
        maze[currentLocation[1]][currentLocation[0]] = 1;
        stack.push(currentLocation);

        //Loop forever until the end is reached
        while(true) {
            //Each iteration of this for loop will process the top of the stack

            //Mark current cell as visited
            maze[currentLocation[1]][currentLocation[0]] = 1;

            //------------------------------------UP-------------------------------------------------------------------
            if(isInBounds(currentLocation[1]-1,currentLocation[0]) && maze[currentLocation[1]-1][currentLocation[0]] != 1) {
                URL url = new URL("http://ec2-34-216-8-43.us-west-2.compute.amazonaws.com/game?token=" + token);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String dir = "action=UP";

                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(dir.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String responseLine;
                    StringBuffer response = new StringBuffer();

                    responseLine = reader.readLine();
                    while (responseLine != null) {
                        response.append(responseLine);
                        responseLine = reader.readLine();
                    }
                    reader.close();
                    String output = parseResult(response.toString());
                    System.out.println(output);
                    if (output.equals("SUCCESS")) {
                        updateGameStatus();
                        System.out.println("New location: " + currentLocation[0] + "," + currentLocation[1]);
                        stack.push(new int[]{currentLocation[0], currentLocation[1]});
                        continue;
                    } else if (output.equals("END")) {
                        return;
                    }
                }
            }
            //------------------------END UP------------------------------------------------------

            //------------------------DOWN--------------------------------------------------------
            if(isInBounds(currentLocation[1]+1, currentLocation[0]) && maze[currentLocation[1]+1][currentLocation[0]] != 1) {
                URL url = new URL("http://ec2-34-216-8-43.us-west-2.compute.amazonaws.com/game?token=" + token);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String dir = "action=DOWN";

                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(dir.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String responseLine;
                    StringBuffer response = new StringBuffer();

                    responseLine = reader.readLine();
                    while (responseLine != null) {
                        response.append(responseLine);
                        responseLine = reader.readLine();
                    }
                    reader.close();
                    String output = parseResult(response.toString());
                    System.out.println(output);
                    if (output.equals("SUCCESS")) {
                        updateGameStatus();
                        System.out.println("New location: " + currentLocation[0] + "," + currentLocation[1]);
                        stack.push(new int[]{currentLocation[0], currentLocation[1]});
                        continue;
                    } else if (output.equals("END")) {
                        return;
                    }
                }
            }
            //------------------------END DOWN----------------------------------------------------

            //----------------------------------------RIGHT----------------------------------------
            if(isInBounds(currentLocation[1], currentLocation[0]+1) && maze[currentLocation[1]][currentLocation[0]+1] != 1) {
                URL url = new URL("http://ec2-34-216-8-43.us-west-2.compute.amazonaws.com/game?token=" + token);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String dir = "action=RIGHT";

                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(dir.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String responseLine;
                    StringBuffer response = new StringBuffer();

                    responseLine = reader.readLine();
                    while (responseLine != null) {
                        response.append(responseLine);
                        responseLine = reader.readLine();
                    }
                    reader.close();
                    String output = parseResult(response.toString());
                    System.out.println(output);
                    if (output.equals("SUCCESS")) {
                        updateGameStatus();
                        System.out.println("New location: " + currentLocation[0] + "," + currentLocation[1]);
                        stack.push(new int[]{currentLocation[0], currentLocation[1]});
                        continue;
                    } else if (output.equals("END")) {
                        return;
                    }
                }
            }
            //---------------------------END RIGHT-------------------------------------------------

            //--------------------------------LEFT-------------------------------------------------
            if(isInBounds(currentLocation[1], currentLocation[0]-1) && maze[currentLocation[1]][currentLocation[0]-1] != 1) {
                URL url = new URL("http://ec2-34-216-8-43.us-west-2.compute.amazonaws.com/game?token=" + token);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String dir = "action=LEFT";

                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(dir.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String responseLine;
                    StringBuffer response = new StringBuffer();

                    responseLine = reader.readLine();
                    while (responseLine != null) {
                        response.append(responseLine);
                        responseLine = reader.readLine();
                    }
                    reader.close();
                    String output = parseResult(response.toString());
                    System.out.println(output);
                    if (output.equals("SUCCESS")) {
                        updateGameStatus();
                        System.out.println("New location: " + currentLocation[0] + "," + currentLocation[1]);
                        stack.push(new int[]{currentLocation[0], currentLocation[1]});
                        continue;
                    } else if (output.equals("END")) {
                        return;
                    }
                }
            }
            //-----------------------------END LEFT------------------------------------------------










            //If this point has been reached, then a dead-end has been reached. Need to backtrack
            stack.pop();
            int[] newTop = stack.peek();
            if(newTop[1] == currentLocation[1]-1){
                //Go UP
                URL url = new URL("http://ec2-34-216-8-43.us-west-2.compute.amazonaws.com/game?token=" + token);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String dir = "action=UP";

                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(dir.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String responseLine;
                    StringBuffer response = new StringBuffer();

                    responseLine = reader.readLine();
                    while (responseLine != null) {
                        response.append(responseLine);
                        responseLine = reader.readLine();
                    }
                    reader.close();
                    String output = parseResult(response.toString());
                    System.out.println(output);
                    if (output.equals("SUCCESS")) {
                        updateGameStatus();
                        System.out.println("New location: " + currentLocation[0] + "," + currentLocation[1]);
                        //stack.push(new int[]{currentLocation[0], currentLocation[1]});
                        continue;
                    } else if (output.equals("END")) {
                        return;
                    }
                }
            }
            else if(newTop[1] == currentLocation[1]+1) {
                //Go DOWN
                URL url = new URL("http://ec2-34-216-8-43.us-west-2.compute.amazonaws.com/game?token=" + token);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String dir = "action=DOWN";

                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(dir.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String responseLine;
                    StringBuffer response = new StringBuffer();

                    responseLine = reader.readLine();
                    while (responseLine != null) {
                        response.append(responseLine);
                        responseLine = reader.readLine();
                    }
                    reader.close();
                    String output = parseResult(response.toString());
                    System.out.println(output);
                    if (output.equals("SUCCESS")) {
                        updateGameStatus();
                        System.out.println("New location: " + currentLocation[0] + "," + currentLocation[1]);
                        //stack.push(new int[]{currentLocation[0], currentLocation[1]});
                        continue;
                    } else if (output.equals("END")) {
                        return;
                    }
                }
            }
            else if(newTop[0] == currentLocation[0]-1){
                //Go LEFT
                URL url = new URL("http://ec2-34-216-8-43.us-west-2.compute.amazonaws.com/game?token=" + token);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String dir = "action=LEFT";

                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(dir.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String responseLine;
                    StringBuffer response = new StringBuffer();

                    responseLine = reader.readLine();
                    while (responseLine != null) {
                        response.append(responseLine);
                        responseLine = reader.readLine();
                    }
                    reader.close();
                    String output = parseResult(response.toString());
                    System.out.println(output);
                    if (output.equals("SUCCESS")) {
                        updateGameStatus();
                        System.out.println("New location: " + currentLocation[0] + "," + currentLocation[1]);
                        //stack.push(new int[]{currentLocation[0], currentLocation[1]});
                        continue;
                    } else if (output.equals("END")) {
                        return;
                    }
                }
            }
            else {
                //Go RIGHT
                URL url = new URL("http://ec2-34-216-8-43.us-west-2.compute.amazonaws.com/game?token=" + token);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String dir = "action=RIGHT";

                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(dir.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String responseLine;
                    StringBuffer response = new StringBuffer();

                    responseLine = reader.readLine();
                    while (responseLine != null) {
                        response.append(responseLine);
                        responseLine = reader.readLine();
                    }
                    reader.close();
                    String output = parseResult(response.toString());
                    System.out.println(output);
                    if (output.equals("SUCCESS")) {
                        updateGameStatus();
                        System.out.println("New location: " + currentLocation[0] + "," + currentLocation[1]);
                        //stack.push(new int[]{currentLocation[0], currentLocation[1]});
                        continue;
                    } else if (output.equals("END")) {
                        return;
                    }
                }
            }

        }
    }

    //Will parse result of movement POST request
    public static String parseResult(String s) {
        return s.substring(s.indexOf(":")+2,s.length()-2);
    }

    //Returns true if the parameter (x,y) is in bounds
    public static boolean isInBounds(int r, int c) {
        return r >= 0 && c >= 0 && c < mazeSize[0] && r < mazeSize[1];
    }

    //Will reset maze, and clear all visited cells
    public static void resetMaze() {
        if(mazeSize != null)
            maze = new int[mazeSize[1]][mazeSize[0]];
    }

    //Since creating a new maze is just the same as resetting a current maze...
    public static void initializeMaze() {
        resetMaze();
    }
}
