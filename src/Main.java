import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static String Stop_word = "EXIT";
    private static String Processing(boolean input_file) throws IOException {
        if(input_file) {
            System.out.print("To close the application enter \"EXIT\". Enter the path to your input file > ");
        } else{
            System.out.print("To close the application enter \"EXIT\". Enter the path to your output file > ");
        }
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));
        return reader.readLine();
    }
    private static Stream<String> ReadFile(String path) throws IOException{
        Stream<String> text = null;
        Path path_ = Paths.get(path);
        if(Files.exists(path_)){
            text = Files.lines(path_);
            return text;
        }
        throw new IOException("The file doesn't exist");
    }
    private static void PrintToFile(String path, ArrayList<Tokenizer.Token> tokens) throws IOException{
        File file = new File(path);
        file.createNewFile();
        FileWriter writer = new FileWriter(path);
        for(var token: tokens){
            switch(token.type){
                case PREPROCESSOR_DIRECTIVE:{
                    writer.write("<preprocessor directive> - " + token.value + "\n");
                    break;
                }
                case HEADER:{
                    writer.write("<header> - " + token.value + "\n");
                    break;
                }
                case KEYWORD:{
                    writer.write("<keyword> - " + token.value + "\n");
                    break;
                }
                case IDENTIFIER:{
                    writer.write("<identifier> - " + token.value + "\n");
                    break;
                }
                case STRING_LITERAL:{
                    writer.write("<string literal> - " + token.value + "\n");
                    break;
                }
                case NUMBER_LITERALS:{
                    writer.write("<number> - " + token.value + "\n");
                    break;
                }
                case CHAR_LITERALS:{
                    writer.write("<single character> - " + token.value + "\n");
                    break;
                }
                case PUNCTUATIONS:{
                    writer.write("<punctuation> - " + token.value + "\n");
                    break;
                }
                case OPERATORS:{
                    writer.write("<operator> - " + token.value + "\n");
                    break;
                }
                default:{
                    writer.write("<error> - " + token.value + "\n");
                    break;
                }
            }
        }
        writer.close();

    }
    public static void main(String[] args) {
        while(true) {
            try {
                String file_path = Processing(true);
                if(file_path.equals(Stop_word)){
                    System.out.println("Bye");
                    return;
                }
                String output_file_path = Processing(false);
                Tokenizer tokenizer = new Tokenizer();
                var text = ReadFile(file_path);
                text = Preprocessor.Preprocess(text);
                var tokens = tokenizer.Analise(text);
                PrintToFile(output_file_path, tokens);
                tokenizer.Clear();
            } catch (Exception ex) {
                System.out.println("An exception was occurred. Description: " + ex.getMessage());
            }
        }
    }
}