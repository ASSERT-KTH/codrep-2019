package com.developmentontheedge.be5.server.services.impl;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.web.Session;
import com.developmentontheedge.be5.server.SessionConstants;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Locale;


public class UserInfoProviderImpl implements UserInfoProvider, Provider<UserInfo>
{
    private final Provider<Session> session;

    @Inject
    public UserInfoProviderImpl(Provider<Session> session)
    {
        this.session = session;
    }

    @Override
    public UserInfo get()
    {
        return (UserInfo) session.get().get(SessionConstants.USER_INFO);
    }

    @Override
    public String getLanguage()
    {
        return getLocale().getLanguage().toLowerCase();
    }

    @Override
    public Locale getLocale()
    {
        return get().getLocale();
    }

    
@Override
    public String getUserName()
    {
        return get().getUserName();
    }

    @Override
    public boolean isLoggedIn()
    {
        return !RoleType.ROLE_GUEST.equals(get().getUserName());
    }

    @Override
    public List<String> getAvailableRoles()
    {
        return get().getAvailableRoles();
    }

    @Override
    public List<String> getCurrentRoles()
    {
        return get().getCurrentRoles();
    }

    @Override
    public String getRemoteAddr()
    {
        return get().getRemoteAddr();
    }
}
