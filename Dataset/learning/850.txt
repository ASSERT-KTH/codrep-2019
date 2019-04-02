package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.model.Project;
import com.developmentontheedge.be5.server.servlet.support.JsonApiController;
import com.developmentontheedge.be5.web.Controller;
import com.developmentontheedge.be5.web.Request;
import one.util.streamex.StreamEx;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class LanguageSelectorController extends JsonApiController implements Controller
{
    private final Meta meta;
    private final ProjectProvider projectProvider;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public LanguageSelectorController(Meta meta, ProjectProvider projectProvider, UserInfoProvider userInfoProvider1)
    {
        this.meta = meta;
        this.projectProvider = projectProvider;
        this.userInfoProvider = userInfoProvider1;
    }

    public static class LanguageSelectorResponse
    {

        public final List<String> languages;
        public final String selected;
        public final Map<String, String> messages;

        public LanguageSelectorResponse(List<String> languages, String selected, Map<String, String> messages)
        {
            this.languages = languages;
            this.selected = selected;
            this.messages = messages;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LanguageSelectorResponse that = (LanguageSelectorResponse) o;

            if (languages != null ? !languages.equals(that.languages) : that.languages != null) return false;
            if (selected != null ? !selected.equals(that.selected) : that.selected != null) return false;
            return messages != null ? messages.equals(that.messages) : that.messages == null;
        }

        public List<String> getLanguages()
        {
            return languages;
        }

        public String getSelected()
        {
            return selected;
        }

        public Map<String, String> getMessages()
        {
            return messages;
        }

        @Override
        public String toString()
        {
            return "LanguageSelectorResponse{" +
                    "languages=" + languages +
                    ", selected='" + selected + '\'' +
                    ", messages=" + messages +
                    '}';
        }
    }

    @Override
    public Object generate(Request req, String requestSubUrl)
    {
        switch (requestSubUrl)
        {
            case "":
                return getInitialData();
            case "select":
                return selectLanguage(req);
            default:
                return null;
        }
    }

    private LanguageSelectorResponse getInitialData()
    {
        return getState();
    }

    private LanguageSelectorResponse selectLanguage(Request req)
    {
        Locale language = meta.getLocale(new Locale(req.getNonEmpty("language")));
        userInfoProvider.get(). setLocale(language);

        return getState();
    }

    private LanguageSelectorResponse getState()
    {
        Project project = projectProvider.get();

        List<String> languages = Arrays.stream(project.getLanguages()).map(String::toUpperCase).collect(Collectors.toList());

        String selectedLanguage = userInfoProvider.get().getLanguage().toUpperCase();
        Map<String, String> messages = readMessages(project, selectedLanguage);

        return new LanguageSelectorResponse(languages, selectedLanguage, messages);
    }

    private Map<String, String> readMessages(Project project, String language)
    {
        Map<String, String> messages = new HashMap<>();

        StreamEx.of(project.getModulesAndApplication())
                .map(m -> m.getLocalizations().get(language.toLowerCase())).nonNull()
                .map(ll -> ll.get("frontend.l10n")).nonNull()
                .flatMap(el -> el.getRows().stream())
                .filter(row -> row.getTopic().equals(DatabaseConstants.L10N_TOPIC_PAGE))
                .forEach(row -> messages.put(row.getKey(), row.getValue()));

        return messages;
    }

}
