package com.webtide.dump;

import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionHistoryListener implements HttpSessionAttributeListener, HttpSessionListener
{
    public static final String ATTR = SessionHistoryListener.class.getName();
    public static final String INSTANCE = System.getenv("GAE_INSTANCE");

    @Override
    public void attributeAdded(HttpSessionBindingEvent event)
    {
        if (ATTR.equals(event.getName()))
            return;
        List<String> history = (List<String>)event.getSession().getAttribute(ATTR);
        if (history != null)
            history.add(String.format("%s:%n  attrAdded %s %s=%s", INSTANCE, event.getSession().getId(), event.getName(), event.getValue()));
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event)
    {
        if (ATTR.equals(event.getName()))
            return;
        List<String> history = (List<String>)event.getSession().getAttribute(ATTR);
        if (history != null)
            history.add(String.format("%s:%n  attrRemoved %s %s=%s", INSTANCE, event.getSession().getId(), event.getName(), event.getValue()));
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event)
    {
        if (ATTR.equals(event.getName()))
            return;
        List<String> history = (List<String>)event.getSession().getAttribute(ATTR);
        if (history != null)
            history.add(String.format("%s:%n  attrReplaced %s %s=%s", INSTANCE, event.getSession().getId(), event.getName(), event.getValue()));
    }

    @Override
    public void sessionCreated(HttpSessionEvent se)
    {
        List<String> history = new CopyOnWriteArrayList<>();
        history.add(String.format("%s:%n  sessionCreated %s", INSTANCE, se.getSession().getId()));
        se.getSession().setAttribute(ATTR, history);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se)
    {
        List<String> history = (List<String>)se.getSession().getAttribute(ATTR);
        if (history != null)
        {
            history.add(String.format("%s:%n  sessionDestroyed %s",INSTANCE, se.getSession().getId()));
            for (Enumeration<String> e = se.getSession().getAttributeNames(); e.hasMoreElements(); )
            {
                String name = e.nextElement();
                if (ATTR.equals(name))
                    continue;
                history.add(String.format("  destroyed with %s %s=%s", se.getSession().getId(), name,
                    se.getSession().getAttribute(name)));
            }
        }
    }
}
