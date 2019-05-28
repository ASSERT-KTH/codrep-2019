package com.developmentontheedge.be5.server.util;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.operation.util.OperationUtils;
import com.developmentontheedge.be5.server.model.Base64File;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Collections ;
import java.util.LinkedHashMap;
import java.util.Map;

public class ParseRequestUtils
{
    public static Map<String, Object> getValuesFromJson(String valuesString) throws Be5Exception
    {
        if (Strings.isNullOrEmpty(valuesString))
        {
            return Collections.emptyMap();
        }

        Map<String, Object> fieldValues = new LinkedHashMap<>();


        //todo gson -> json-b
        //            InputStream stream = new ByteArrayInputStream(valuesString.getBytes(StandardCharsets.UTF_8.name()));
//            javax.json.stream.JsonParser parser = Json.createParser(stream);
//
//            javax.json.JsonObject object = parser.getObject();
//            Set<Map.Entry<String, JsonValue>> entries = object.entrySet();


        JsonObject values = (JsonObject) new JsonParser().parse(valuesString);
        for (Map.Entry entry : values.entrySet())
        {
            String name = entry.getKey().toString();
            if (entry.getValue() instanceof JsonNull)
            {
                fieldValues.put(name, null);
            }
            else if (entry.getValue() instanceof JsonArray)
            {
                JsonArray value = (JsonArray) entry.getValue();

                String[] arrValues = new String[value.size()];
                for (int i = 0; i < value.size(); i++)
                {
                    arrValues[i] = value.get(i).getAsString();
                }

                fieldValues.put(name, arrValues);
            }
            else if (entry.getValue() instanceof JsonObject
                    || (entry.getValue() instanceof JsonPrimitive &&
                        ((JsonPrimitive) entry.getValue()).getAsString().contains("\"type\":\"Base64File\"")))
            {
                JsonObject jsonObject;
                if (entry.getValue() instanceof JsonPrimitive)
                {
                    String value = ((JsonPrimitive) entry.getValue()).getAsString();
                    jsonObject = (JsonObject) new JsonParser().parse(value);
                }
                else
                {
                    jsonObject = ((JsonObject) entry.getValue());
                }
                String type = jsonObject.get("type").getAsString();
                if ("Base64File".equals(type))
                {
                    try
                    {
                        String data = jsonObject.get("data").getAsString();
                        String base64 = ";base64,";
                        int base64Pos = data.indexOf(base64);

                        String mimeTypes = data.substring("data:".length(), base64Pos);
                        byte[] bytes = data.substring(base64Pos + base64.length(), data.length()).getBytes("UTF-8");

                        byte[] decoded = Base64.getDecoder().decode(bytes);

                        fieldValues.put(name, new Base64File(jsonObject.get("name").getAsString(), decoded, mimeTypes));
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        throw Be5Exception.internal(e);
                    }
                }
                else
                {
                    fieldValues.put(name, jsonObject.toString());
                }
            }
            else if (entry.getValue() instanceof JsonPrimitive)
            {
                fieldValues.put(name, ((JsonPrimitive) entry.getValue()).getAsString());
            }

        }

        return OperationUtils.replaceEmptyStringToNull(fieldValues);
    }
}
