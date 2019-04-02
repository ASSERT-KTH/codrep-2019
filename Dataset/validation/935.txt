package com.developmentontheedge.be5.server.servlet;

import com.developmentontheedge.be5. base.services.ProjectProvider;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.server.helpers.UserHelper;
import com.developmentontheedge.be5.server.services.HtmlMetaTags;
import com.developmentontheedge.be5.server.servlet.support.FilterSupport;
import com.developmentontheedge.be5.server.servlet.support.ServletUtils;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;


public class TemplateFilter extends FilterSupport
{
    private ServletContext servletContext;
    private TemplateEngine templateEngine;

    private final HtmlMetaTags htmlMetaTags;
    private final UserHelper userHelper;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public TemplateFilter(UserHelper userHelper, ProjectProvider projectProvider,
                          HtmlMetaTags htmlMetaTags, UserInfoProvider userInfoProvider)
    {
        this.userHelper = userHelper;
        this.htmlMetaTags = htmlMetaTags;
        this.userInfoProvider = userInfoProvider;

        projectProvider.addToReload(() -> templateEngine.clearTemplateCache());
    }

    @Override
    public void init(FilterConfig filterConfig)
    {
        servletContext = filterConfig.getServletContext();
        this.templateEngine = new TemplateEngine();

        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L);
        templateResolver.setCacheable(true);
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    public void filter(Request req, Response res, FilterChain chain) throws IOException, ServletException
    {
        if (userInfoProvider.get() == null)
        {
            userHelper.initGuest();
        }
        String reqWithoutContext = getRequestWithoutContext(req.getContextPath(), req.getRequestUri());
        if (servletContext.getResourceAsStream("/WEB-INF/templates" + reqWithoutContext + "index.html") == null)
        {
            chain.doFilter(req.getRawRequest(), res.getRawResponse());
        }
        else
        {
            ServletUtils.addHeaders(req.getRawRequest(), res.getRawResponse());
            res.sendHtml(templateEngine.process(reqWithoutContext + "index", getContext(req)));
        }
    }

    private static String getRequestWithoutContext(String contextPath, String requestUri)
    {
        String reqWithoutContext = requestUri.replaceFirst(contextPath, "");
        if (!reqWithoutContext.endsWith("/")) reqWithoutContext += "/";
        return reqWithoutContext;
    }

    private Context getContext(Request req)
    {
        Context context = new Context();
        context.setVariables(htmlMetaTags.getTags(req));
        context.setVariable("requestUrl", req.getRequestUri());
        context.setVariable("contextPath", req.getContextPath());
        return context;
    }

}
