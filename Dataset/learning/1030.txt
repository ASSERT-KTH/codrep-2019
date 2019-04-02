package com.developmentontheedge.be5.base.services;

import javax.annotation.Nullable;
import java.util.Map;

public interface CoreUtils
{
    @Nullable
    String getSystemSettingInSection(String section, String param);

    @Nullable
    String getSystemSettingInSection(String section, String param, String defValue);

    void setSystemSettingInSection(String section, String param, String value);

    Map<String, String> getSystemSettingsInSection(String section);

    @Nullable
    String getSystemSetting(String param);

    @Nullable
    String getSystemSetting(String param, String defValue);

    boolean getBooleanSystemSetting(String param, boolean defValue);

    boolean getBooleanSystemSetting(String param);

    @Nullable
    String getModuleSetting(String module, String param);

    @Nullable
    String getModuleSetting(String module, String param, String defValue);

    boolean getBooleanModuleSetting(String module, String param, boolean defValue);

    boolean getBooleanModuleSetting(String module, String param);

    @Nullable
    String getUserSetting(String user, String param);

    void setUserSetting(String user, String param, String value);

    void removeUserSetting(String user, String param);

    Map<String, Object> getColumnSettingForUser(String table_name, String query_name, String column_name,
                                        String user_name);
    void setColumnSettingForUser(String table_name, String query_name, String column_name,
                                        String user_name, Map<String, Object> values);
    
void removeColumnSettingForUser(String table_name, String query_name, String column_name,
                                           String user_name);
}
