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

package com.webtide.gae;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.TimeUnit;

/**
 * Test Servlet Cookies.
 */
@SuppressWarnings("serial")
@WebServlet(name = "ClassLoader", value = "/classloader/*")
public class ClassLoaderDump extends HttpServlet
{
    /* ------------------------------------------------------------ */
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException
    {
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        out.println("<h1>ClassLoader Dump Servlet:</h1>");
        out.println("<a href=\"/\">home</a><br/>");

        if (request.getPathInfo()!=null)
        {
            try
            {
                Class<?> info = Thread.currentThread().getContextClassLoader().loadClass(request.getPathInfo().substring(1));
                out.println("<h3>Loader for:"+info+"</h3>");
                dump(out,info.getClassLoader());
            }
            catch (Throwable th)
            {
                out.println("<h3>Could not load "+request.getPathInfo()+":</h3>");
                out.println("<pre>");
                th.printStackTrace(out);
                out.println("</pre>");
            }
        }

        Class<?> jre = String.class;
        out.println("<h3>JRE Loader for:"+jre+"</h3>");
        dump(out,jre.getClassLoader());

        Class<?> webapp = this.getClass();
        out.println("<h3>WebApp Loader for:"+webapp+"</h3>");
        dump(out,webapp.getClassLoader());

        out.println("<h3>Thread Context Loader for:"+Thread.currentThread()+"</h3>");
        dump(out,Thread.currentThread().getContextClassLoader());

        Class<?> container = ServletException.class;
        out.println("<h3>Container Loader for:"+container+"</h3>");
        dump(out,container.getClassLoader());

        Class<?> google = com.google.appengine.api.utils.SystemProperty.class;
        out.println("<h3>Google Loader for:"+google+"</h3>");
        dump(out,google.getClassLoader());
    }

    private void dump(PrintWriter out, ClassLoader loader)
    {
        if (loader==null)
        {
            out.println("SYSTEM LOADER<br/>");
            return;
        }

        out.println(loader);
        out.println("<ul>");
        if (loader instanceof URLClassLoader)
        {
            for (URL url : ((URLClassLoader)loader).getURLs())
                out.println("<li>"+url+"</li>");
        }
        if (loader.getParent()!=null)
        {
            out.println("<li>");
            dump(out,loader.getParent());
            out.println("</li>");
        }
        out.println("</ul>");
    }


}
