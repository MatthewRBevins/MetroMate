package com.metromate.busapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Web {
    static JSONParser parser = new JSONParser();

    /**
     * Fetch data from InputStream
     * @param is InputStream to read
     * @return String of read data
     * @throws IOException Reading InputStream
     */
    public static String readInStream(InputStream is) throws IOException {
        String f = "";
        try( BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            //Loop through all lines in InputStream
            String line;
            while ((line = br.readLine()) != null) {
                //Add current line to final string
                f += line;
            }
            return f;
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            throw new MalformedURLException("URL is malformed");
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new IOException();
        }
    }

    /**
     * Creates InputStream based on given web URL
     * @param webURL Web URL to read
     * @return Read InputStream of Web URL
     * @throws IOException Reading InputStream
     */
    public static String readFromWeb(String webURL) throws IOException {
        URL url = new URL(webURL);
        InputStream is =  url.openStream();
        return readInStream(is);
    }

    /**
     * Parse JSON based on a given stream reader
     * @param json Stream reader
     * @return JSONObject of parsed reader
     * @throws ParseException Parsing JSON
     * @throws IOException Opening reader
     */
    public static JSONObject readJSON(Reader json) throws ParseException, IOException {
        return (JSONObject) parser.parse(json);
    }
}
