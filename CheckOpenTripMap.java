import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
public class CheckOpenTripMap {
    public static void main(String[] args) throws Exception {
        String url = \"https://api.opentripmap.com/0.1/en/geoname?name=PARIS&apikey=5ae2e3f221c38a28845f05b6f1099b090a7c56fae432d959577af72f\";
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod(\"GET\");
        int status = con.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();
        System.out.println(\"Status: \" + status);
    }
}
