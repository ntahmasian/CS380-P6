/**
 * Created by Narvik on 5/25/17.
 */
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TicTacToeClient {
            Scanner kb = new Scanner(System.in);
            CommandMessage command;
            MoveMessage move;
    BoardMessage board;
    Object serverMessage;
    ObjectOutputStream send;
    ObjectInputStream receive;

    boolean inProgress = false;

    public static void main(String[] args) {
        TicTacToeClient ticTacToe = new TicTacToeClient();
        try {
            ticTacToe.run();
        }
        catch (Exception err) {

        }
    }

    private void run() throws UnknownHostException, IOException {
        final Socket socket = new Socket("codebank.xyz", 38006);

        try {
            send = new ObjectOutputStream(socket.getOutputStream());
            receive = new ObjectInputStream(socket.getInputStream());
            int option = 4;

            Thread thread = new Thread() {
                public void run() {
                    try {
                        while (true) {
                            serverMessage = receive.readObject();

                            if (serverMessage instanceof Message) {
                                if (!(serverMessage instanceof ErrorMessage) && !(serverMessage instanceof BoardMessage))
                                    System.out.println(serverMessage);
                            }

                            if (serverMessage instanceof ErrorMessage) {
                                System.out.println();
                                System.out.println(((ErrorMessage) serverMessage).getError());
                                System.out.println();
                            }

                            if (serverMessage instanceof BoardMessage) {
                                board = (BoardMessage) serverMessage;
                                printBoard(board.getBoard());

                                if (board.getStatus() == BoardMessage.Status.IN_PROGRESS) {
                                    inProgress = true;
                                }else {
                                    System.out.println(board.getStatus());
                                    inProgress = false;
                                    System.out.println("Goodbye!");
                                    socket.close();
                                    System.exit(0);
                                }
                            }
                        }
                    }
                    catch (EOFException endOfFile) {
                        System.out.println("Too slow, ran out of time, ending game");

                        try {
                            socket.close();
                        }
                        catch (Exception err) {}
                        System.exit(0);
                    }
                    catch (Exception err) {
                        err.printStackTrace();
                    }
                }
            };

            thread.start();

            System.out.print("Please enter your username: ");
            String username = kb.nextLine();
            ConnectMessage name = new ConnectMessage(username);
            send.writeObject(name);

            System.out.print("Available commands are\n(1) start a new game\n"+
                    "(0) exit\nMake a selection by typing the corresponding number and hit return: ");
            option = kb.nextInt();
            kb.nextLine();

            while (true) {
                switch (option) {
                    case 0: {
                        command = new CommandMessage(CommandMessage.Command.EXIT);
                        send.writeObject(command);
                        System.out.println("Goodbye!");
                        socket.close();
                        System.exit(0);
                    }
                    case 1: {
                        if (!inProgress) {
                            command = new CommandMessage(CommandMessage.Command.NEW_GAME);
                            send.writeObject(command);
                            System.out.print("You are player 1 make a move");
                            inProgress = true;
                        }
                        System.out.println();
                        break;
                    }
                    case 2: {
                        if (inProgress) {
                            byte row = -1, col = -1;

                            while (row < 0 || row > 2) {
                                System.out.print("Make a selection between rows (0-2): ");
                                row = kb.nextByte();
                                kb.nextLine();
                            }

                            while (col < 0 || col > 2) {
                                System.out.print("Make a selection between columns (0-2): ");
                                col = kb.nextByte();
                                kb.nextLine();
                            }
                            move = new MoveMessage(row, col);
                            send.writeObject(move);
                        }
                        break;
                    }
                    case 3: {
                        if (inProgress) {
                            command = new CommandMessage(CommandMessage.Command.SURRENDER);
                            send.writeObject(command);
                            System.out.println("You have surrendered\nGame over");

                            inProgress = false;
                            break;
                        }
                        inProgress = false;
                        break;
                    }
                    default: {
                        System.out.println("Invalid entry!");
                    }
                }
                System.out.println("-----------------------");
                System.out.println("Please select an option");

                if (!inProgress)
                    System.out.println("(1) New game\n(0) Exit");
                if (inProgress) {
                    System.out.println("(2) Make a move");
                    System.out.println("(3) Surrender");
                }
                option = kb.nextInt();
                kb.nextLine();
            }
        }
        catch (Exception err) {}
    }

    private void printBoard(byte[][] board) {
        System.out.println("Current board: \n***************");
        for (int row = 0; row < board.length; row++) {
            System.out.print("\t ");
            for (int col = 0; col < board[row].length; col++) {
                System.out.print(board[row][col] + " ");
            }
            System.out.println();
        }
        System.out.println("***************");
    }
}