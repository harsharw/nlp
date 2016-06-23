package com.wishitventures.search.server;

import com.wishitventures.nlp.domain.EnglishWord;
import com.wishitventures.nlp.domain.Word;
import com.wishitventures.nlp.domain.WordSenseDictionaryLoader;
import com.wishitventures.nlp.domain.WordsCollectionFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashSet;

public class DictionaryServer {

  private static final Logger logger = Logger.getLogger(DictionaryServer.class);
  // http://localhost:61000/api?query=test
  public static void main(String[] args) throws Exception {
    BasicConfigurator.configure();
    Server server = new Server(61000);

    URL warUrl = DictionaryServer.class.getClassLoader().getResource("resources");

    String warUrlString = warUrl.toExternalForm();
    WebAppContext wac = new WebAppContext();
    wac.setResourceBase(warUrlString);
    wac.setContextPath("/dictionary");

    ServletContextHandler sch = new ServletContextHandler(ServletContextHandler.SESSIONS);
    sch.setResourceBase(warUrlString);
    sch.setContextPath("/");
    sch.addServlet(new ServletHolder(DictionaryServlet.class), "/api");

    FilterHolder filterHolder = new FilterHolder(CrossOriginFilter.class);
    filterHolder.setInitParameter("allowedOrigins", "*");
    filterHolder.setInitParameter("allowedMethods", "GET, POST");
    //sch.addFilter(filterHolder, "/*", null);

    HandlerList handlerList = new HandlerList();
    handlerList.setHandlers(new Handler[]{wac, sch});

    server.setHandler(handlerList);

    server.start();
    server.join();
  }

  public static final class DictionaryServlet extends HttpServlet {
    private final WordSenseDictionaryLoader loader =
        new WordSenseDictionaryLoader(new File("/Users/ECA/Desktop/AWS_Search/Dictionaries/WordSense_3.0/"));
    private WordsCollectionFactory.TextToWordsMap words = null;

    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException {

      response.setContentType("text/html");
      PrintWriter writer = response.getWriter();

      StringBuilder msg = new StringBuilder();
      if(words == null) {
        words = (WordsCollectionFactory.TextToWordsMap) loader.loadWords();
        msg.append("Loaded dictionary");
        Runtime runtime = Runtime.getRuntime();
        msg.append("Free memory: " + runtime.freeMemory() + ", total: " + runtime.totalMemory());
      }

      String wordText = request.getParameter("query");
      EnglishWord w = (EnglishWord)words.get(wordText);
      if(w != null) {
        HashSet<Word> synonyms = w.synonymsTarget(3);

        if(!synonyms.isEmpty()) {
          msg.append("Synonyms for: " + w + " --> " + synonyms.toString());
        } else {
          msg.append("No synonyms for '" + w.toString()
              + "' not found in Dictionary. Variants: " + w.variants());
        }
      } else  {
        msg.append(wordText + " not found in Dictionary");
      }

      writer.print(msg.toString());
    }
  }
}
