import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Bot
{
    public static void main(String[] args)
    {
        /* The boolean passed to the Configuration constructor dictates whether or not the
           bot is connecting to the prod or test exchange. Be careful with this switch! */
        Configuration config = new Configuration(false);
        try
        {
            Socket skt = new Socket(config.exchange_name(), config.port());
            BufferedReader from_exchange = new BufferedReader(new InputStreamReader(skt.getInputStream()));
            PrintWriter to_exchange = new PrintWriter(skt.getOutputStream(), true);

            /*
              A common mistake people make is to to_exchange.println() > 1
              time for every from_exchange.readLine() response.
              Since many write messages generate marketdata, this will cause an
              exponential explosion in pending messages. Please, don't do that!
            */
            to_exchange.println(("HELLO " + config.team_name).toUpperCase());
            String reply;
            int counter = 0;
            while((reply = from_exchange.readLine())!=null)
            {
                System.err.printf("The exchange replied: %s\n", reply);
                reply = from_exchange.readLine().trim();
                if(counter<30)
                {
                    counter++;
                }
                else
                {
                    to_exchange.println(("ADD 7 BOND BUY 998 10" + config.team_name).toUpperCase());
                    to_exchange.println(("ADD 7 BOND SELL 1002 10" + config.team_name).toUpperCase());
                    counter = 0;
                }

            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
    }

}
