//
//  ========================================================================
//  Copyright (c) 1995-2019 Mort Bay Consulting Pty. Ltd.
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

package com.acme.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RunAs;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * AnnotationTest
 *
 */

@RunAs("special")
@WebServlet(urlPatterns = {"/","/test/*"}, name="AnnotationTest", initParams={@WebInitParam(name="fromAnnotation", value="xyz")})
@DeclareRoles({"user","client"})
public class AnnotationTest extends HttpServlet 
{
    static List<String> __HandlesTypes = Arrays.asList( "javax.servlet.GenericServlet",
        "javax.servlet.http.HttpServlet",
        "com.acme.test.AsyncListenerServlet",
        "com.acme.test.ClassLoaderServlet",
        "com.acme.test.AnnotationTest",
        "com.acme.test.RoleAnnotationTest",
        "com.acme.test.MultiPartTest",
        "com.acme.fragment.FragmentServlet",
        "com.acme.test.TestListener",
        "com.acme.test.SecuredServlet",
        "com.acme.test.Bar");

    private String postConstructResult = "<span class=\"fail\">FAIL</span>";

    private ServletConfig config;

    @PostConstruct
    private void myPostConstructMethod ()
    {       
        postConstructResult = "<span class=\"pass\">PASS</span>";
    }
    
    @PreDestroy
    private void myPreDestroyMethod()
    {
    }
    
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        this.config = config;
    }

    
    
    /* ------------------------------------------------------------ */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

    /* ------------------------------------------------------------ */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {      
        try
        {
            response.setContentType("text/html");
            ServletOutputStream out = response.getOutputStream();
            out.println("<html>");
            out.println("<HEAD><link rel=\"stylesheet\" type=\"text/css\"  href=\"stylesheet.css\"/></HEAD>");
            out.println("<body>");
            out.println("<h1>Results</h1>");
            out.println("<h2>Container Information</h2>");
            out.println("<pre>");
            ServletContext context = getServletContext();
            out.println("ServerInfo: "+ context.getServerInfo());
            out.println("MajorVersion.MinorVersion: "+ context.getMajorVersion()+"."+ context.getMinorVersion());
            out.println("SessionTrackingModes: "+ context.getDefaultSessionTrackingModes());
            out.println("Effective MajorVersion.MinorVersion: "+ context.getEffectiveMajorVersion()+"."+ context.getEffectiveMinorVersion());
            out.println("Effective SessionTrackingModes: "+ context.getDefaultSessionTrackingModes());
            out.println("</pre>");

            out.println("<h2>Init Params from Annotation</h2>");
            out.println("<pre>");
            out.println("initParams={@WebInitParam(name=\"fromAnnotation\", value=\"xyz\")}");
            out.println("</pre>");
            out.println("<p><b>Result: "+("xyz".equals(config.getInitParameter("fromAnnotation"))? "<span class=\"pass\">PASS": "<span class=\"fail\">FAIL")+"</span></p>");

            out.println("<h2>Init Params from web-fragment</h2>");
            out.println("<pre>");
            out.println("extra1=123, extra2=345");
            out.println("</pre>");
            boolean fragInitParamResult = "123".equals(config.getInitParameter("extra1")) && "345".equals(config.getInitParameter("extra2"));
            out.println("<p><b>Result: "+(fragInitParamResult? "<span class=\"pass\">PASS": "<span class=\"fail\">FAIL")+"</span></p>");

             out.println("<h2>@ContainerInitializer</h2>");
             out.println("<pre>");
             out.println("@HandlesTypes({javax.servlet.Servlet.class, Foo.class})");
             out.println("</pre>");
             out.print("<p><b>Result: ");
             List<Class> classes = (List<Class>)config.getServletContext().getAttribute("com.acme.Foo");
             List<String> classNames = new ArrayList<String>();
             if (classes != null)
             {
                 for (Class c : classes)
                     classNames.add(c.getName());

                 for (String expected : __HandlesTypes)
                 {
                     if (classNames.contains(expected))
                         out.println("<br/>&nbsp;" + expected + ":&nbsp;<span class=\"pass\">PASS</span>");
                     else
                        out.println("<br/>&nbsp;" + expected + ":&nbsp;<span class=\"fail\">FAIL</span>");
                 }
                 for (String actual : classNames)
                 {
                     if (__HandlesTypes.contains(actual))
                         continue;

                     // Ignore dump sevlets
                     if (actual.startsWith("com.webtide.dump."))
                         continue;

                     // Ignore precompile JSP servlets
                     if (actual.startsWith("org.apache.jsp.") && actual.endsWith("_jsp"))
                         continue;

                     out.println("<br/>&nbsp;" + actual + ":&nbsp;<span class=\"fail\">UNEXPECTED!</span>");
                 }
             }
             else
                 out.print("<br/><span class=\"fail\">FAIL</span> (No such attribute com.acme.Foo)");
             out.println("</b></p>");

            out.println("<h2>Complete Servlet Registration</h2>");
            Boolean complete = (Boolean)config.getServletContext().getAttribute("com.acme.AnnotationTest.complete");
            out.println("<p><b>Result: "+(complete.booleanValue()?"<span class=\"pass\">PASS":"<span class=\"fail\">FAIL")+"</span></b></p>");
            
            out.println("<h2>ServletContextListener Programmatic Registration from ServletContainerInitializer</h2>");
            Boolean programmaticListener = (Boolean)config.getServletContext().getAttribute("com.acme.AnnotationTest.listenerTest");
            out.println("<p><b>Result: "+(programmaticListener.booleanValue()?"<span class=\"pass\">PASS":"<span class=\"fail\">FAIL")+"</span></b></p>");
            
            out.println("<h2>ServletContextListener Programmatic Registration Prevented from ServletContextListener</h2>");
            Boolean programmaticListenerPrevention = (Boolean)config.getServletContext().getAttribute("com.acme.AnnotationTest.listenerRegoTest");
            out.println("<p><b>Result: "+(programmaticListenerPrevention.booleanValue()?"<span class=\"pass\">PASS":"<span class=\"fail\">FAIL")+"</span></b></p>");
            
            out.println("<h2>ServletContextListener Registration Prevented from ServletContextListener</h2>");
            Boolean webListenerPrevention = (Boolean)config.getServletContext().getAttribute("com.acme.AnnotationTest.sclFromSclRegoTest");
            out.println("<p><b>Result: "+(webListenerPrevention.booleanValue()?"<span class=\"pass\">PASS":"<span class=\"fail\">FAIL")+"</span></b></p>");
            
            out.println("<h2>Invalid Type for Listener Detection</h2>");
            Boolean badListener = (Boolean)config.getServletContext().getAttribute("com.acme.AnnotationTest.invalidListenerRegoTest");
            out.println("<p><b>Result: "+(badListener.booleanValue()?"<span class=\"pass\">PASS":"<span class=\"fail\">FAIL")+"</span></b></p>");

            out.println("<h2>@PostConstruct Callback</h2>");
            out.println("<p><b>Result: "+postConstructResult+"</b></p>");

            out.println("</body>");            
            out.println("</html>");
            out.flush();
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }
    

  
   
}
