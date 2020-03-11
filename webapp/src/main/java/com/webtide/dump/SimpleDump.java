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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;

/**
 * Dump Servlet Request.
 */
@SuppressWarnings("serial")
@WebServlet(name = "Simple", value = {"/simple/*", "*.simple"}, asyncSupported = true)
public class SimpleDump extends HttpServlet
{
    /* ------------------------------------------------------------ */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

    /* ------------------------------------------------------------ */
    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

    /* ------------------------------------------------------------ */
    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/plain");
        PrintWriter pout = response.getWriter();

        ServletContext context = getServletContext();
        pout.printf("%50s: %s%n", "context.getServerInfo()", context.getServerInfo());
        pout.printf("%50s: %s%n", "context.getContextPath()", context.getContextPath());
        pout.printf("%50s: %s%n", "context.getRealPath(\"/\")", context.getRealPath("/"));
        pout.printf("%50s: %s%n", "context.getMajorVersion()", context.getMajorVersion());
        pout.printf("%50s: %s%n", "context.getEffectiveMajorVersion()", context.getEffectiveMajorVersion());
        pout.printf("%50s: %s%n", "context.getMinorVersion()", context.getMinorVersion());
        pout.printf("%50s: %s%n", "context.getEffectiveMinorVersion()", context.getEffectiveMinorVersion());

        pout.printf("%50s: %s%n", "request.getProtocol()", request.getProtocol());
        pout.printf("%50s: %s%n", "request.getScheme()", request.getScheme());
        pout.printf("%50s: %s%n", "request.getMethod()", request.getMethod());
        pout.printf("%50s: %s%n", "request.getRequestURI()", request.getRequestURI());
        pout.printf("%50s: %s%n", "request.getContextPath()", request.getContextPath());
        pout.printf("%50s: %s%n", "request.getServletPath()", request.getServletPath());
        pout.printf("%50s: %s%n", "request.getPathInfo()", request.getPathInfo());
        pout.printf("%50s: %s%n", "request.getQueryString()", request.getQueryString());
        pout.printf("%50s: %s%n", "request.getCharacterEncoding()", request.getCharacterEncoding());
        pout.printf("%50s: %s%n", "request.getContentLengthLong()", request.getContentLengthLong());
        pout.printf("%50s: %s%n", "request.getContentType()", request.getContentType());
        pout.printf("%50s: %s%n", "request.isSecure()", request.isSecure());

        Enumeration<String> h = request.getHeaderNames();
        while (h.hasMoreElements())
        {
            String name = (String)h.nextElement();
            Enumeration<String> h2= request.getHeaders(name);
            while (h2.hasMoreElements())
            {
                String hv= (String)h2.nextElement();
                pout.printf("%50s: %20s: %s%n", "request.getParameter(...)", notag(name), notag(hv));
            }
        }

        InputStream in = request.getInputStream();
        byte[] buffer = new byte[40];
        int len = in.read(buffer);
        while (len > 0)
        {
            pout.printf("%50s: ", "request.getInputStream().read(...)");
            for (int i = 0; i < len; i++)
                pout.printf("%02x", 0xff&buffer[i]);
            pout.println();
            len = in.read(buffer);
        }
    }

    /* ------------------------------------------------------------ */
    @Override
    public String getServletInfo()
    {
        return "Simple Dump Servlet";
    }

    private String notag(String s)
    {
        if (s==null)
            return "null";
        s=s.replaceAll("&","&amp;");
        s=s.replaceAll("<","&lt;");
        s=s.replaceAll(">","&gt;");
        return s;
    }
}
