import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

/**
 * Created by Admin on 07.06.2016.
 */
public class Main extends JFrame {

    private JPanel panelAddUser, panelDelUser, panelSendM;
    private JLabel userName, userLogin, userPassword, userAcId, userID, userIDS, message;
    private JTextField userNameF, userLoginF, userPasswordF, userAcIdF, userIDF, userIDSF, messageF;
    private JTextArea textArea;
    private JButton addUser, deleteUser, sendMessage, clean;
    public Connection connection;
    public ServerSocket serverSocket;
    Statement statement;
    //ResultSet resultSet;
   static Main main;

    static BufferedReader in;
    static PrintWriter out;
    Socket socket;


    public Main(){
        super("Bank Server");
        setSize(950, 730);
        setLayout(new BorderLayout());
        panelAddUser = new JPanel(new FlowLayout());
        panelDelUser = new JPanel(new FlowLayout());
        panelSendM = new JPanel(new FlowLayout());

        userName = new JLabel("Wpisz imie: ");
        userLogin = new JLabel("Wpisz login: ");
        userPassword = new JLabel("Wpisz haslo: ");
        userAcId = new JLabel("Wpisz numer konta: ");
        userID = new JLabel("Wpisz ID uzytkownika: ");
        userIDS = new JLabel("Wpisz ID: ");
        message = new JLabel("Wpisz tresc wiadomosci: ");

        userNameF = new JTextField();
        userNameF.setPreferredSize(new Dimension(60, 20));
        userLoginF = new JTextField();
        userLoginF.setPreferredSize(new Dimension(60, 20));
        userPasswordF = new JTextField();
        userPasswordF.setPreferredSize(new Dimension(60, 20));
        userAcIdF = new JTextField();
        userAcIdF.setPreferredSize(new Dimension(60, 20));
        userIDF = new JTextField();
        userIDF.setPreferredSize(new Dimension(60, 20));
        userIDSF = new JTextField();
        userIDSF.setPreferredSize(new Dimension(60, 20));
        messageF = new JTextField();
        messageF.setPreferredSize(new Dimension(60, 20));

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setPreferredSize(new Dimension(950, 600));

        addUser = new JButton("Dodaj uzytkownika");
        deleteUser = new JButton("Usun uzytkownika");
        sendMessage = new JButton("Wyslij wiadomosc");
        clean = new JButton("Wyczysc");
        Servise servise = new Servise();
        addUser.addActionListener(servise);
        deleteUser.addActionListener(servise);
        sendMessage.addActionListener(servise);
        clean.addActionListener(servise);

        panelAddUser.add(userName);
        panelAddUser.add(userNameF);
        panelAddUser.add(userLogin);
        panelAddUser.add(userLoginF);
        panelAddUser.add(userPassword);
        panelAddUser.add(userPasswordF);
        panelAddUser.add(userAcId);
        panelAddUser.add(userAcIdF);
        panelAddUser.add(addUser);
        add(panelAddUser, BorderLayout.NORTH);

        panelDelUser.add(userID);
        panelDelUser.add(userIDF);
        panelDelUser.add(deleteUser);
       add(panelDelUser, BorderLayout.BEFORE_LINE_BEGINS);

        panelSendM.add(userIDS);
        panelSendM.add(userIDSF);
        panelSendM.add(message);
        panelSendM.add(messageF);
        panelSendM.add(sendMessage);
        panelSendM.add(clean);
        add(panelSendM, BorderLayout.CENTER);


        add(new JScrollPane(textArea), BorderLayout.SOUTH);

        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

    private class Servise implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
           m: if(e.getActionCommand().equals("Dodaj uzytkownika")){
                String name = userNameF.getText();
                String login = userLoginF.getText();
                String password = userPasswordF.getText();
                String userAcID = userAcIdF.getText();

                try{

                    ResultSet resultSet = statement.executeQuery("SELECT login FROM Userr WHERE login LIKE '"+login+"';");

                    if(resultSet.next()){
                        wyswietlKomunikat("Uzytkownik z takim loginem juz istnieje!\n");
                        userNameF.setText("");
                        userNameF.repaint();
                        userLoginF.setText("");
                        userLoginF.repaint();
                        userPasswordF.setText("");
                        userPasswordF.repaint();
                        userAcIdF.setText("");
                        userAcIdF.repaint();
                        break m;
                    }

                    statement.execute("INSERT INTO 'Userr' ('Name', 'login', 'password', 'a_id') VALUES ('"+name+"', '"+login+"', '"+password+"', '"+userAcID+"');");

                    ResultSet temp = statement.executeQuery("SELECT u_id FROM Userr WHERE login LIKE '"+login+"';");
                    String id=temp.getString("u_id");
                    statement.execute("INSERT INTO 'Account' ('money', 'u_id') VALUES ('0', '"+id+"');");

                    userNameF.setText("");
                    userNameF.repaint();
                    userLoginF.setText("");
                    userLoginF.repaint();
                    userPasswordF.setText("");
                    userPasswordF.repaint();
                    userAcIdF.setText("");
                    userAcIdF.repaint();

                    wyswietlKomunikat("Dodano uzytkownika "+ name);
                    test();
                }catch (SQLException ex){
                    System.out.println("SQLException");
                }
            }
            if(e.getActionCommand().equals("Usun uzytkownika")){
                String id = userIDF.getText();
                try{
                    statement.execute("DELETE FROM Userr WHERE u_id = "+id+";");
                    wyswietlKomunikat("usunieto uzytkownika z id "+id);
                    userIDF.setText("");
                    userIDF.repaint();
                    test();
                }catch (SQLException ex){
                    System.out.println("SQLException");
                }
            }
            if(e.getActionCommand().equals("Wyslij wiadomosc")){
                String id = userIDSF.getText();
                String message = messageF.getText();
                if(!id.equals("100")){
                    try{
                        statement.execute("INSERT INTO 'Message' ('message_v', 'sender', 'reciver') VALUES ('"+message+"', '0', '"+id+"');");
                        testMessage(id);
                        userIDSF.setText("");
                        userIDSF.repaint();
                        messageF.setText("");
                        messageF.repaint();

                    }catch (SQLException ex){
                        System.out.println("SQLException");
                    }
                }
                if(id.equals("100")){
                    try{
                        statement.execute("INSERT INTO 'Message' ('message_v', 'sender', 'reciver') VALUES ('"+message+"', '0', '100');");
                        testMessage(id);
                        userIDSF.setText("");
                        userIDSF.repaint();
                        messageF.setText("");
                        messageF.repaint();
                    }catch (SQLException ex){
                        System.out.println("SQLException");
                    }
                }
            }
            if(e.getActionCommand().equals("Wyczysc")){
                textArea.setText("");
                textArea.repaint();
            }
        }
    }

    public void test(){
        wyswietlKomunikat("\n");
        wyswietlKomunikat("TEST");
        try{
            ResultSet resultSet = statement.executeQuery("SELECT * FROM Userr;");
            while (resultSet.next()){
                String id = resultSet.getString("u_id");
                String name = resultSet.getString("Name");
                String login = resultSet.getString("login");
                String password = resultSet.getString("password");
                String acc = resultSet.getString("a_id");

                wyswietlKomunikat("Id: " +id+ " Name: "+ name+ " Login: "+ login+ " Password: "+ password+ " accNum: "+acc+".\n");
            }
        }catch (SQLException e){
            System.out.println("SQL Exception in test()");
        }

    }

    public void testMessage(String id){
        wyswietlKomunikat("\n");
        wyswietlKomunikat("TEST MESSAGE\n");
        try{
            ResultSet resultSet = statement.executeQuery("SELECT message_v FROM Message WHERE reciver LIKE'"+id+"' OR reciver LIKE '100';");
            while (resultSet.next()){

                String message = resultSet.getString("message_v");
                wyswietlKomunikat("Messages: " +message+".\n");
            }
        }catch (SQLException e){
            System.out.println("SQL Exception in testMessage()");
        }
    }

    public void wyswietlKomunikat(String tekst){
        textArea.append(tekst);
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    public void start(){

        try{


            serverSocket = new ServerSocket(1234);
            connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/Bank.s3db");
            wyswietlKomunikat("Serwer uruchomiony na porcie 1234 \n");
            wyswietlKomunikat("Polaczenie z baza danych\n");
            statement = connection.createStatement();
            test();
            //testMessage("2");
            while (true){
                socket = serverSocket.accept();

                wyswietlKomunikat("nowy klient");


                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                boolean ok = false;
                String login;
                String password;


                while (!ok){
                            login=in.readLine();
                            password=in.readLine();

                            ResultSet resultSet = statement.executeQuery("SELECT * FROM Userr WHERE login LIKE '"+login+"' AND password LIKE '"+password+"';");
                            if(resultSet.next()){
                                String id = resultSet.getString("u_id");
                                String name = resultSet.getString("Name");
                                String accNum = resultSet.getString("a_id");
                                String money="";
                                ResultSet t = statement.executeQuery("SELECT money FROM Account WHERE a_id LIKE '"+accNum+"';");
                                if(t.next()){
                                    money=t.getString("money");
                                }


                                out.println(id);
                                out.println(name);
                                out.println(accNum);
                                out.println(money);

                                ok=true;
                                wyswietlKomunikat(name+ "pojawil sie na serwerze\n");



                                new Thread(new Cient(socket, id, name, main)).start();

                            }

                            else {
                                out.println("N");
                                out.println("N");
                        out.println("N");
                        out.println("N");
                    }

                }


            }

        }catch (IOException e){
            System.out.println("IOException");
        }
        catch (SQLException e){
            System.out.println("SQL Exception");
        }finally {
            try{
                connection.close();
            }catch (SQLException e){
                System.out.println("finally sql");
            }
        }
    }
    public static void main(String[] args) {
        main = new Main();
        main.start();



    }



}
