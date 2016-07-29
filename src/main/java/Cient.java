import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;


/**
 * Created by Admin on 08.06.2016.
 */
public class Cient implements Runnable {

    Socket socket;
    BufferedReader in;
    PrintWriter out;
    Statement statement;
   public static String id, name;
    Main main;
    boolean ok=false;




    public Cient(Socket socket, String id, String name, Main main){
        this.socket=socket;
        this.id=id;
        this.name=name;
        this.main=main;

    }

    @Override
    public void run() {

        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            statement = main.connection.createStatement();
            System.out.println("Klient: "+name+ " Id: "+id);


            while(true){
                String temp = in.readLine();
                if(temp.equals(Protokol.TRANSACTION)){
                    System.out.println("Transaction");
                    transaction();
                }
                if(temp.equals(Protokol.MESSAGE)){
                    System.out.println("Message");
                    message();
                }
                if(temp.equals(Protokol.HISTORY)){
                    System.out.println("History");
                    history();
                }
                if(temp.equals(Protokol.LOGOUT)){
                    System.out.println("LogOut");
                    logOut();
                }
            }




        }catch (IOException e){
            System.out.println("IO Exception");
        }
        catch (SQLException e){
            System.out.println("SQL Exception");
        }

    }

    public void transaction(){
        String money ="";
        String a_id="";
        try {
           ResultSet resultSet = statement.executeQuery("SELECT money, a_id FROM Account WHERE u_id LIKE '" + id + "';");

            money = resultSet.getString("money");
            System.out.println(money);
            a_id=resultSet.getString("a_id");
            out.println(money);
        }catch (SQLException e) {
            System.out.println("SQL Exception in tranaction 1");
        }

        try{
            String reciver=in.readLine();
            String acc=in.readLine();
            String moneyt=in.readLine();

           int m =  Integer.parseInt(money)- Integer.parseInt(moneyt);

            if(reciver.equals("N")){
                return;
            }
            else {
                statement.execute("UPDATE Account SET money = '" + m + "' WHERE u_id LIKE '" + id + "';");
                statement.execute("INSERT INTO 'History' ('u_id', 'a_id', 'opis', 'kwota') VALUES ('" + id + "', '" + a_id + "','" + reciver + "', '" + moneyt + "' ); ");
            }
        }catch (SQLException e){
            System.out.println("sql exception 2");
        }catch (IOException e){
            System.out.println("IOException 2");
        }

    }

    public void message() {

        try {
            String message;
            int i = 0;
            System.out.println("TEST!!!");
            ResultSet resultSet = statement.executeQuery("SELECT m_id, message_v FROM Message WHERE reciver LIKE '" + id + "' OR reciver LIKE '100';");
            System.out.println("SELECT");
            ArrayList<String> m = new ArrayList<>();
            ArrayList<String> iddd = new ArrayList<>();
            int numberOfElements = 0;


            while (resultSet.next()) {
                message = resultSet.getString("message_v");
                m.add(message);
                String mID = resultSet.getString("m_id");
                iddd.add(mID);
                i++;
                numberOfElements++;
            }


            out.println(numberOfElements);

            for (int ii = 0; ii < numberOfElements; ii++) {
                out.println(m.get(ii));
                out.println(iddd.get(ii));
            }
            while (!ok) {
                try {
                    String messagge, reciver;
                    messagge = in.readLine();
                    reciver = in.readLine();

                    if (!messagge.equals("N") && !messagge.equals("DELETE")) {
                        String idr = "";

                        Statement tempSt = main.connection.createStatement();

                        ResultSet messagRS = tempSt.executeQuery("SELECT u_id FROM Userr WHERE login LIKE '" + reciver + "';");
                        System.out.println("Select 2");
                        idr = messagRS.getString("u_id");

                        System.out.println(messagge + id + idr);
                        tempSt.execute("INSERT INTO Message ('message_v', 'sender', 'reciver') VALUES ('" + messagge + "', '" + id + "', '" + idr + "');");
                        System.out.println("INsert");
                    } else if (messagge.equals("DELETE")) {
                        statement.execute("DELETE FROM Message WHERE m_id LIKE '" + reciver + "';");
                    } else {
                        ok = true;
                    }

                } catch (IOException e) {
                    System.out.println("Insert");
                } finally {
                    statement.close();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();


        }
    }


    public void history(){

        try{
            ResultSet resultSet = statement.executeQuery("SELECT opis, kwota FROM history WHERE u_id LIKE '"+id+"';");

            ArrayList<String> o = new ArrayList<>();
            ArrayList<String> k = new ArrayList<>();
            int count=0;

            while (resultSet.next()) {
                String opis = resultSet.getString("opis");
                o.add(opis);
                String value = resultSet.getString("kwota");
                k.add(value);
                count++;
            }

            out.println(count);
            for(int i=0; i<count; i++){
                out.println(o.get(i));
                out.println(k.get(i));
            }


                boolean okHistory = false;


                while (!okHistory){
                    try {
                        String s =in.readLine();
                        if(s.equals("DELETE")){
                            statement.execute("DELETE FROM history WHERE u_id LIKE '"+id+"';");
                        }
                        if(s.equals("END")){
                            okHistory=true;
                        }


                    }catch (IOException e){
                        e.printStackTrace();
                    }

                }

        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    public void logOut() {

        boolean ok = false;
        String login;
        String password;

        try {

            while (!ok) {
                login = in.readLine();
                password = in.readLine();

                ResultSet resultSet = statement.executeQuery("SELECT * FROM Userr WHERE login LIKE '" + login + "' AND password LIKE '" + password + "';");
                if (resultSet.next()) {
                    String id = resultSet.getString("u_id");
                    String name = resultSet.getString("Name");
                    String accNum = resultSet.getString("a_id");
                    String money = "";
                    ResultSet t = statement.executeQuery("SELECT money FROM Account WHERE a_id LIKE '" + accNum + "';");
                    if (t.next()) {
                        money = t.getString("money");
                    }


                    out.println(id);
                    out.println(name);
                    out.println(accNum);
                    out.println(money);

                    ok = true;
                    main.wyswietlKomunikat(name + "pojawil sie na serwerze\n");


                    new Thread(new Cient(socket, id, name, main)).start();

                } else {
                    out.println("N");
                    out.println("N");
                    out.println("N");
                    out.println("N");
                }
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
