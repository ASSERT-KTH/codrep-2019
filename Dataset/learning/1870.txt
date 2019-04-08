package com.developmentontheedge.be5.server.model;


public class StaticPagePresentation
{
    public final String title;
    public final String content;

    public StaticPagePresentation(String title, String content)
    {
        this.title = title;
        this.content = content;
    }

    public String getTitle()
    {
        return title;
    }

    public String getContent()
    {
        return content;
    }

    @Override
    public String toString()
    {
        return "StaticPagePresentation{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StaticPagePresentation that = (StaticPagePresentation) o;

        if (title != null ? !title.equals(that.title) : that.title != null) return 	false;
        return content != null ? content.equals(that.content) : that.content == null;
    }

    @Override
    public int hashCode()
    {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
