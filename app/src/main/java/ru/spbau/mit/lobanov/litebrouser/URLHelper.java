package ru.spbau.mit.lobanov.litebrouser;

import android.webkit.URLUtil;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Артём on 25.06.2017.
 */

public final class URLHelper {

    private static final String ADDRESS_REGEXP = "^[a-zA-Zа-яА-ЯёЁ]\\S*\\.[a-zA-Zа-яА-ЯеЁ]{2,6}$";
    private static final String SEARCH_REQUEST = "https://yandex.ru/search/?text=";

    private URLHelper(){
    }

    public static String createURL(String request) throws UnsupportedEncodingException {
        if (URLUtil.isValidUrl(request)) { // its valid URL
            return request;
        } else if (request.matches(ADDRESS_REGEXP)) { // looks like web address
            return "http://" + request;
        } else {
            return SEARCH_REQUEST + URLEncoder.encode(request, "UTF-8");
        }
    }
}
