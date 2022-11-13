import java.util.ArrayList;
import java.util.stream.Stream;

public class Tokenizer {
    private Automation analisator = new Automation();
    public enum TokenType{
        COMMENT,
        HEADER,
        PREPROCESSOR_DIRECTIVE,
        WRONG_STRING_LITERALS,
        WRONG_NUMBER_LITERALS,
        STRING_LITERAL,
        CHAR_LITERALS,
        ERROR,
        KEYWORD,
        NUMBER_LITERALS,
        IDENTIFIER,
        OPERATORS,
        PUNCTUATIONS;
    }
    private ArrayList<Token> tokens = new ArrayList<Token>();
    public void Clear(){
        tokens.clear();
    }
    public ArrayList<Token> Analise(Stream<String> text){
        text.forEach((s) -> {tokens.addAll(analisator.GetTokensFromString(s));});
        return tokens;
    }

}
