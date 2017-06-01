/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cst438;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.annotation.WebServlet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lisa
 */
@WebServlet(name = "NewServlet", urlPatterns = {"/NewServlet", "*.gif", "*.ico"})
public class NewServlet extends HttpServlet {

    protected static EntityManagerFactory emf = Persistence.createEntityManagerFactory("week5PU");
    EntityManager em = emf.createEntityManager();
    static final String RESOURCE_DIR = "src/main/resources/";
    static Random generator = new Random();
    static boolean goodEntry = false;
    private Game game = new Game();
    private String cookie = "0";     // used to keep track of current game 

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String gameResponse = "";
        String uri = request.getRequestURI();
        response.setContentType("text/html;charset=UTF-8");
        System.out.println("URI=" + uri);
        if (uri.endsWith(".gif") || uri.endsWith(".ico")) {
            // http get request for an image file
            sendFile(response, uri.substring(1));
        } else {
            // come here to play the game

            // get cookie value from http request
            String requestCookie = request.getHeader("Cookie");
            System.out.println("Cookie=" + requestCookie);
            System.out.println("Cookie=" + cookie);
            // if there is no cookie, or it is "0" or differfent from the current value, then start a new game
            if (requestCookie == null || requestCookie.equals("0") || cookie.equals("0") || !requestCookie.equals(cookie)) {
                game.startNewGame();
                cookie = generateCookie();
                gameResponse = "<!DOCTYPE html><html><head><title>MyHttpServer</title></head><body><h2>Hangman</h2>"
                        + "<img src=\"" + "h" + game.getState() + ".gif" + "\">"
                        + "<h2 style=\"font-family:'Lucida Console', monospace\"> " + game.getDisplayWord() + "</h2>"
                        + "<form action=\"\" method=\"get\"> "
                        + "Guess a character <input type=\"text\" name=\"guess\"><br>"
                        + "<input type=\"submit\" value=\"Submit\">" + "</form></body></html>";
            } else {
                // continue with current game

                String[] uriParts = uri.split("\\?");
                String guess = request.getParameter("guess");
                // if it has url params

                goodEntry = guess.matches("^[\\w]$");

                if (goodEntry) {
                    char ch = guess.toLowerCase().charAt(guess.length() - 1);  // letter that user has guessed

                    int result = game.playGame(ch);
                    switch (result) {
                        case 0: // good guess, continue game
                            gameResponse = "<!DOCTYPE html><html><head><title>MyHttpServer</title></head><body><h2>Hangman</h2>"
                                    + "<img src=\"" + "h" + game.getState() + ".gif" + "\">"
                                    + "<h2 style=\"font-family:'Lucida Console', monospace\"> " + game.getDisplayWord() + "</h2>"
                                    + "<form action=\"\" method=\"get\"> "
                                    + "Guess a character <input type=\"text\" name=\"guess\"><br>"
                                    + "<input type=\"submit\" value=\"Submit\">" + "</form></body></html>";
                            break;
                        case 1: // good guess, win game
                            gameResponse = "<!DOCTYPE html><html><head><title>MyHttpServer</title></head><body><h2>Hangman</h2>"
                                    + "<img src=\"" + "h" + game.getState() + ".gif" + "\">"
                                    + "<h2 style=\"font-family:'Lucida Console', monospace\"> " + "</h2>"
                                    + "<h2>Congratulations you win!</h2>" + "</body></html>";
                            cookie = "0";
                            break;
                        case 2: // bad guess, continue game
                            gameResponse = "<!DOCTYPE html><html><head><title>MyHttpServer</title></head><body><h2>Hangman</h2>"
                                    + "<img src=\"" + "h" + game.getState() + ".gif" + "\">"
                                    + "<h2 style=\"font-family:'Lucida Console', monospace\"> " + game.getDisplayWord() + "</h2>"
                                    + "<form action=\"\" method=\"get\"> "
                                    + "Guess a character <input type=\"text\" name=\"guess\"><br>"
                                    + "<input type=\"submit\" value=\"Submit\">" + "</form></body></html>";
                            break;
                        case 3: // bad guess, lost game
                            gameResponse = "<!DOCTYPE html><html><head><title>MyHttpServer</title></head><body><h2>Hangman</h2>"
                                    + "<img src=\"" + "h7.gif" + "\">" + "<h2>You lost!  The word is " + game.getWord() + "</h2>"
                                    + "</body></html>";
                            cookie = "0";
                            break;
                    }

                } else {
                    gameResponse = "<!DOCTYPE html><html><head><title>MyHttpServer</title></head><body><h2>Hangman</h2>"
                            + "<img src=\"" + "h" + game.getState() + ".gif" + "\">"
                            + "<h2 style=\"font-family:'Lucida Console', monospace\"> " + game.getDisplayWord() + "</h2>"
                            + "<form action=\"\" method=\"get\"> "
                            + "Guess a character <input type=\"text\" name=\"guess\"><br>"
                            + "<p style='color:red'>Error bad submission</p>"
                            + "<input type=\"submit\" value=\"Submit\">" + "</form></body></html>";
                }
            }
            response.setHeader("Content-Type", "text/html");
            if (cookie != null) {
                response.setHeader("Set-Cookie", cookie);
            } else {
                response.setHeader("Set-Cookie", "0");
            }
            response.setHeader("displayword", game.getDisplayWord()); // set the displayWord in the HTTP Headers
            System.out.println("New cookie:" + cookie);
            /* t.sendResponseHeaders(200, response.length());
                System.out.println("response=" + response);
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close(); */

        }

        try (PrintWriter out = response.getWriter()) {
            out.println(gameResponse);
            /* TODO output your page here. You may use following sample code. 
            
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet NewServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet NewServlet at " + request.getContextPath() + "</h1>");
             */
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /* 
        EntityManager em = emf.createEntityManager();           
        Query q = em.createNamedQuery("Word.findAll");
        List<Word> words = q.getResultList();
        Random rand = new Random();
        int r = rand.nextInt(words.size());
        Word w;
        try {
            w = words.get(r);
        } catch (Exception e) {
            w = null;
        }
        
        request.getAttribute(cookie);
        request.getRequestDispatcher("newjsp.jsp").forward(request, response);
        em.close();
         */
        processRequest(request, response);

    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private String generateCookie() {
        return Long.toString(generator.nextLong());
    }

    /*
                 * send a gif file
     */
    private static void sendFile(HttpServletResponse response, String filename) {

        try {
            File file = new File(RESOURCE_DIR + filename);
            byte[] fileData = new byte[(int) file.length()];
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(fileData);
            dis.close();

            response.setHeader("Content-Type", "image/gif");
            response.setContentLength(fileData.length);
            try (PrintWriter out = response.getWriter()) {
                out.println(fileData);
            } catch (Exception e) {
                System.out.println("Error in sendFile:" + filename + " " + e.getMessage());
            }

        } catch (IOException ex) {
            Logger.getLogger(NewServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
