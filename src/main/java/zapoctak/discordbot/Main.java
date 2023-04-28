package zapoctak.discordbot;

import net.dv8tion.jda.api.exceptions.InvalidTokenException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // read token from config file given as first argument
        try {
            if (args.length < 1) {
                System.out.println("No config file given!");
                System.exit(1);
            }
            var reader = new BufferedReader(new FileReader(args[0]));
            var token = reader.readLine();
            BotFactory.buildBot(token);
        } catch (FileNotFoundException e) {
            System.out.println("Config file" + args[0] + " not found!");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Error reading config file!");
            System.exit(1);
        } catch (InvalidTokenException e) {
            System.out.println("Invalid token!");
            System.exit(1);
        }
    }
}