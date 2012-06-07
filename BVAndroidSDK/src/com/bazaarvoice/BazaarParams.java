package com.bazaarvoice;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: gary
 * Date: 5/6/12
 * Time: 9:31 PM
 *
 * This class helps construct parameters for a URL request for BazaarVoice requests
 */
public abstract class BazaarParams {

    protected Media media;
    private String encryptedUser;

    /**
     * Convert the class into a url parameters string
     * @param url the base url to append to
     * @return the url with parameters
     */
    public abstract String toURL(String url);

    /**
     * Get the media file associated with these parameters
     * @return the media file or null
     */
    public Media getMedia() {
        return media;
    }


    /**
     * Encrypt the user based on the auth key, the userId, and a date
     * @param userAuthKey the user authentication key
     * @param date the date used to encode the user
     * @param userId the user id
     */
    public void setEncryptUser(String userAuthKey, Date date, String userId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String userStr = "date="+sdf.format(date)+"&userid="+userId;

        String md5 = Utils.getMD5(userAuthKey + userStr);
        encryptedUser = md5 + new String(Utils.encodeHex(userStr.getBytes()));
    }

    /**
     * Encrypt the user based on the auth key, user id and today's date
     * @param userAuthKey
     * @param userId
     */
    public void setEncryptUser(String userAuthKey, String userId) {
        setEncryptUser(userAuthKey, new Date(), userId);
    }


    /**
     * @return the encrypted user id
     */
    public String getEncryptedUser() {
        return encryptedUser;
    }

    /**
     * Helper method for ensuring a map exists and adding a value to it.
     * @param map the map
     * @param type the key
     * @param value the value
     * @return map if map != null, a new map otherwise
     */
    protected Map<String, String> addToMap(Map<String,String> map, String type, String value) {
        if (map == null)
        {
            map = new HashMap<String, String>();
        }

        String oldValue = map.get(type);
        if (oldValue != null)
        {
            value = oldValue + ":" + value;
        }
        map.put(type, value);
        return map;
    }

    /**
     * Add a parameter to the current url.
     * @param url the current url with or without parameters
     * @param name the name of the parameter to add
     * @param value the value
     * @return the url with the parameter added
     */
    static public String addURLParameter(String url, String name, String value) {
        if (value != null && value.length() > 0) {
            value = encode(value);
            char separator = url.contains("?")?'&':'?';
            url += separator + name + '=' + value;
        }
        return url;
    }

    /**
     * Add a boolean parameter to the current url as long as the value is not null
     * @param url the current url with or without parameters
     * @param name the name of the parameter to add
     * @param value the value
     * @return the url with the parameter added
     */
    static public String addURLParameter(String url, String name, Boolean value) {
        if (value != null) {
            return addURLParameter(url,name,value.toString());
        }
        return url;
    }

    /**
     * Add an integer parameter to the current url as long as the value is not null
     * @param url the current url with or without parameters
     * @param name the name of the parameter to add
     * @param value the value
     * @return the url with the parameter added
     */
    static public String addURLParameter(String url, String name, Integer value) {
        if (value != null) {
            return addURLParameter(url,name,value.toString());
        }
        return url;
    }

    /**
     * Add a map of parameters to the current url in the form "name_key1=value1&name_key2=value2"
     * @param url the current url with or without parameters
     * @param name the name of the parameter to add
     * @param map the map of values to add
     * @return the url with the parameter added
     */
    static public String addURLParameter(String url, String name, Map<String, String> map) {
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                url = addURLParameter(url, name + "_" + key, value);
            }
        }
        return url;
    }


    /**
     * URL encode the value
     * @param value the value to encode
     * @return encoded value
     */
    static protected String encode(String value) {
        return URLEncoder.encode(value);
    }

    /**
     * add parameters to the url from a list in the form "name=value1,value2,value3"
     * @param url the current url with or without parameters
     * @param name the name of the parameter to add
     * @param values the list of values that will be separated by commas
     * @return the new url
     */
    static public String addURLParamsFromList(String url, String name, List<String> values) {
        if (values != null) {
            String paramList = "";
            boolean first = true;
            for (String value : values) {
                if (first) {first=false;}
                else {paramList += ",";}

                paramList += value;
            }
            return addURLParameter(url,name,paramList);
        }
        return url;
    }

}