package uci.uciproxy.user;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;

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
import java.util.HashMap;

/**
 * Created by daniel on 18/02/17.
 */

public class User {
    public static String QUOTA_SOAP_ACTION = "https://cuotas.uci.cu/servicios/v1/InetCuotasWS";
    public static String QUOTA_NAMESPACE = "https://cuotas.uci.cu/";
    public static String QUOTA_METHOD_NAME = "ObtenerCuota";
    public static String QUOTA_URL = "https://cuotas.uci.cu/servicios/v1/InetCuotasWS.php?wsdl";

    public static String AUTHENTICATION_SOAP_ACTION = "https://autenticacion2.uci.cu/v6/AutenticarUsuario";
    public static String AUTHENTICATION_NAMESPACE = "https://autenticacion2.uci.cu/v6/";
    public static String AUTHENTICATION_METHOD_NAME = "AutenticarUsuario";
    public static String AUTHENTICATION_URL = "https://autenticacion2.uci.cu/v6/PasarelaAutenticacionWS.php";

    public String username;
    public String password;

    public String name;
    public String logoUrl;
    public boolean authenticated = false;
    public Bitmap logo;


    public double quota = -1;
    public double usedQuota = -1;
    public String navigationLevel = "none";

    public boolean networkError = false;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void updateUserData() throws IOException, XmlPullParserException, NetworkErrorException {
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
            throw new NetworkErrorException("network exception");
        }

        // Get the SoapResult from the envelope body.
        SoapObject result = (SoapObject) envelope.bodyIn;
        name = ((SoapObject) result.getProperty(0)).getProperty(1).toString();
        SoapObject logo = (SoapObject) ((SoapObject) result.getProperty(0)).getProperty(13);
        String url = logo.getProperty(1).toString();
        String [] arr = url.split(":");
        logoUrl = "https:" + arr[1];

        authenticated = ((SoapObject) result.getProperty(0)).getProperty(15).toString().equals("false") ? false : true;
    }

    public void updateQuotaData() throws IOException, XmlPullParserException, NetworkErrorException {
        if (!authenticated) return;

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
            throw new NetworkErrorException();
        }
        // Get the SoapResult from the envelope body.
        SoapObject result = (SoapObject) envelope.bodyIn;
        HashMap<String, String> currentState = new HashMap<String, String>();

        quota = Double.valueOf(((SoapObject) result.getProperty(0)).getProperty(0).toString());
        usedQuota = Double.valueOf(((SoapObject) result.getProperty(0)).getProperty(1).toString());
        navigationLevel = ((SoapObject) result.getProperty(0)).getProperty(2).toString();
    }

    public void update() throws IOException, XmlPullParserException, NetworkErrorException {
        updateUserData();
        if (authenticated) {
            updateQuotaData();
        }
    }

    public void downloadLogo(Context context) {
        ImageSize targetSize = new ImageSize(40, 40);
//        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
//                .imageDownloader(new BaseImageDownloader(this, 5 * 1000, 20 * 1000))
//                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
        imageLoader.handleSlowNetwork(true);
        Log.e("url logo", logoUrl);
        logo = imageLoader.loadImageSync(logoUrl, targetSize);
    }


}
