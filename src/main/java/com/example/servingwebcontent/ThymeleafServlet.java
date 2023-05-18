package com.example.servingwebcontent;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

@WebServlet(name = "thymeleaf", urlPatterns = "*.html")
public class ThymeleafServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ServletContextTemplateResolver resolver;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        resolver = new ServletContextTemplateResolver(this.getServletContext());

        System.out.println("+--------------------------+");
        System.out.println(this.getServletContext());
        System.out.println(this.getServletContext().getRealPath("index.html"));
        System.out.println("+--------------------------+");

        //resolver.setTemplateMode(TemplateMode.HTML5);
        resolver.setPrefix("/templates/");
        resolver.setCacheable(true);
        resolver.setCacheTTLMs(60000L);
        resolver.setCharacterEncoding("utf-8");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doService(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doService(request, response);
    }

    protected void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding(resolver.getCharacterEncoding());

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);

        WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
        ctx.setVariable("name", "friendly student");

        ctx.setVariable("thename", "Jonas");
        ctx.setVariable("completeurl", "http://localhost:8080/thymeleafServlet/hellofromservlet.html");

        String templateName = getTemplateName(request);
        String result = engine.process(templateName, ctx);

        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.println(result);
        } finally {
            out.close();
        }
    }

    protected String getTemplateName(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath == null) {
            contextPath = "";
        }

        return requestPath.substring(contextPath.length());
    }
}
