package com.example.busapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class Web {
    public Web() {

    }
    public static String readInStream(InputStream is) throws IOException {
        String f = "";
        try( BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                f += line;
            }
            return f;
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            throw new MalformedURLException("URL is malformed!!");
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new IOException();
        }
    }
    public static String readFromWeb(String webURL) throws IOException {
        URL url = new URL(webURL);
        InputStream is =  url.openStream();
        return readInStream(is);
    }
}
