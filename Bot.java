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
            to_exchange.println(("ADD 6 BOND BUY 998 30" ).toUpperCase());
            to_exchange.println(("ADD 66 BOND SELL 1002 30" ).toUpperCase());
            int counter = 0;
            int currentBondBuyLists = 0;
            int currentBondSellLists = 0;
            int currentVALEBuyLists = 0;
            int currentVALESellLists = 0;

            int VALBZ_estimatedValue;
            while((reply = from_exchange.readLine())!=null)
            {
                System.err.printf("The exchange replied: %s\n", reply);
                switch(reply.toUpperCase())
                {
                    case "ACK 7":
                        currentBondBuyLists += 50;
                        break;

                    case "ACK 77":
                        currentBondSellLists += 50;
                        break;

                    case "ACK 2":
                        currentVALEBuyLists += 2;
                        break;

                    case "ACK 22":
                        currentVALESellLists += 2;
                        break;

                    case "OUT 7":
                        currentBondBuyLists = 0;
                        break;

                    case "OUT 77":
                        currentBondSellLists = 0;
                        break;
                    case "OUT 2":
                        currentVALEBuyLists =0;
                        break;
                    case "OUT 22":
                        currentVALESellLists =0;
                        break;

                    case "REJECT 77 DUPLICATE_ORDER_ID":
                        counter = 0;
                        break;

                    case "REJECT 7 DUPLICATE_ORDER_ID":
                        counter = 0;
                        break;
                }
                if(reply.toUpperCase().length() < 10 )
                {
                    reply = from_exchange.readLine();
                }

                switch(reply.toUpperCase().substring(0,9))
                {
                    case "BOOK VALE":
                        //VALBZ_estimatedValue = (int)reply.substring(15,19);
                        if(currentVALEBuyLists <= 0&& counter > 35 && currentVALESellLists <=0) {
                            to_exchange.println(("ADD 2 VALE BUY " + reply.substring(14, 18) + " 2").toUpperCase());
                            System.out.println(reply.substring(15, 19));
                            for(int x = 0; x< reply.length();x++)
                            {
                                if(reply.substring(x,x+4).equalsIgnoreCase("sell"))
                                {
                                    to_exchange.println(("ADD 22 VALE SELL " + reply.substring(x+5, x+9) + " 2").toUpperCase());
                                    System.out.println(reply.substring(x+5,x+9));
                                    break;
                                }
                            }

                            break;
                        }
                }



                if(currentBondBuyLists <= 0 && counter > 35)
                {
                    to_exchange.println(("ADD 7 BOND BUY 999 50" ).toUpperCase());
                }
                else if(currentBondSellLists <= 0 &&  counter > 35)
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
