package ee.ut.cs.domatching;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coap.*;
import endpoint.Endpoint;
import endpoint.LocalEndpoint;





/**
 * Created by weiding on 28/03/15.
 */
public class CoapServer extends LocalEndpoint{
    /*
	 * Constructor for a new SampleServer
	 *
	 */
    private final String TAG = "CoapServer";
    CoapDebugInfo coapDebugInfo;

    public CoapServer(CoapDebugInfo coapDebugInfo) throws SocketException {

        // add resources to the server
        this.coapDebugInfo = coapDebugInfo;
        addResource(new CoapResource());
//        addResource(new StorageResource());
//        addResource(new ToUpperResource());
//        addResource(new SeparateResource());
    }

    // Resource definitions ////////////////////////////////////////////////////

    /*
     * Defines a resource that returns "Hello World!" on a GET request.
     *
     */
    private class CoapResource extends LocalResource {

        public CoapResource() {
            super("temperatureMatching");
            setResourceName("post rt and the matching temperature ontology");
        }

        @Override
        public void performPOST(POSTRequest request) {

            // retrieve text to convert from payload
            String payload = request.getPayloadString();

            String ontologyUri = GetOntologyUrl(payload);

            boolean matchResult = PerformMatchingTemperature(ontologyUri);
            request.respond(CodeRegistry.V3_RESP_OK, matchResult ? "true" : "false");
            coapDebugInfo.printDebugInfo("Matching Result " + (matchResult ? "true" : "false"));


        }
    }

    private boolean PerformMatchingTemperature(String ontologyUri) {
        String relationshipUri = "http://purl.org/vocab/relationship/";
        Model model = ModelFactory.createDefaultModel();
        Property subclassof = model.createProperty(relationshipUri,"subclassof");
        Property onPropery = model.createProperty(relationshipUri,"onProperty");

        InputStream in = FileManager.get().open(ontologyUri);
        if (in == null) {
            throw new IllegalArgumentException( "File:  not found");
        }
        model.read(in,"");

        ResIterator temperaturesRes = model.listSubjectsWithProperty(subclassof);

        boolean matchResult;
        // Because subjects of statements are Resources, the method returned a ResIterator
        while (temperaturesRes.hasNext()) {

            // ResIterator has a typed nextResource() method
            com.hp.hpl.jena.rdf.model.Resource temperatureRes = temperaturesRes.nextResource();
            // Print the URI of the resource
            Statement matchStatement = temperatureRes.getProperty(subclassof);
            com.hp.hpl.jena.rdf.model.Resource matchResource = matchStatement.getResource();
            if(matchResource.getURI().contains("temperature")){
                Log.e(TAG,temperatureRes.getURI());
                matchResult = MatchTemperatureProperty(matchResource,onPropery);
                if(matchResult){
                    return true;
                }
            }
        }

        return false;

    };

    private static boolean MatchTemperatureProperty(com.hp.hpl.jena.rdf.model.Resource resource, Property property){
        HashMap<String,Boolean> temperatureOntologyMap =InitTemperatureOntologyMap();

        StmtIterator iterator = resource.listProperties();
        while(iterator.hasNext()){
            Statement statement = iterator.nextStatement();
            String key = statement.getObject().toString();
            if(temperatureOntologyMap.containsKey(key)){
                temperatureOntologyMap.put(key, true);
            }
        }

        for(Map.Entry<String, Boolean> entry: temperatureOntologyMap.entrySet()){
            if(!entry.getValue()){
                return false;
            }
        }

        return true;
    }

    private static HashMap<String,Boolean>  InitTemperatureOntologyMap(){
        HashMap<String,Boolean>  temperatureOntologyMap = new HashMap<String,Boolean>();
        temperatureOntologyMap.put("http://ontology/scale" , false);
        temperatureOntologyMap.put("http://ontology/degree" , false);
        return temperatureOntologyMap;

    }


    private String GetOntologyUrl(String payload){
        List<String> list = new ArrayList<String>(Arrays.asList(payload.split(";")));
        for(String string : list){
            if(string.startsWith("rt=")){
                return string.substring("rt=".length()).replace("\"", "");
            }
        }

        return null;
    }

