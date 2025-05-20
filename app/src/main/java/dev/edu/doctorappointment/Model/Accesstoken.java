package dev.edu.doctorappointment.Model;

import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;

import org.apache.commons.logging.impl.Log4JLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Accesstoken {
    private static final String firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging";

    public String getAccessToken(){
        try {
            String jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"doctor-appointment-f5d32\",\n" +
                    "  \"private_key_id\": \"5239a582fa2f3578fa8df2754d12920a9138f18a\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDtPLs6hgAKDc0X\\n1U7IcHQMYY8zAP++HGbl9DutIBLcuSrndswTpScALWR1n4tHSGllh9BF/GQ/kIKC\\n2DMeUEmu28uSvWm+b9qvEe7EkPcgu06yYC5OpUcywVe9sSJrRzeyeSRGb8E1G7hi\\nS8Sk5t6mA2Qq/8jpdqGfK+N4botIJu4gMm1xutaL2f7bINlNoXwvIhST7wc1OrJQ\\nXOzsgDj0pONTmYhogP1bqDErq3chCZKqpzrJevm66Q0WLkV1HHr2SZrx+IaxogTf\\n0d7T+KSa1aDmYnPnVb744ClqQylUVzoqDSDC0Jg2/c5s4o5AfYvtDSogfQO+xJxq\\nUXXBL9+XAgMBAAECggEAWYHk6B8/q4tqepgF2fUnq7YxV56EXx+LKS6dR2QHCdUp\\nHVyCgdT5YoU91m0kGPh2n5J+37Dqx9rHiwN0Qfcr3bfa1ln1qB6+pPgAnJRPUT+q\\nYP2N6ylLtBgjeRP4dv5PQtyW9aZbgKSdTCTnJAphLcSwydJo5MDYGNxvvfQDQ+yG\\nMOKs2ESDx95ijb+BVnk5c85Hd00AE8tpTLckPqX4xm1b+aqe4PrMhjNQlif/gBWT\\naXVp4ftWY+906gfYiACu+UTpFdzd2T+/6qH00MHla2p81sZEJaOHgif7rhv47vP0\\nK47Vn7isbcDiK8atX+OpHE2k8dRR6Vr5X4c5yvB/RQKBgQD+8YSHjo/Y6+R75/F8\\nNxAPoM2VBnExKp13DfMzipzGKHkcODx6pMvomiMEZsEZbA72FcE2JUPu1eTWKxCB\\nXR1RZISbUvQLzEpa5HHg1nAvIUCmwiah3vvslaJ6+rsnTO9PFBWMZN8RFaMYIgqp\\nBJ+UUhdaYlOQ3g56wQokBvpo6wKBgQDuOG2jWPgFZr7DprsQhABdfzDyj6U3TAa2\\n+XWVj/ADTeVhujppSUsuw2qhnc9yKLJUkwbAah7dJASFgUhSi753DxTa0VG/3Ony\\n4BR0jG6Jo82zNBQInbM3L2RSS7u416xiF16zjOhw+Z2DGqp+rHlWsfu0gkKmuaEK\\nHi5z2u65BQKBgQDnOMc5ZtwiIng8xLWpMgt4ESDDq28X542fP+C2e14K/wQwyvRX\\nS4XAtaNSx5sU4oODP2HYKN4/DHGaqL40he4kNuVqNkcvOB6r5ws9n19p71/Cns4I\\n+8NC5MfTf/oj/8v/zbhpd8c0qIoeRimi95WAISAbEpi2CB/bx57Z5BZS9wKBgGes\\nszqF+9sN7jPkGkGD0UzfkAPgtDyun7V+N8hRjllBy3SOweEUft3pOj0AG0pJn+Li\\n4etXw/rmM/mOXFVgcBIMxDy/IF7p2/9RulVG0MyiN2GoZpehWl/IBlcyko5Nfr5q\\nAVMPHhIc8IogTtc2ZNBwQWU705oP66Alpn3RaFepAoGBAIn8Vzu1Y+M8xJNIDaQv\\nTT+OFGyGKbBZGKXJl3HKU9xOJ8GY7rWP7Id+y2OUrfxemrnY67PHqxBH4f0UmvQM\\necuvvXvpLqIoRwhdBQnvA7kcKVhup/aOVzRNssaiyMdN89oBMaT1lzuzAlyQieTc\\n/9TtvcwZfjFe/Yo5r16vEjKN\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-fbsvc@doctor-appointment-f5d32.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"106703809940197748441\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40doctor-appointment-f5d32.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n";

            InputStream steam = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));

            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(steam).createScoped(Lists.newArrayList(firebaseMessagingScope));
            googleCredentials.refresh();
            Log.d("123", "" + googleCredentials.getAccessToken());
            return googleCredentials.getAccessToken().getTokenValue();
        }catch (IOException e){
            Log.e("error", "" + e.getMessage());
            return null;
        }
    }
}
