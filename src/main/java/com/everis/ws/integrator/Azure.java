package com.everis.ws.integrator;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;
import com.everis.ws.object.Run;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


@Path("/azuint")
public class Azure {
	
	@POST
	@Path("/postrun")
	@Consumes({MediaType.APPLICATION_JSON + ";charset=utf-8"})
	@Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
	public String postRun(Run run) throws Exception {
			
		synchronized(Azure.class){	
			
			String organization=run.getOrganization();
			String project=run.getProject();
			String testPlanId=run.getTestPlanId();
			String suiteId=run.getSuiteId();
			String testCaseId=run.getTestCaseId();
			//String displayName=run.getDisplayName();
			String testCaseName=run.getTestCaseName();
			String testCaseTitle=run.getTestCaseTitle();
			String runStatus=run.getRunStatus();
			
		
			AzureValues azu=new AzureValues();
			WebResource webResource;
			ClientResponse resp;
			String output;
			String outputMsg="";
			String testPointId="";
			String runId="";
			String url="";
			ArrayList<String> validator= new ArrayList<String>();
			
			
			//genero el encoding de la concatenación de la autenticación
			//String authString="Basic" + ":" + "zo53krzodu4t6wcadje5wn3x2dw273n7idhfg7dws6ki4jlol5aa";
			String authString="Basic" + ":" + azu.getPropValues().toString();				
			String authStringEnc = new Base64().encodeToString(authString.getBytes());			
			Client restClient = Client.create();			
			JsonParser jsonParser = new JsonParser();
									
			try{	
				
				//GET_TEST_POINT_ID
		        url = "https://dev.azure.com/"+organization+"/"+project+"/_apis/testplan/Plans/"+testPlanId+"/Suites/"+suiteId+"/TestPoint?testCaseId="+testCaseId;
		        
		        webResource = restClient.resource(url);	        
		        resp = webResource.accept("application/json")
		                                         .header("Authorization", "Basic " + authStringEnc)	                                         
		                                         .get(ClientResponse.class);
		        
		        if(resp.getStatus() != 200){
		            System.err.println("Unable to connect to the server");
		            validator.add("GET_TEST_POINT_ID="+resp.getStatus());
		        }
		        		        
		        
		        output = resp.getEntity(String.class);
		        JsonObject parseGetTestPointId = jsonParser.parse(output).getAsJsonObject();
		        JsonObject value=parseGetTestPointId.get("value").getAsJsonArray().get(0).getAsJsonObject();		              
		        testPointId=value.get("id").getAsString();
		        	        		 
		        //TO DO: Aca tengo que tomar el ultimo TEST POINT
		        System.out.println("response GET_TEST_POINT_ID: "+output);
		        				        
				//ADD_RUN
		        url = "https://dev.azure.com/"+organization+"/"+project+"/_apis/test/runs?api-version=5.1-preview.2";			
				String addRun="{\"name\": \""+testCaseName+"\",\"plan\": {\"id\":\""+testPlanId+"\"},\"pointIds\": [\""+testPointId+"\"],\"runStatistics\": [{\"state\": \"Completed\",\"outcome\": \""+runStatus+"\",\"count\": 1}]}";			
				JsonObject objectRunFromString = jsonParser.parse(addRun).getAsJsonObject();
							
				webResource = restClient.resource(url);	        
				resp = webResource.accept("application/json")
		                                         .header("Authorization", "Basic " + authStringEnc)
		                                         .type("application/json")
		                                         .post(ClientResponse.class,objectRunFromString.toString());
				
				if(resp.getStatus() != 200){
		            System.err.println("Existieron inconvenientes en ADD_RUN:"+resp.getStatus());
		            validator.add("ADD_RUN="+resp.getStatus());
		        }
		        
		        output = resp.getEntity(String.class);
		        JsonObject parseRunId = jsonParser.parse(output).getAsJsonObject();
		        runId=parseRunId.get("id").getAsString();
		        
		        System.out.println("response ADD_RUN: "+output);
		        	        	       
		        //ADD_RESULT
		        url = "https://dev.azure.com/"+organization+"/"+project+"/_apis/test/Runs/"+runId+"/results?api-version=5.1-preview.2";		        
				String addResult="[{\"testCaseTitle\": \""+testCaseTitle+"\",\"testCaseRevision\": 1,\"automatedTestName\": \"Prueba realizada con Postman\",\"testRun\": {\"id\": \""+runId+"\",\"name\":\""+testCaseName+"\"},\"testPoint\": {\"id\": \""+testPointId+"\"},\"testCase\":{\"id\": \""+testCaseId+"\"},\"priority\": 1,\"outcome\": \""+runStatus+"\"}]";			
				JsonArray objectResultFromString = jsonParser.parse(addResult).getAsJsonArray();
							
				webResource = restClient.resource(url);	        
				resp = webResource.accept("application/json")
		                                         .header("Authorization", "Basic " + authStringEnc)
		                                         .type("application/json")
		                                         .post(ClientResponse.class,addResult);
				
				
				if(resp.getStatus() != 200){
		            System.err.println("Existieron inconvenientes en ADD_RESULT:"+resp.getStatus());
		            validator.add("ADD_RESULT="+resp.getStatus());
		        }
		        
		        output = resp.getEntity(String.class);	    	        
		        System.out.println("response ADD_RESULT: "+output);
		        
		        //Modify Test Point
		        //PATCH_TEST_POINT_ID
		        url = "https://dev.azure.com/"+organization+"/"+project+"/_apis/testplan/Plans/"+testPlanId+"/Suites/"+suiteId+"/TestPoint?api-version=5.1-preview.2";
		        String modifyTestPoint="[{\"id\":"+testPointId+",\"results\":{\"outcome\":\""+runStatus+"\"}}]";		        
		        webResource = restClient.resource(url);	        
		        resp = webResource.accept("application/json")		       
		                            .header("Authorization", "Basic " + authStringEnc)		
		                            .header("X-HTTP-Method-Override","PATCH")
		                            .type("application/json")
		                            .post(ClientResponse.class,modifyTestPoint);		                            		 
		        
		        output = resp.getEntity(String.class);	    	        
		        System.out.println("response PATCH_TEST_POINT: "+output);	        
		        if(resp.getStatus() != 200){
		            System.err.println("Unable to connect to the server");
		            validator.add("PATCH_TEST_POINT_ID="+resp.getStatus());
		        }
			
			}catch(Exception ex){
				outputMsg="{\"msj\":\"Existieron inconvenientes en el alta de la corrida: "+ex.getMessage()+"\"}";
				validator.add(outputMsg);
				System.out.println("Excepcion: "+ex.getMessage());
			}	
			
			if(validator.size()!=0){				
				outputMsg="{\"msj\":\"Existieron inconvenientes en el alta de la corrida: "+validator.toString()+"\"}";
			}
			else
			{
				outputMsg="{\"msj\":\"La corrida fue incorporada correctamente con ID:"+runId+"\"}";
			}
			
	        return outputMsg;
		}
	}
	
}
