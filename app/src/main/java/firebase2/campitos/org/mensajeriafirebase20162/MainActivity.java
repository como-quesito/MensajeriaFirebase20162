package firebase2.campitos.org.mensajeriafirebase20162;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class MainActivity extends AppCompatActivity {
    String token;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                String value = getIntent().getExtras().getString(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
        // [END handle_data_extras]

        Button subscribeButton = (Button) findViewById(R.id.subscribeButton);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // [START subscribe_topics]
                FirebaseMessaging.getInstance().subscribeToTopic("news");
                // [END subscribe_topics]

                // Log and toast
                String msg = getString(R.string.msg_subscribed);
                Log.d(TAG, msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        Button logTokenButton = (Button) findViewById(R.id.logTokenButton);
        logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get token
                 token = FirebaseInstanceId.getInstance().getToken();


                // Log and toast
                String msg = getString(R.string.msg_token_fmt, token);
                Log.d(TAG, msg);
                registrarEnBackground();
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /***********************************************************************
     EL METODO registrarEnBackground, es el corazón del registro
     **********************************************************************/

    private  void registrarEnBackground(){

        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... strings) {
                String msg="";
                try{

                    msg="Dispositivo registrado con id="+token;
                    /******************************************************************************************
                     Una vez obtenido el registro debemos transferirlo a través de http para que se guarde en el servidor
                     **************************************************************************************************/
                    String servidorulr="http://192.168.1.75:9000/registro-mensajes";

                    try{
                        hacerPost(servidorulr,token);
                    }catch(Exception e){
                        System.out.println("algo malo ocurrio...."+e.getMessage());
                    }
                    // Persist the regID - no need to register again.
                  //  storeRegistrationId(ctx, registroId);

                }catch(Exception e){
                    System.out.println("algo malote ocurrio:"+e.getMessage());
                }


                return msg;
            }
        }.execute(null,null,null);
    }

    public String hacerPost(String  url,String registroId){

        RegistroMensajeria registroMensajeria=new RegistroMensajeria();
        registroMensajeria.setRegistroId(registroId);
        //Headers
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.setContentType(new MediaType("application","json"));
        //creamos la entidad a enviar
        HttpEntity<RegistroMensajeria> mensajeriaHttpEntity=new HttpEntity<RegistroMensajeria>(registroMensajeria,httpHeaders);
        //Creamoe una instancia de restemplate
        RestTemplate restTemplate=new RestTemplate();
        //Agregamos los convertidores json
        //Agregamos los convertidores de jackson
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

        //Hacemos el post y obtenemos nuestra respuesta
        ResponseEntity<String> responseEntity=restTemplate.exchange(url, HttpMethod.POST,mensajeriaHttpEntity,String.class);
        String respuesta=responseEntity.getBody();
        return respuesta;

    }


}