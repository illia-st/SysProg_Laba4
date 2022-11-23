import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Stream;

public class Tokenizer {
    // keywords
    public static HashSet<String> keywords = Service.newHashSet(
    "auto", 	"double", 	"int",	"struct",
    "break", "else", "long", "switch",
    "case", "enum", "register", "typedef",
    "char",	"extern", "return", "union",
    "continue",	"for", "signed", "void",
    "do", "if", "static", "while",
    "default", "goto", "sizeof", "volatile",
    "const", "float", "short", "unsigned"
    );
    // operators
    public static HashSet<String> operators = Service.newHashSet(
            "-","=","+", "...",
            "*","/","%","~","|","&","<<",">>","^","==","!=",
            "<",">","<=",">=","<=>","++","--","!","&&","||","."
    );
    // directives
    public static HashSet<String> directives = Service.newHashSet(
            "#define", "#include", "#ifdef", "ifndef",
            "#endif", "#error", "#if", "#elif", "#else", "#import", "#line", "#using", "#undef"
    );
    // punctuations
    private final HashSet<String> punctuations = Service.newHashSet(")", "(", "[", "]", ",", ".", "{", "}", ";", "?", " ", "\n");
    // hex letters
    public static final HashSet<Character> allowed_letters_in_hex = Service.newHashSet('A', 'B', 'C', 'D', 'E', 'F');
    public static final HashSet<String> special_characters = Service.newHashSet("\\\\", "\\0", "\\'", "\\t", "\\0", "\\n");
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
