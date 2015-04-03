package ee.ut.cs.domatching;

import android.util.Log;

import java.net.SocketException;

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
    public CoapServer() throws SocketException {

        // add resources to the server
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
            String text = request.getPayloadString();
            System.out.println("request  " + text);
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
        Log.e(TAG,"Incoming request:");
        request.log();

        // handle the request
        super.handleRequest(request);
    }

}
