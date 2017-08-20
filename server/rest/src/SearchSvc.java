package rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

//import org.apache.commons.lang.*;

import java.sql.*;  
import java.util.*;

import dto.Search;
import dto.Response;

@Path("/search")
public class SearchSvc {
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response<List<Search>> getSearches() { 
		String sql = "SELECT search_term, COUNT(*) as count FROM search_terms GROUP BY search_term ORDER BY count DESC";
		List<Search> searches = new ArrayList<Search>();
		Response<List<Search>> response = new Response<List<Search>>();
		response.setReturnData(searches);
		Search search = new Search();
		search.setTerm("Hello World");
		search.setCount(1); 
		searches.add(search); 
		return response;
	}
	
	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response<String> postSearch(Search search) {
		System.out.println("Output json server .... \n");
		System.out.println(search);
		return new Response<String>("Succesfully inserted term: " + search.getTerm());
	} 
} 

