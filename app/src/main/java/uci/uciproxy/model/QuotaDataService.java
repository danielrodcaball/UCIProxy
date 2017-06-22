package uci.uciproxy.model;

import android.accounts.NetworkErrorException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.Proxy;

/**
 * Created by daniel on 21/06/17.
 */

public class QuotaDataService {
    private static String QUOTA_SOAP_ACTION = "https://cuotas.uci.cu/servicios/v1/InetCuotasWS";
    private static String QUOTA_NAMESPACE = "https://cuotas.uci.cu/";
    private static String QUOTA_METHOD_NAME = "ObtenerCuota";
    private static String QUOTA_URL = "https://cuotas.uci.cu/servicios/v1/InetCuotasWS.php?wsdl";


    public int quota;
    public float usedQuota;
    public String navigationLevel;

    public QuotaDataService(int quota, float usedQuota, String navigationLevel) {
        this.quota = quota;
        this.usedQuota = usedQuota;
        this.navigationLevel = navigationLevel;
    }

    public static QuotaDataService getQuotaData(String username, String password) throws IOException, XmlPullParserException, NetworkErrorException {
        SoapObject request = new SoapObject(QUOTA_NAMESPACE, QUOTA_METHOD_NAME);
        request.addProperty("usuario", username);
        request.addProperty("clave", password);
        request.addProperty("dominio", "uci.cu");

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        envelope.dotNet = true;

        HttpTransportSE androidHttpTransport = new HttpTransportSE(Proxy.NO_PROXY, QUOTA_URL);
        androidHttpTransport.debug = true;
        androidHttpTransport.setXmlVersionTag("<!--?xml version=\\\"1.0\\\" encoding= \\\"UTF-8\\\" ?-->");

        androidHttpTransport.call(QUOTA_SOAP_ACTION, envelope);
        if (envelope.bodyIn instanceof SoapFault) {
            throw (SoapFault)envelope.bodyIn;
        }
        // Get the SoapResult from the envelope body.
        SoapObject result = (SoapObject) envelope.bodyIn;

        int quota = Integer.valueOf(((SoapObject) result.getProperty(0)).getProperty(0).toString());
        float usedQuota = Float.valueOf(((SoapObject) result.getProperty(0)).getProperty(1).toString());
        String navigationLevel = ((SoapObject) result.getProperty(0)).getProperty(2).toString();

        return new QuotaDataService(quota, usedQuota, navigationLevel);
    }
}


