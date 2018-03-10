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
            String reply;
            to_exchange.println(("HELLO " + config.team_name).toUpperCase());
            reply = from_exchange.readLine().trim();
            System.err.printf("The exchange replied: %s\n", reply);
            to_exchange.println(("ADD 7 BOND BUY 999 50" ).toUpperCase());
            to_exchange.println(("ADD 77 BOND SELL 1001 50" ).toUpperCase());
            int counter = 0;
            int currentBondBuyLists = 0;
            int currentBondSellLists = 0;
            while((reply = from_exchange.readLine())!=null)
            {
                switch(reply.toUpperCase())
                {
                    case "ACK 7":
                        currentBondBuyLists += 50;
                        break;

                    case "ACK 77":
                        currentBondSellLists += 50;
                        break;

                    case "OUT 7":
                        currentBondBuyLists = 0;
                        break;

                    case "OUT 77":
                        currentBondSellLists =0;
                        break;

                    case "REJECT 77 DUPLICATE_ORDER_ID":
                        counter = 0;
                        break;

                    case "REJECT 7 DUPLICATE_ORDER_ID":
                        counter = 0;
                        break;
                }

                System.err.printf("The exchange replied: %s\n", reply);
                reply = from_exchange.readLine().trim();
                if(currentBondBuyLists <= 0 && counter > 30)
                {
                    to_exchange.println(("ADD 7 BOND BUY 999 50" ).toUpperCase());
                }
                else if(currentBondSellLists <= 0 &&  counter > 30)
                {
                    to_exchange.println(("ADD 77 BOND SELL 1001 50" ).toUpperCase());
                }
            counter++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
    }

}