    private String FetchOntology(String ontologyUri) throws IOException {
        if(!ontologyUri.startsWith("http")){
            ontologyUri = "http://"+ ontologyUri;
        }

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(new HttpGet(ontologyUri));
        StatusLine statusLine = response.getStatusLine();
        if(statusLine.getStatusCode() == HttpStatus.SC_OK){
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            String responseString = out.toString();
            out.close();
            return responseString;
        } else{
            //Closes the connection.
            response.getEntity().getContent().close();
            throw new IOException(statusLine.getReasonPhrase());
        }

    }

    private class ToUpperResource extends LocalResource {

        public ToUpperResource() {
            super("toUpper");
            setResourceName("POST text here to convert it to uppercase");
        }

        @Override
        public void performPOST(POSTRequest request) {

            // retrieve text to convert from payload
            String text = request.getPayloadString();

            // complete the request
            request.respond(CodeRegistry.V3_RESP_OK, text.toUpperCase());
        }
    }

    /*
     * Defines a resource that stores POSTed data and that creates new
     * sub-resources on PUT request where the Uri-Path doesn't yet point
     * to an existing resource.
     *
     */
    private class StorageResource extends LocalResource {

        public StorageResource(String resourceIdentifier) {
            super(resourceIdentifier);
            setResourceName("POST your data here or PUT new resources!");
        }

        public StorageResource() {
            this("storage");
            isRoot = true;
        }

        @Override
        public void performGET(GETRequest request) {

            // create response
            Response response = new Response(CodeRegistry.RESP_CONTENT);

            // set payload
            response.setPayload(data);

            // set content type
            response.setOption(contentType);

            // complete the request
            request.respond(response);
        }

        @Override
        public void performPOST(POSTRequest request) {

            // store payload
            storeData(request);

            // complete the request
            request.respond(CodeRegistry.RESP_CHANGED);
        }

        @Override
        public void performPUT(PUTRequest request) {

            // store payload
            storeData(request);

            // complete the request
            request.respond(CodeRegistry.RESP_CHANGED);
        }

        @Override
        public void performDELETE(DELETERequest request) {

            // disallow to remove the root "storage" resource
            if (!isRoot) {

                // remove this resource
                remove();

                request.respond(CodeRegistry.RESP_DELETED);
            } else {
                request.respond(CodeRegistry.RESP_FORBIDDEN);
            }
        }

        @Override
        public void createNew(PUTRequest request, String newIdentifier) {

            // create new sub-resource
            StorageResource resource = new StorageResource(newIdentifier);
            addSubResource(resource);

            // store payload
            resource.storeData(request);

            // complete the request
            request.respond(CodeRegistry.RESP_CREATED);
        }

        private void storeData(Request request) {

            // set payload and content type
            data = request.getPayload();
            contentType = request.getFirstOption(OptionNumberRegistry.CONTENT_TYPE);

            // signal that resource state changed
            changed();
        }

        private byte[] data;
        private Option contentType;
        private boolean isRoot;
    }

    /*
     * Defines a resource that returns "Hello World!" on a GET request.
     *
     */
    private class SeparateResource extends ReadOnlyResource {

        public SeparateResource() {
            super("separate");
            setResourceName("GET a response in a separate CoAP Message");
        }

        @Override
        public void performGET(GETRequest request) {

            // we know this stuff may take longer...
            // promise the client that this request will be acted upon
            // by sending an Acknowledgement
            request.accept();

            // do the time-consuming computation
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            // create response
            Response response = new Response(CodeRegistry.RESP_CONTENT);

            // set payload
            response.setPayload("This message was sent by a separate response.\n" +
                    "Your client will need to acknowledge it, otherwise it will be retransmitted.");

            // complete the request
            request.respond(response);
        }
    }

    // Logging /////////////////////////////////////////////////////////////////

    @Override
    public void handleRequest(Request request) {

        // output the request
        coapDebugInfo.printDebugInfo("Incoming request " + request.getURI());
        request.log();
        // handle the request
        super.handleRequest(request);
    }

}
