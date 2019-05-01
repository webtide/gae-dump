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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ServiceLoader;

import com.google.cloud.logging.LoggingFactory;
import com.google.cloud.logging.LoggingOptions;

/**
 * Test Servlet Cookies.
 */
@SuppressWarnings("serial")
@WebServlet(name = "ClassLoader", value = "/classloaderdump/*")
public class ClassLoaderDump extends HttpServlet
{
    /* ------------------------------------------------------------ */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException
    {
        try
        {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<h1>ClassLoader Dump Servlet:</h1>");
            out.println("<a href=\"/\">home</a><br/>");
            out.println("<h3>Thread Context Loader:" + Thread.currentThread().getContextClassLoader() + "</h3>");

            out.println("<h2>ClassLoader Dumps</h3>");
            out.println("<h3>WebApp ClassLoader:</h3>");
            dumpLoader(out,this.getClass().getClassLoader());
            out.println("<h3>Container ClassLoader:</h3>");
            dumpLoader(out,request.getServletContext().getClass().getClassLoader());
            out.println("<h3>JVM ClassLoader:</h3>");
            dumpLoader(out,String.class.getClassLoader());


            out.println("<h2>ClassLoader Examples</h3>");

            if (request.getPathInfo() != null)
            {
                try
                {
                    Class<?> info = Thread.currentThread().getContextClassLoader().loadClass(request.getPathInfo().substring(1));
                    dump("Info", out, info);
                }
                catch (Throwable th)
                {
                    out.println("<h3>Could not load " + request.getPathInfo() + ":</h3>");
                    out.println("<pre>");
                    th.printStackTrace(out);
                    out.println("</pre>");
                }
            }

            Class<?> jre = String.class;
            dump("JRE", out, jre);

            Class<?> context = request.getServletContext().getClass();
            dump("Container", out, context);

            Class<?> webapp = this.getClass();
            dump("WebApp", out, webapp);

            Class<?> servlet = ServletException.class;
            dump("Servlet API", out, servlet);

            Class<?> gcloud = LoggingOptions.class;
            dump("App GCloud API", out, gcloud);

            Class<?> dgcloud = LoggingOptions.getDefaultInstance().getClass();
            dump("App Default GCloud API", out, dgcloud);

            Class<?> cgcloud = null;
            try
            {
                cgcloud = request.getServletContext().getClass().getClassLoader().loadClass("com.google.cloud.logging.LoggingOptions");
                dump("Container GCloud API", out, cgcloud);
            }
            catch(Exception e)
            {
                out.println("<h3>Container GCloud API</h3>\n<p>not found:" + e + "</p>\n");
            }

            Class<?> dgcloudSvc = LoggingOptions.getDefaultInstance().getService().getClass();
            dump("App Default GCloud Service", out, dgcloudSvc);

            dump("WebApp", out, LoggingFactory.class);

            out.printf("<h2>Service Loader: %s</h2>%n", LoggingFactory.class);

            out.println("<pre>");
            try
            {
                Class<?> factory = LoggingFactory.class;
                ServiceLoader loader = ServiceLoader.load(factory);
                for (Object o : loader)
                {
                    out.printf("service %s of %s loader from %s%n",o, o.getClass(), getLocationOfClass(o.getClass()));
                }
            }
            catch(Exception e)
            {
                e.printStackTrace(out);
            }
            finally
            {
                out.println("</pre>");
            }
        }
        catch(Exception e)
        {
            throw new ServletException(e);
        }
    }

    private void dump(String scope, PrintWriter out, Class<?> clazz) throws URISyntaxException
    {
        if (clazz!=null)
        {
            out.printf("<h3>%s: class %s</h3>%n", scope, clazz.getName());
            out.printf("<p>Loaded from %s</p>%n", getLocationOfClass(clazz));
            out.printf("<p>Package Implementation version %s</p>%n", clazz.getPackage().getImplementationVersion());
            out.printf("<p>Package Info %s</p>%n", clazz.getPackage());
        }
    }

    private void dumpLoader(PrintWriter out, ClassLoader loader)
    {
        out.printf("<p>Loader: %s</p>%n", loader);
        if (loader!=null)
        {
            out.println("<ul>");
            if (loader instanceof URLClassLoader)
            {
                for (URL url : ((URLClassLoader)loader).getURLs())
                    out.println("<li>" + url + "</li>");
            }
            if (loader.getParent() != null)
            {
                out.println("<li>");
                dumpLoader(out, loader.getParent());
                out.println("</li>");
            }
            out.println("</ul>");
        }
    }

    /* ------------------------------------------------------------ */
    public static URI getLocationOfClass(Class<?> clazz) throws URISyntaxException
    {
        ProtectionDomain domain = clazz.getProtectionDomain();
        if (domain != null)
        {
            CodeSource source = domain.getCodeSource();
            if (source != null)
            {
                URL location = source.getLocation();

                if (location != null)
                    return location.toURI();
            }
        }

        String resourceName = clazz.getName().replace('.', '/') + ".class";
        ClassLoader loader = clazz.getClassLoader();
        URL url = (loader == null ? ClassLoader.getSystemClassLoader() : loader).getResource(resourceName);
        if (url != null)
        {
            return getJarSource(url.toURI());
        }
        return null;
    }

    public static URI getJarSource(URI uri)
    {
        try
        {
            if (!"jar".equals(uri.getScheme()))
                return uri;
            // Get SSP (retaining encoded form)
            String s = uri.getRawSchemeSpecificPart();
            int bang_slash = s.indexOf("!/");
            if (bang_slash>=0)
                s=s.substring(0,bang_slash);
            return new URI(s);
        }
        catch(URISyntaxException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    public static String getJarSource(String uri)
    {
        if (!uri.startsWith("jar:"))
            return uri;
        int bang_slash = uri.indexOf("!/");
        return (bang_slash>=0)?uri.substring(4,bang_slash):uri.substring(4);
    }
}
