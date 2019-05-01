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

import java.io.PrintWriter;
import java.lang.reflect.Method;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Test Servlet Cookies.
 */
@SuppressWarnings("serial")
@WebServlet(name = "Jetty", value = "/jetty")
public class JettyDump extends HttpServlet
{
    /* ------------------------------------------------------------ */
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException
    {
        try
        {
            response.setContentType("text/html");

            PrintWriter out = response.getWriter();
            out.println("<h1>Jetty Dump Servlet:</h1>");
            out.println("<a href=\"/\">home</a><br/>");
            out.println("<pre>");

            ServletContext context = request.getServletContext();
            Method method = context.getClass().getMethod("getContextHandler");
            Object handler = method.invoke(context);
            method = handler.getClass().getMethod("getServer");
            Object server = method.invoke(handler);
            method = server.getClass().getMethod("dump");
            out.println(method.invoke(server));

            out.println("</pre>");

        }
        catch(Exception e)
        {
            throw new ServletException(e);
        }
    }
}
