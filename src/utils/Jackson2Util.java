package utils;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Jackson2Util {
    private static Logger logger =  Logger.getLogger(Jackson2Util.class.getName());
    private static final String TAG = Jackson2Util.class.getSimpleName();
    private static ObjectMapper mapper = new ObjectMapper();

    public static String objectToJson(Object value) throws JsonProcessingException {
        String jsonStr = mapper.writeValueAsString(value);
        return jsonStr;
    }

    public static <T> T jsonToObject(String jsonStr, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        T obj = mapper.readValue(jsonStr, clazz);
        return obj;
    }

    public static <T> List<T> objectToPOJOList(Object obj, TypeReference tyl) {
        List<T> result = null;
        try {
            result = mapper.convertValue(obj, tyl);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING,  TAG + e.getMessage());
        }
        return result;
    }

    public static <T> String listToJsonString(List<T> list) {
        String str = null;
        try {
            str = mapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            logger.log(Level.WARNING,  TAG + e.getMessage());
        }
        return str;
    }

    public static <T> T objectToPojo(Object obj, Class<T> tClass) {
        T pojo = null;
        try {
            pojo = mapper.convertValue(obj, tClass);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING,  TAG + e.getMessage());
        }
        return pojo;
    }

    public static void objWriteToFile(Object obj, File file) {
        try {
            mapper.writeValue(file, obj);
        } catch (IOException e) {
            logger.log(Level.WARNING,  TAG + e.getMessage());
        }
    }
    public static <T> T readObjFromFile(Class<T> cls, File file) {

        try {
            return  mapper.readValue(file, cls);
        } catch (IOException e) {
            logger.log(Level.WARNING,  TAG + e.getMessage());
        }
        return null;
    }
}