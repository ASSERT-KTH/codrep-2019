package com.developmentontheedge.be5.server.services.impl;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.UserInfoProvider;import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.server.helpers.MenuHelper;
import com.developmentontheedge.be5.server.model.Action;
import com.developmentontheedge.be5.server.model.UserInfoModel;
import com.developmentontheedge.be5.server.services.UserInfoModelService;

import javax.inject.Inject;
import java.util.logging.Logger;

public class UserInfoModelServiceImpl implements UserInfoModelService
{
    public static final Logger log = Logger.getLogger(UserInfoModelServiceImpl.class.getName());

    protected final MenuHelper menuHelper;
    protected final UserInfoProvider userInfoProvider;

    @Inject
    public UserInfoModelServiceImpl(MenuHelper menuHelper, UserInfoProvider userInfoProvider)
    {
        this.menuHelper = menuHelper;
        this.userInfoProvider = userInfoProvider;
    }

    @Override
    public UserInfoModel getUserInfoModel()
    {
        UserInfo userInfo = userInfoProvider.get();
        return new UserInfoModel(
                !RoleType.ROLE_GUEST.equals(userInfo.getUserName()),
                userInfo.getUserName(),
                userInfo.getAvailableRoles(),
                userInfo.getCurrentRoles(),
                userInfo.getCreationTime().toInstant(),
                getDefaultAction()
        );
    }

    protected String getDefaultAction()
    {
        Action defaultAction = menuHelper.getDefaultAction();
        String defaultRouteCall = "";
        if (defaultAction == null)
        {
            log.severe("Default Action must not be null");
        }
        else
        {
            if (defaultAction.getName().equals("call"))
            {
                defaultRouteCall = defaultAction.getArg();
            }
            else
            {
                log.severe("Default Action type must be 'call'");
            }
        }
        return defaultRouteCall;
    }
}
