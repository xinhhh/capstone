package uk.ac.cam.cares.jps.chatbotwrappers;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class OntologyLookup
 */
@WebServlet("/OntologyLookup")
public class OntologyLookup extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OntologyLookup() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String term = request.getParameter("term"); // to get parameter question from the HTTP request 
		
		// request the interface provided by flask (The Python scripts already provides HTTP access, the Java wrapper is )
		String http_url = "http://localhost:5000/chemistry_chatbot/OntologyLookup";
		
		// put the keys and inputs 
		ArrayList<String> inputs = new ArrayList<String>();
		inputs.add(term);
		
		ArrayList<String> keys = new ArrayList<String>();
		inputs.add("term");
		String result = MakeRequest.send(inputs, keys, http_url); // make the http request		
		response.getWriter().write(result);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
