//
//  ========================================================================
//  Copyright (c) 1995-2018 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package com.webtide.dump;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;

/** 
 * Test Servlet Sessions.
 */
@SuppressWarnings("serial")
@WebServlet(name = "Session", value = "/session", loadOnStartup = 1)
public class SessionDump extends HttpServlet
{
    /** 
      * Simple object attribute to test serialization
      */
    public class ObjectAttributeValue implements java.io.Serializable
    {
        long l;
        
        public ObjectAttributeValue(long l)
        {
            this.l = l;
        }

        public long getValue()
        {
            return l;
        }
    }

    public static class Listener implements javax.servlet.http.HttpSessionAttributeListener, javax.servlet.http.HttpSessionListener
    {
        public List<Object> history = new CopyOnWriteArrayList<>();

        @Override
        public void attributeAdded(HttpSessionBindingEvent event)
        {
            history.add(String.format("attrAdded %s %s=%s", event.getSession().getId(), event.getName(), event.getValue()));
        }

        @Override
        public void attributeRemoved(HttpSessionBindingEvent event)
        {
            history.add(String.format("attrRemoved %s %s=%s", event.getSession().getId(), event.getName(), event.getValue()));
            history.add(new Throwable());
        }

        @Override
        public void attributeReplaced(HttpSessionBindingEvent event)
        {
            history.add(String.format("attrReplaced %s %s=%s", event.getSession().getId(), event.getName(), event.getValue()));
        }

        @Override
        public void sessionCreated(HttpSessionEvent se)
        {
            history.add(String.format("sessionCreated %s", se.getSession().getId()));
        }

        @Override
        public void sessionDestroyed(HttpSessionEvent se)
        {
            history.add(String.format("sessionDestroyed %s", se.getSession().getId()));
            for (Enumeration<String> e = se.getSession().getAttributeNames(); e.hasMoreElements();)
            {
                String name = e.nextElement();
                history.add(String.format("destroyed with %s %s=%s", se.getSession().getId(), name, se.getSession().getAttribute(name)));
            }
        }
    }

    int redirectCount=0;
    /* ------------------------------------------------------------ */
    String pageType;
    Listener listener = new Listener();

    /* ------------------------------------------------------------ */
    @Override
    public void init(ServletConfig config)
         throws ServletException
    {
        super.init(config);
        getServletContext().addListener(listener);
    }

    /* ------------------------------------------------------------ */
    protected void handleForm(HttpServletRequest request,
                          HttpServletResponse response)
    {
        HttpSession session = request.getSession(false);
        String action = request.getParameter("Action");
        String name =  request.getParameter("Name");
        String value =  request.getParameter("Value");

        if (action!=null)
        {
            if(action.equals("New Session"))
            {
                session = request.getSession(true);
                session.setAttribute("test","value");
                session.setAttribute("obj", new ObjectAttributeValue(System.currentTimeMillis()));
            }
            else if (session!=null)
            {
                if (action.equals("Invalidate"))
                    session.invalidate();
                else if (action.equals("Set") && name!=null && name.length()>0)
                    session.setAttribute(name,value);
                else if (action.equals("Remove"))
                    session.removeAttribute(name);
            }
        }
    }

    /* ------------------------------------------------------------ */
    @Override
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
        throws ServletException, IOException
    {
        handleForm(request,response);
        String nextUrl = getURI(request)+"?R="+redirectCount++;
        String encodedUrl=response.encodeRedirectURL(nextUrl);
        response.sendRedirect(encodedUrl);
    }

    /* ------------------------------------------------------------ */
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException
    {
        handleForm(request,response);

        response.setContentType("text/html");

        HttpSession session = request.getSession(getURI(request).indexOf("new")>0);
        try
        {
            if (session!=null)
                session.isNew();
        }
        catch(IllegalStateException e)
        {
            session=null;
        }

        PrintWriter out = response.getWriter();
        out.println("<h1>Session Dump Servlet:</h1>");
        out.println("<a href=\"/\">home</a><br/>");
        out.println("<form action=\""+response.encodeURL(getURI(request))+"\" method=\"post\">");

        if (session==null)
        {
            out.println("<H3>No Session</H3>");
            out.println("<input type=\"submit\" name=\"Action\" value=\"New Session\"/>");
        }
        else
        {
            try
            {
                out.println("<b>ID:</b> "+session.getId()+"<br/>");
                out.println("<b>New:</b> "+session.isNew()+"<br/>");
                out.println("<b>Created:</b> "+new Date(session.getCreationTime())+"<br/>");
                out.println("<b>Last:</b> "+new Date(session.getLastAccessedTime())+"<br/>");
                out.println("<b>Max Inactive:</b> "+session.getMaxInactiveInterval()+"<br/>");
                out.println("<b>Context:</b> "+session.getServletContext()+"<br/>");


                Enumeration<String> keys=session.getAttributeNames();
                while(keys.hasMoreElements())
                {
                    String name=(String)keys.nextElement();
                    String value=""+session.getAttribute(name);

                    out.println("<b>"+name+":</b> "+value+"<br/>");
                }

                out.println("<b>Name:</b><input type=\"text\" name=\"Name\" /><br/>");
                out.println("<b>Value:</b><input type=\"text\" name=\"Value\" /><br/>");

                out.println("<input type=\"submit\" name=\"Action\" value=\"Set\"/>");
                out.println("<input type=\"submit\" name=\"Action\" value=\"Remove\"/>");
                out.println("<input type=\"submit\" name=\"Action\" value=\"Refresh\"/>");
                out.println("<input type=\"submit\" name=\"Action\" value=\"Invalidate\"/><br/>");

                out.println("</form><br/>");

                if (request.isRequestedSessionIdFromCookie())
                    out.println("<P>Turn off cookies in your browser to try url encoding<BR>");

                if (request.isRequestedSessionIdFromURL())
                    out.println("<P>Turn on cookies in your browser to try cookie encoding<BR>");
                out.println("<a href=\""+response.encodeURL(request.getRequestURI()+"?q=0")+"\">Encoded Link</a><BR>");

            }
            catch (IllegalStateException e)
            {
                e.printStackTrace();
            }
        }

        out.println("</form><h2>History</h2><pre>");
        for (Object o : listener.history)
        {
            if (o instanceof Throwable)
                ((Throwable)o).printStackTrace(out);
            else
                out.println(o);
        }
        out.println("</pre>");
    }

    /* ------------------------------------------------------------ */
    @Override
    public String getServletInfo() {
        return "Session Dump Servlet";
    }

    /* ------------------------------------------------------------ */
    private String getURI(HttpServletRequest request)
    {
        String uri=(String)request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        if (uri==null)
            uri=request.getRequestURI();
        return uri;
    }

}
