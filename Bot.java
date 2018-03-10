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
            to_exchange.println(("ADD 7 BOND BUY 999 2" ).toUpperCase());
            to_exchange.println(("ADD 77 BOND SELL 1001 2" ).toUpperCase());
            int counter = 0;
            int currentBondBuyLists = 0;
            int currentBondSellLists = 0;
            int currentVALEBuyLists = 0;
            int totalVALE =0;
            int currentGSBuyLists =0;
            int currentGSSellLists =0;
            int currentMSBuyLists =0;
            int currentMSSellLists =0;


                while ((reply = from_exchange.readLine()) != null) {
                    switch (reply.toUpperCase()) {
                        case "ACK 7":
                            currentBondBuyLists += 30;
                            System.out.println("BOND Buy ACK");
                            break;

                        case "ACK 77":
                            currentBondSellLists += 30;
                            System.out.println("BOND sell ACK");
                            break;

                        case "ACK 2":
                            currentVALEBuyLists += 2;
                            System.out.println("Vale Buy ACK");
                            break;


                        case "ACK 3":
                            currentGSBuyLists += 2;
                            System.out.println("GS Buy ACK");
                            break;
                        case "ACK 33":
                            currentGSSellLists += 2;
                            System.out.println("GS Sell ACK");

                        case "ACK 4":
                            currentMSBuyLists += 2;
                            System.out.println("MS Buy ACK");
                            break;

                        case "ACK 44":
                            currentMSSellLists += 2;
                            System.out.println("MS Sell ACK");
                            break;

                        case "OUT 7":
                            currentBondBuyLists = 0;
                            System.out.println("BOND BUY OUT");
                            break;

                        case "OUT 77":
                            currentBondSellLists = 0;
                            System.out.println("BOND SELL OUT");
                            break;
                        case "OUT 2":
                            currentVALEBuyLists = 0;
                            System.out.println("VALE Buy OUT");
                            totalVALE += 2;
                            System.out.println("totalVale :"+totalVALE);
                            if (totalVALE >= 10) {
                                to_exchange.println(("CONVERT 10 VALBZ BUY 10").toUpperCase());
                                System.out.println("Converting Vale");
                            }
                            break;

                        case "OUT 3":
                            currentGSBuyLists = 0;
                            System.out.println("GS Buy OUT");
                            break;

                        case "OUT 33":
                            currentGSSellLists = 0;
                            System.out.println("GS Sell OUT");
                            break;

                        case "OUT 4":
                            currentMSBuyLists = 0;
                            System.out.println("MS Buy OUT");
                            break;
                        case "OUT 44":
                            currentMSSellLists = 0;
                            System.out.println("MS Sell OUT");
                            break;

                        case "REJECT 77 DUPLICATE_ORDER_ID":
                            counter = 0;
                            break;

                        case "REJECT 7 DUPLICATE_ORDER_ID":
                            counter = 0;
                            break;
                        case "CLOSE":
                            Thread.sleep(3000);
                            break;
                    }
                    if (reply.toUpperCase().length() < 10) {
                        reply = from_exchange.readLine();
                    }

                    switch (reply.toUpperCase().substring(0, 9)) {
                        case "BOOK VALE":
                            if (currentVALEBuyLists <= 0 && counter > 35) {
                                to_exchange.println(("ADD 2 VALE BUY " + (Integer.parseInt(reply.substring(14, 18))+1) + " 2").toUpperCase());
                                System.out.println("Buying VALE @ "+reply.substring(14, 18));

                            }

                            switch (reply.toUpperCase().substring(0, 7)) {
                                case "BOOK GS":
                                    //VALBZ_estimatedValue = (int)reply.substring(15,19);
                                    if (currentGSBuyLists <= 0 && counter > 35 && currentGSSellLists <= 0) {
                                        to_exchange.println(("ADD 3 GS BUY " + ((Integer.parseInt(reply.substring(12, 16))) - 1) + " 2").toUpperCase());
                                        System.out.println("Buying GS @ "+reply.substring(15, 19));
                                        for (int x = 0; x < reply.length(); x++) {
                                            if (reply.substring(x, x + 4).equalsIgnoreCase("sell")) {
                                                to_exchange.println(("ADD 33 GS SELL " + (Integer.parseInt(reply.substring(x + 5, x + 9)) + 1) + " 2").toUpperCase());
                                                System.out.println("Selling GS @"+reply.substring(x + 5, x + 9));
                                                break;
                                            }
                                        }

                                        break;
                                    }

                                case "BOOK MS":
                                    //VALBZ_estimatedValue = (int)reply.substring(15,19);
                                    if (currentMSBuyLists <= 0 && counter > 35 && currentMSSellLists <= 0) {
                                        to_exchange.println(("ADD 4 MS BUY " + ((Integer.parseInt(reply.substring(12, 16))) - 1) + " 2").toUpperCase());
                                        System.out.println("Buying MS @ "+reply.substring(15, 19));
                                        for (int x = 0; x < reply.length(); x++) {
                                            if (reply.substring(x, x + 4).equalsIgnoreCase("sell")) {
                                                to_exchange.println(("ADD 44 MS SELL " + (Integer.parseInt(reply.substring(x + 5, x + 9))) + " 2").toUpperCase());
                                                System.out.println("Selling MS @ "+reply.substring(x + 5, x + 9));
                                                break;
                                            }
                                        }

                                        break;
                                    }
                            }


                            if (currentBondBuyLists <= 0 && counter > 35) {
                                to_exchange.println(("ADD 7 BOND BUY 999 10").toUpperCase());
                                System.out.println("Buying BOND @ 999");
                            } else if (currentBondSellLists <= 0 && counter > 35) {
                                to_exchange.println(("ADD 77 BOND SELL 1001 10").toUpperCase());
                                System.out.println("Selling BOND @ 1001");
                            }
                            counter++;
                    }
                }
            }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
    }

}