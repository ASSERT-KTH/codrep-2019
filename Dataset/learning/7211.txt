package com.developmentontheedge.be5.base.model;

import java.util.HashMap;
import java.util.Map;


/**
 * @see com.developmentontheedge.beans.BeanInfoConstants
 */
public class DPSAttributes
{
    private Map<String, Object> map = new HashMap<>();

    private String name;

    public void setName(String value)
    {
        name = value;
    }

    public void setDISPLAY_NAME(String value)
    {
        map.put("DISPLAY_NAME", value);
    }

    public void setTYPE(Class<?> value)
    {
        map.put("TYPE", value);
    }

    public void setValue(Object value)
    {
        map.put("value", value);
    }

    public void setREAD_ONLY(boolean value)
    {
        map.put("READ_ONLY", value);
    }

    public void setHIDDEN(boolean value)
    {
        map.put("HIDDEN", value);
    }

    public void setRAW_VALUE(boolean value)
    {
        map.put("RAW_VALUE", value);
    }

    public void setRELOAD_ON_CHANGE(boolean value)
    {
        map.put("RELOAD_ON_CHANGE", value);
    }

    public void setRELOAD_ON_FOCUS_OUT(boolean value)
    {
        map.put("RELOAD_ON_FOCUS_OUT", value);
    }

    public void setCAN_BE_NULL(boolean value)
    {
        map.put("CAN_BE_NULL", value);
    }

    public void setMULTIPLE_SELECTION_LIST(boolean value)
    {
        map.put("MULTIPLE_SELECTION_LIST", value);
    }

    public void setPASSWORD_FIELD(boolean value)
    {
        map.put("PASSWORD_FIELD", value);
    }

    public void setLABEL_FIELD(boolean value)
    {
        map.put("LABEL_FIELD", value);
    }

    public void setTAG_LIST_ATTR(Object value)
    {
        map.put("TAG_LIST_ATTR", value);
    }

    public void setEXTRA_ATTRS(Object value)
    {
        map.put("EXTRA_ATTRS", value);
    }

    public void setVALIDATION_RULES(Object value)
    {
        map.put("VALIDATION_RULES", value);
    }

    public void setCOLUMN_SIZE_ATTR(Object value)
    {
        map.put("COLUMN_SIZE_ATTR", value);
    }

    public void setINPUT_SIZE_ATTR(Object value)
    {
        map.put("INPUT_SIZE_ATTR", value);
    }

    public void setPLACEHOLDER(String value)
    {
        map.put("PLACEHOLDER", value);
    }

    public void setGROUP_ID(Object value)
    {
        map.put("GROUP_ID", value);
    }

    public void setGROUP_NAME(String value)
    {
        map.put("GROUP_NAME", value);
    }

    public void setGROUP_CLASSES(String value)
    {
        map.put("GROUP_CLASSES", value);
    }

    public void setDEFAULT_VALUE(Object value)
    {
        map.put("DEFAULT_VALUE", value);
    }

    public void setCSS_CLASSES(String value)
    {
        map.put("CSS_CLASSES", value);
    }

    public void setSTATUS(String value)
    {
        map.put("STATUS", value);
    }

    public void setMESSAGE
(String value)
    {
        map.put("MESSAGE", value);
    }

    public Map<String, Object> getMap()
    {
        return map;
    }

    public String getName()
    {
        return name;
    }

    public Class<?> getTYPE()
    {
        return (Class<?>) map.getOrDefault("TYPE", String.class);
    }

}
