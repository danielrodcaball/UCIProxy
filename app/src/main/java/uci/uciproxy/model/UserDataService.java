package uci.uciproxy.model;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;

/**
 * Created by daniel on 21/06/17.
 */

public class UserDataService {
    private static String AUTHENTICATION_SOAP_ACTION = "https://autenticacion2.uci.cu/v6/AutenticarUsuario";
    private static String AUTHENTICATION_NAMESPACE = "https://autenticacion2.uci.cu/v6/";
    private static String AUTHENTICATION_METHOD_NAME = "AutenticarUsuario";
    private static String AUTHENTICATION_URL = "https://autenticacion2.uci.cu/v6/PasarelaAutenticacionWS.php";

    public String name;
    public String logoUrl;
    public boolean isAuthenticate;

    public UserDataService(String name, String logoUrl, boolean isAuthenticate) {
        this.name = name;
        this.logoUrl = logoUrl;
        this.isAuthenticate = isAuthenticate;
    }

    public static UserDataService getUserData(String username, String password) throws IOException, XmlPullParserException {
        SoapObject request = new SoapObject(AUTHENTICATION_NAMESPACE, AUTHENTICATION_METHOD_NAME);
        request.addProperty("Usuario", username);
        request.addProperty("Clave", password);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        envelope.dotNet = true;

        HttpTransportSE androidHttpTransport = new HttpTransportSE(Proxy.NO_PROXY, AUTHENTICATION_URL);
        androidHttpTransport.debug = true;
        androidHttpTransport.setXmlVersionTag("<!--?xml version=\\\"1.0\\\" encoding= \\\"UTF-8\\\" ?-->");

        ArrayList<HeaderProperty> headerPropertyArrayList = new ArrayList<HeaderProperty>();
        headerPropertyArrayList.add(new HeaderProperty("Connection", "close"));

        androidHttpTransport.call(AUTHENTICATION_SOAP_ACTION, envelope, headerPropertyArrayList);

        if (envelope.bodyIn instanceof SoapFault) {
            throw (SoapFault)envelope.bodyIn;
        }

        // Get the SoapResult from the envelope body.
        SoapObject result = (SoapObject) envelope.bodyIn;

        String name = ((SoapObject) result.getProperty(0)).getProperty(1).toString();
        SoapObject logo = (SoapObject) ((SoapObject) result.getProperty(0)).getProperty(13);
        String url = logo.getProperty(1).toString();
        String [] arr = url.split(":");
        String logoUrl = "https:" + arr[1];
        boolean isAuthenticated = ((SoapObject) result.getProperty(0)).getProperty(15).toString().equals("false") ? false : true;
        return new UserDataService(name, logoUrl, isAuthenticated);
    }
}
