package com.developmentontheedge.be5.server.helpers;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.server.SessionConstants;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Session;
import com.google.inject.Stage;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;


public class UserHelper
{
    public final Logger log = Logger.getLogger(UserHelper.class.getName());

    private final Meta meta;
    private final Stage stage;
    private final Provider<Request> requestProvider;

    @Inject
    public UserHelper(Meta meta, Stage stage, Provider<Request> requestProvider)
    {
        this.meta = meta;
        this.stage = stage;
        this.requestProvider = requestProvider;
    }

    public UserInfo saveUser(String userName, List<String> availableRoles, List<String> currentRoles,
                             Locale locale, String remoteAddr)
    {
        UserInfo ui;
        if (stage != Stage.PRODUCTION && ModuleLoader2.getDevRoles().size() > 0)
        {
            Set<String> devAvailableRoles = new LinkedHashSet<String>() {{
                addAll(availableRoles);
                addAll(ModuleLoader2.getDevRoles());
            }};
            Set< String> devCurrentRoles = new LinkedHashSet<String>() {{
                addAll(currentRoles);
                addAll(ModuleLoader2.getDevRoles());
            }};

            ui = new UserInfo(userName, devAvailableRoles, devCurrentRoles);

            log.info("Dev roles added - " + ModuleLoader2.getDevRoles().toString());
        }
        else
        {
            ui = new UserInfo(userName, availableRoles, currentRoles);
        }

        ui.setRemoteAddr(remoteAddr);
        ui.setLocale(meta.getLocale(locale));

        Session session = requestProvider.get().getSession();

        session.set("remoteAddr", remoteAddr);
        session.set(SessionConstants.USER_INFO, ui);
        session.set(SessionConstants.CURRENT_USER, ui.getUserName());

        return ui;
    }

    public void logout()
    {
        Session session = requestProvider.get().getSession();
        UserInfo userInfo = (UserInfo) session.get(SessionConstants.USER_INFO);
        String username = userInfo.getUserName();

        session.invalidate();
        initGuest();

        log.info("Logout user: " + username);
    }

    public void initGuest()
    {
        Request req = requestProvider.get();
        Objects.requireNonNull(req);

        List<String> roles = Collections.singletonList(RoleType.ROLE_GUEST);

        saveUser(RoleType.ROLE_GUEST, roles, roles, req.getLocale(), req.getRemoteAddr());
    }

}
